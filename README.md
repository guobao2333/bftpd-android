# bftpd-android

Cross-compile bftpd as `libbftpd.so` for embedding in Android apps via JNI or dlopen.

## Two load paths

### Path A — dlopen() (no JVM required)
```c
void* handle = dlopen("/path/to/libbftpd.so", RTLD_NOW);
// bftpd is already running — __attribute__((constructor)) fired on load
// package name auto-read from /proc/self/cmdline
// config auto-created at /data/data/<pkg>/files/bftpd.conf if missing
```

### Path B — System.loadLibrary() (JVM present)
```kotlin
// BftpdLoader triggers System.loadLibrary("bftpd")
// on_load() fires first (ELF init_array), bftpd starts automatically
// RegisterNatives binds methods onto BftpdJni — any package name works
bftpd.BftpdLoader.registerOn(BftpdJni::class.java)
BftpdJni.isRunning()   // true — already started by on_load()
```

Both paths share the same server thread. If both are exercised in the same
process, `g_auto_started` prevents a double-start.

## Repository layout

```
.github/workflows/build-bftpd.yml   GitHub Actions — builds all 4 ABIs
patches/
  0001-bionic-compat.patch           Remove utmp/PAM/libcap (Bionic incompatible)
  0002-no-fork-use-pthread.patch     Replace fork() with pthread_create()
  0003-jni-entry.patch               constructor + JNI entry + exit() interceptor
  0004-makefile-so.patch             Build as shared library, enable cross-compile
android/jni/
  BftpdLoader.kt                     Fixed bootstrap (package "bftpd", stable name)
  BftpdJni.kt                        Host app JNI interface (rename package freely)
  FtpService.kt                      Example foreground Service
```

## Setup

1. Push to `main` — Actions builds `libbftpd.so` for arm64-v8a, armeabi-v7a, x86_64, x86.
2. Drop Release artifacts into your app:
   ```
   app/src/main/jniLibs/
   ├── arm64-v8a/libbftpd.so
   ├── armeabi-v7a/libbftpd.so
   ├── x86_64/libbftpd.so
   └── x86/libbftpd.so
   ```
3. Copy the three Kotlin files, rename package in BftpdJni.kt and FtpService.kt.

## Notes

- Default port: **2121** (no root needed)
- Config auto-created at `/data/data/<pkg>/files/bftpd.conf` on first load
- `exit()` redirected to `bftpd_exit()` via `-Dexit=bftpd_exit` in Makefile —
  bftpd's fatal paths shut down only the server thread, never the host process
- NDK r26d, minSdk 19 (armeabi-v7a/x86) / 21 (arm64/x86_64)
