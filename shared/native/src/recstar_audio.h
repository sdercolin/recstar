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

/* Sample format ids shared with the Kotlin side. Samples are always little-endian and interleaved. */
#define RA_FORMAT_S16 1
#define RA_FORMAT_S24 2
#define RA_FORMAT_S32 3
#define RA_FORMAT_F32 4

typedef struct ra_context ra_context;
typedef struct ra_capture ra_capture;

RA_API const char* ra_version(void);

RA_API int ra_backend_is_available(int backend);

RA_API ra_context* ra_context_create(int backend);
RA_API void ra_context_destroy(ra_context* ctx);

/* Enumerates devices and caches the result. Returns the device count or a negative error. */
RA_API int ra_context_refresh_devices(ra_context* ctx, int is_capture);
RA_API int ra_context_device_name(ra_context* ctx, int is_capture, int index, char* out, int out_size);
RA_API int ra_context_device_is_default(ra_context* ctx, int is_capture, int index);

/*
 * Opens a capture device by name, or the default device if device_name is NULL. Captured frames go into a ring
 * buffer of buffer_ms milliseconds, drained with ra_capture_read. Sets *error_out and returns NULL on failure.
 * Must not run concurrently with other calls on the same context.
 */
RA_API ra_capture* ra_capture_open(ra_context* ctx, const char* device_name, int sample_rate, int format,
                                   int channels, int buffer_ms, int* error_out);
RA_API int ra_capture_start(ra_capture* capture);
RA_API int ra_capture_stop(ra_capture* capture);
/* Returns 1 while the device is running. Turns 0 after ra_capture_stop or when the device stops by itself. */
RA_API int ra_capture_is_running(ra_capture* capture);
/* Copies up to max_frames frames into out without blocking. Returns the number of frames copied. */
RA_API int ra_capture_read(ra_capture* capture, void* out, int max_frames);
/* Returns the number of frames dropped because the ring buffer was full. */
RA_API long long ra_capture_dropped_frames(ra_capture* capture);
RA_API void ra_capture_close(ra_capture* capture);

#endif
