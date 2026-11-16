#define MA_NO_MP3
#define MA_NO_FLAC
#define MA_NO_ENCODING
#define MINIAUDIO_IMPLEMENTATION
#include "../miniaudio/miniaudio.h"

#include "recstar_audio.h"

#include <string.h>

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

    if (error_out != NULL) *error_out = RA_OK;
    if (ctx == NULL || !ra_to_ma_format(format, &ma_fmt) || sample_rate <= 0 || channels <= 0 || buffer_ms <= 0) {
        if (error_out != NULL) *error_out = RA_ERROR_INVALID_ARGS;
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

    if (ma_device_init(&ctx->context, &config, &capture->device) != MA_SUCCESS) {
        ma_pcm_rb_uninit(&capture->ring_buffer);
        ma_free(capture, NULL);
        if (error_out != NULL) *error_out = RA_ERROR;
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
