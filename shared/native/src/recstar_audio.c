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
