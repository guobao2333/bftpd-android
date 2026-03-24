package com.example.ftp

/**
 * Host app's JNI interface — package name doesn't matter.
 *
 * Methods are registered dynamically via RegisterNatives in BftpdLoader,
 * so renaming this class or moving it to any package requires zero C changes.
 */
object BftpdJni {
    init {
        // Triggers System.loadLibrary("bftpd") + JNI_OnLoad inside BftpdLoader,
        // then registers start/stop/isRunning/getPackageName onto this class.
        bftpd.BftpdLoader.registerOn(BftpdJni::class.java)
    }

    /** Start FTP server with given config file path. Returns 0 on success, -1 if already running. */
    external fun start(confPath: String): Int

    /** Stop FTP server gracefully (shuts down server thread only, not the process). */
    external fun stop()

    /** True if server thread is alive. */
    external fun isRunning(): Boolean

    /**
     * Get the host application's package name via ActivityThread reflection.
     * Useful if you need the package name inside the native layer (e.g. for
     * constructing paths) without passing it in from Java.
     */
    external fun getPackageName(): String?
}
