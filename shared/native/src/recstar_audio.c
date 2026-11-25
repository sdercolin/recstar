#define MA_NO_MP3
#define MA_NO_FLAC
#define MA_NO_ENCODING
#define MINIAUDIO_IMPLEMENTATION
#include "../miniaudio/miniaudio.h"

#include "recstar_audio.h"

#include <string.h>

#ifdef _WIN32
#include <windows.h>
#endif

struct ra_context {
    ma_context context;
    ma_device_info* playback_infos;
    ma_uint32 playback_count;
    ma_device_info* capture_infos;
    ma_uint32 capture_count;
};

static int ra_to_ma_backend(int backend, ma_backend* out) {
    switch (backend) {
        case RA_BACKEND_WASAPI: *out = ma_backend_wasapi; return 1;
        case RA_BACKEND_DSOUND: *out = ma_backend_dsound; return 1;
        case RA_BACKEND_WINMM: *out = ma_backend_winmm; return 1;
        case RA_BACKEND_COREAUDIO: *out = ma_backend_coreaudio; return 1;
        case RA_BACKEND_PULSEAUDIO: *out = ma_backend_pulseaudio; return 1;
        case RA_BACKEND_ALSA: *out = ma_backend_alsa; return 1;
        case RA_BACKEND_JACK: *out = ma_backend_jack; return 1;
        default: return 0;
    }
}

const char* ra_version(void) {
    return ma_version_string();
}

int ra_backend_is_available(int backend) {
    ma_backend ma;
    if (backend == RA_BACKEND_AUTO) return 1;
    if (!ra_to_ma_backend(backend, &ma)) return 0;
    return ma_is_backend_enabled(ma) ? 1 : 0;
}

ra_context* ra_context_create(int backend) {
    ra_context* ctx = (ra_context*)ma_malloc(sizeof(ra_context), NULL);
    ma_result result;
    if (ctx == NULL) return NULL;
    memset(ctx, 0, sizeof(*ctx));

    if (backend == RA_BACKEND_AUTO) {
        /* Without this, miniaudio falls back to the null backend, which "records" silence without any error. */
        ma_backend backends[MA_BACKEND_COUNT];
        size_t enabled_count = 0;
        ma_uint32 count = 0;
        size_t i;
        if (ma_get_enabled_backends(backends, MA_BACKEND_COUNT, &enabled_count) != MA_SUCCESS) {
            ma_free(ctx, NULL);
            return NULL;
        }
        for (i = 0; i < enabled_count; i++) {
            if (backends[i] != ma_backend_null) backends[count++] = backends[i];
        }
        if (count == 0) {
            ma_free(ctx, NULL);
            return NULL;
        }
        result = ma_context_init(backends, count, NULL, &ctx->context);
    } else {
        ma_backend ma;
        if (!ra_to_ma_backend(backend, &ma)) {
            ma_free(ctx, NULL);
            return NULL;
        }
        result = ma_context_init(&ma, 1, NULL, &ctx->context);
    }
    if (result != MA_SUCCESS) {
        ma_free(ctx, NULL);
        return NULL;
    }
    return ctx;
}

void ra_context_destroy(ra_context* ctx) {
    if (ctx == NULL) return;
    ma_context_uninit(&ctx->context);
    ma_free(ctx, NULL);
}

int ra_context_refresh_devices(ra_context* ctx, int is_capture) {
    if (ctx == NULL) return RA_ERROR_INVALID_ARGS;
    /* The returned arrays are owned by the context and stay valid until the next enumeration. */
    if (ma_context_get_devices(&ctx->context, &ctx->playback_infos, &ctx->playback_count,
                               &ctx->capture_infos, &ctx->capture_count) != MA_SUCCESS) {
        ctx->playback_count = 0;
        ctx->capture_count = 0;
        return RA_ERROR;
    }
    return (int)(is_capture ? ctx->capture_count : ctx->playback_count);
}

static ma_device_info* ra_device_info_at(ra_context* ctx, int is_capture, int index) {
    if (ctx == NULL || index < 0) return NULL;
    if (is_capture) {
        return (ma_uint32)index < ctx->capture_count ? &ctx->capture_infos[index] : NULL;
    }
    return (ma_uint32)index < ctx->playback_count ? &ctx->playback_infos[index] : NULL;
}

int ra_context_device_name(ra_context* ctx, int is_capture, int index, char* out, int out_size) {
    ma_device_info* info = ra_device_info_at(ctx, is_capture, index);
    if (info == NULL || out == NULL || out_size <= 0) return RA_ERROR_INVALID_ARGS;
    ma_strncpy_s(out, (size_t)out_size, info->name, (size_t)-1);
    return RA_OK;
}

int ra_context_device_is_default(ra_context* ctx, int is_capture, int index) {
    ma_device_info* info = ra_device_info_at(ctx, is_capture, index);
    if (info == NULL) return 0;
    return info->isDefault ? 1 : 0;
}

static int ra_to_ma_format(int format, ma_format* out) {
    switch (format) {
        case RA_FORMAT_S16: *out = ma_format_s16; return 1;
        case RA_FORMAT_S24: *out = ma_format_s24; return 1;
        case RA_FORMAT_S32: *out = ma_format_s32; return 1;
        case RA_FORMAT_F32: *out = ma_format_f32; return 1;
        default: return 0;
    }
}

static const ma_device_id* ra_find_device_id(ra_context* ctx, ma_device_type type, const char* name) {
    ma_device_info* infos;
    ma_uint32 count;
    ma_uint32 i;
    if (name == NULL) return NULL;
    if (type == ma_device_type_capture) {
        infos = ctx->capture_infos;
        count = ctx->capture_count;
    } else {
        infos = ctx->playback_infos;
        count = ctx->playback_count;
    }
    for (i = 0; i < count; i++) {
        if (strcmp(infos[i].name, name) == 0) return &infos[i].id;
    }
    return NULL;
}

struct ra_capture {
    ma_device device;
    ma_pcm_rb ring_buffer;
    ma_uint32 bytes_per_frame;
    MA_ATOMIC(8, ma_uint64) dropped_frames;
};

static void ra_capture_callback(ma_device* device, void* output, const void* input, ma_uint32 frame_count) {
    ra_capture* capture = (ra_capture*)device->pUserData;
    const ma_uint8* source = (const ma_uint8*)input;
    ma_uint32 remaining = frame_count;
    (void)output;

    /* Two passes cover the wrap-around at the end of the ring buffer. */
    while (remaining > 0) {
        ma_uint32 frames = remaining;
        void* destination;
        if (ma_pcm_rb_acquire_write(&capture->ring_buffer, &frames, &destination) != MA_SUCCESS || frames == 0) {
            break;
        }
        memcpy(destination, source, (size_t)frames * capture->bytes_per_frame);
        ma_pcm_rb_commit_write(&capture->ring_buffer, frames);
        source += (size_t)frames * capture->bytes_per_frame;
        remaining -= frames;
    }
    if (remaining > 0) {
        ma_atomic_fetch_add_64(&capture->dropped_frames, remaining);
    }
}

ra_capture* ra_capture_open(ra_context* ctx, const char* device_name, int sample_rate, int format, int channels,
                            int buffer_ms, int* error_out) {
    ma_format ma_fmt;
    ma_device_config config;
    const ma_device_id* device_id = NULL;
    ra_capture* capture;
    ma_uint32 buffer_frames;
    ma_result result;

    if (error_out != NULL) *error_out = RA_OK;
    if (ctx == NULL || buffer_ms <= 0) {
        if (error_out != NULL) *error_out = RA_ERROR_INVALID_ARGS;
        return NULL;
    }
    if (!ra_to_ma_format(format, &ma_fmt) || sample_rate <= 0 || channels <= 0) {
        if (error_out != NULL) *error_out = RA_ERROR_UNSUPPORTED_FORMAT;
        return NULL;
    }
    if (device_name != NULL) {
        ra_context_refresh_devices(ctx, 1);
        device_id = ra_find_device_id(ctx, ma_device_type_capture, device_name);
        if (device_id == NULL) {
            if (error_out != NULL) *error_out = RA_ERROR_NO_DEVICE;
            return NULL;
        }
    }

    capture = (ra_capture*)ma_malloc(sizeof(ra_capture), NULL);
    if (capture == NULL) {
        if (error_out != NULL) *error_out = RA_ERROR;
        return NULL;
    }
    memset(capture, 0, sizeof(*capture));
    capture->bytes_per_frame = ma_get_bytes_per_frame(ma_fmt, (ma_uint32)channels);

    buffer_frames = (ma_uint32)((ma_uint64)sample_rate * (ma_uint64)buffer_ms / 1000);
    if (ma_pcm_rb_init(ma_fmt, (ma_uint32)channels, buffer_frames, NULL, NULL, &capture->ring_buffer) != MA_SUCCESS) {
        ma_free(capture, NULL);
        if (error_out != NULL) *error_out = RA_ERROR;
        return NULL;
    }

    config = ma_device_config_init(ma_device_type_capture);
    config.capture.pDeviceID = device_id;
    config.capture.format = ma_fmt;
    config.capture.channels = (ma_uint32)channels;
    config.sampleRate = (ma_uint32)sample_rate;
    config.dataCallback = ra_capture_callback;
    config.pUserData = capture;

    result = ma_device_init(&ctx->context, &config, &capture->device);
    if (result != MA_SUCCESS) {
        ma_pcm_rb_uninit(&capture->ring_buffer);
        ma_free(capture, NULL);
        if (error_out != NULL) *error_out = result == MA_FORMAT_NOT_SUPPORTED ? RA_ERROR_UNSUPPORTED_FORMAT : RA_ERROR;
        return NULL;
    }
    return capture;
}

int ra_capture_start(ra_capture* capture) {
    if (capture == NULL) return RA_ERROR_INVALID_ARGS;
    return ma_device_start(&capture->device) == MA_SUCCESS ? RA_OK : RA_ERROR;
}

int ra_capture_stop(ra_capture* capture) {
    if (capture == NULL) return RA_ERROR_INVALID_ARGS;
    return ma_device_stop(&capture->device) == MA_SUCCESS ? RA_OK : RA_ERROR;
}

int ra_capture_is_running(ra_capture* capture) {
    if (capture == NULL) return 0;
    return ma_device_is_started(&capture->device) ? 1 : 0;
}

int ra_capture_read(ra_capture* capture, void* out, int max_frames) {
    ma_uint8* destination = (ma_uint8*)out;
    ma_uint32 remaining;
    int total = 0;
    if (capture == NULL || out == NULL || max_frames <= 0) return 0;
    remaining = (ma_uint32)max_frames;
    while (remaining > 0) {
        ma_uint32 frames = remaining;
        void* source;
        if (ma_pcm_rb_acquire_read(&capture->ring_buffer, &frames, &source) != MA_SUCCESS || frames == 0) {
            break;
        }
        memcpy(destination, source, (size_t)frames * capture->bytes_per_frame);
        ma_pcm_rb_commit_read(&capture->ring_buffer, frames);
        destination += (size_t)frames * capture->bytes_per_frame;
        remaining -= frames;
        total += (int)frames;
    }
    return total;
}

long long ra_capture_dropped_frames(ra_capture* capture) {
    if (capture == NULL) return 0;
    return (long long)ma_atomic_load_64(&capture->dropped_frames);
}

void ra_capture_close(ra_capture* capture) {
    if (capture == NULL) return;
    ma_device_uninit(&capture->device);
    ma_pcm_rb_uninit(&capture->ring_buffer);
    ma_free(capture, NULL);
}

struct ra_player {
    ma_engine engine;
    ma_sound sound;
    ma_bool32 has_sound;
    float volume;
};

ra_player* ra_player_open(ra_context* ctx, const char* device_name, int* error_out) {
    ma_engine_config config;
    ma_device_id* device_id = NULL;
    ra_player* player;

    if (error_out != NULL) *error_out = RA_OK;
    if (ctx == NULL) {
        if (error_out != NULL) *error_out = RA_ERROR_INVALID_ARGS;
        return NULL;
    }
    if (device_name != NULL) {
        ra_context_refresh_devices(ctx, 0);
        device_id = (ma_device_id*)ra_find_device_id(ctx, ma_device_type_playback, device_name);
        if (device_id == NULL) {
            if (error_out != NULL) *error_out = RA_ERROR_NO_DEVICE;
            return NULL;
        }
    }

    player = (ra_player*)ma_malloc(sizeof(ra_player), NULL);
    if (player == NULL) {
        if (error_out != NULL) *error_out = RA_ERROR;
        return NULL;
    }
    memset(player, 0, sizeof(*player));
    player->volume = 1.0f;

    config = ma_engine_config_init();
    config.pContext = &ctx->context;
    config.pPlaybackDeviceID = device_id;
    config.noAutoStart = MA_TRUE;
    if (ma_engine_init(&config, &player->engine) != MA_SUCCESS) {
        ma_free(player, NULL);
        if (error_out != NULL) *error_out = RA_ERROR;
        return NULL;
    }
    return player;
}

static void ra_player_unload(ra_player* player) {
    if (!player->has_sound) return;
    ma_sound_uninit(&player->sound);
    player->has_sound = MA_FALSE;
}

int ra_player_load(ra_player* player, const char* path) {
    const ma_uint32 flags = MA_SOUND_FLAG_DECODE | MA_SOUND_FLAG_NO_PITCH | MA_SOUND_FLAG_NO_SPATIALIZATION;
    ma_result result;
    if (player == NULL || path == NULL) return RA_ERROR_INVALID_ARGS;
    /* Uninit first so the resource manager drops its cached copy and a modified file is decoded again. */
    ra_player_unload(player);
#ifdef _WIN32
    {
        /* miniaudio opens narrow paths with the ANSI code page on Windows, so convert from UTF-8 explicitly. */
        wchar_t* wide_path;
        int length = MultiByteToWideChar(CP_UTF8, 0, path, -1, NULL, 0);
        if (length <= 0) return RA_ERROR_INVALID_ARGS;
        wide_path = (wchar_t*)ma_malloc(sizeof(wchar_t) * (size_t)length, NULL);
        if (wide_path == NULL) return RA_ERROR;
        MultiByteToWideChar(CP_UTF8, 0, path, -1, wide_path, length);
        result = ma_sound_init_from_file_w(&player->engine, wide_path, flags, NULL, NULL, &player->sound);
        ma_free(wide_path, NULL);
    }
#else
    result = ma_sound_init_from_file(&player->engine, path, flags, NULL, NULL, &player->sound);
#endif
    if (result != MA_SUCCESS) return RA_ERROR;
    player->has_sound = MA_TRUE;
    ma_sound_set_volume(&player->sound, player->volume);
    return RA_OK;
}

static ma_uint32 ra_player_sample_rate(ra_player* player) {
    ma_uint32 sample_rate = 0;
    if (ma_sound_get_data_format(&player->sound, NULL, NULL, &sample_rate, NULL, 0) != MA_SUCCESS) return 0;
    return sample_rate;
}

int ra_player_play(ra_player* player, double start_ms) {
    ma_uint32 sample_rate;
    if (player == NULL || !player->has_sound || start_ms < 0) return RA_ERROR_INVALID_ARGS;
    sample_rate = ra_player_sample_rate(player);
    if (sample_rate == 0) return RA_ERROR;
    ma_sound_stop(&player->sound);
    if (ma_sound_seek_to_pcm_frame(&player->sound, (ma_uint64)(start_ms * sample_rate / 1000.0)) != MA_SUCCESS) {
        return RA_ERROR;
    }
    if (ma_engine_start(&player->engine) != MA_SUCCESS) return RA_ERROR;
    return ma_sound_start(&player->sound) == MA_SUCCESS ? RA_OK : RA_ERROR;
}

int ra_player_stop(ra_player* player) {
    ma_result result = MA_SUCCESS;
    if (player == NULL) return RA_ERROR_INVALID_ARGS;
    if (player->has_sound) result = ma_sound_stop(&player->sound);
    if (ma_engine_stop(&player->engine) != MA_SUCCESS) result = MA_ERROR;
    return result == MA_SUCCESS ? RA_OK : RA_ERROR;
}

void ra_player_set_volume(ra_player* player, float volume) {
    if (player == NULL) return;
    player->volume = volume < 0.0f ? 0.0f : (volume > 1.0f ? 1.0f : volume);
    if (player->has_sound) ma_sound_set_volume(&player->sound, player->volume);
}

int ra_player_is_playing(ra_player* player) {
    if (player == NULL || !player->has_sound) return 0;
    return ma_sound_is_playing(&player->sound) && !ma_sound_at_end(&player->sound) ? 1 : 0;
}

double ra_player_position_ms(ra_player* player) {
    ma_uint64 cursor = 0;
    ma_uint32 sample_rate;
    if (player == NULL || !player->has_sound) return 0;
    sample_rate = ra_player_sample_rate(player);
    if (sample_rate == 0 || ma_sound_get_cursor_in_pcm_frames(&player->sound, &cursor) != MA_SUCCESS) return 0;
    return (double)cursor * 1000.0 / sample_rate;
}

double ra_player_length_ms(ra_player* player) {
    ma_uint64 length = 0;
    ma_uint32 sample_rate;
    if (player == NULL || !player->has_sound) return 0;
    sample_rate = ra_player_sample_rate(player);
    if (sample_rate == 0 || ma_sound_get_length_in_pcm_frames(&player->sound, &length) != MA_SUCCESS) return 0;
    return (double)length * 1000.0 / sample_rate;
}

void ra_player_close(ra_player* player) {
    if (player == NULL) return;
    ra_player_unload(player);
    ma_engine_uninit(&player->engine);
    ma_free(player, NULL);
}
