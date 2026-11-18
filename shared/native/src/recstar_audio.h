#ifndef RECSTAR_AUDIO_H
#define RECSTAR_AUDIO_H

#ifdef _WIN32
#define RA_API __declspec(dllexport)
#else
#define RA_API __attribute__((visibility("default")))
#endif

#define RA_OK 0
#define RA_ERROR (-1)
#define RA_ERROR_NO_DEVICE (-2)
#define RA_ERROR_INVALID_ARGS (-3)

/* Backend ids shared with the Kotlin side. Keep in sync with NativeAudio.kt. */
#define RA_BACKEND_AUTO 0
#define RA_BACKEND_WASAPI 1
#define RA_BACKEND_DSOUND 2
#define RA_BACKEND_WINMM 3
#define RA_BACKEND_COREAUDIO 4
#define RA_BACKEND_PULSEAUDIO 5
#define RA_BACKEND_ALSA 6
#define RA_BACKEND_JACK 7

typedef struct ra_context ra_context;

RA_API const char* ra_version(void);

RA_API int ra_backend_is_available(int backend);

RA_API ra_context* ra_context_create(int backend);
RA_API void ra_context_destroy(ra_context* ctx);

/* Enumerates devices and caches the result. Returns the device count or a negative error. */
RA_API int ra_context_refresh_devices(ra_context* ctx, int is_capture);
RA_API int ra_context_device_name(ra_context* ctx, int is_capture, int index, char* out, int out_size);
RA_API int ra_context_device_is_default(ra_context* ctx, int is_capture, int index);

#endif
