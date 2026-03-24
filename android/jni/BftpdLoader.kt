package bftpd

/**
 * Internal bootstrap object — package name "bftpd" is fixed and stable.
 *
 * This is the ONLY class whose name appears in a JNI function name on the C side:
 *   Java_bftpd_BftpdLoader_registerOn
 *
 * It exposes a single function that registers the actual bftpd methods onto
 * whatever class the host app provides — so the host's package name never
 * needs to appear anywhere in the native code.
 */
internal object BftpdLoader {
    init {
        System.loadLibrary("bftpd")
    }

    /**
     * Register start / stop / isRunning / getPackageName onto [clazz].
     * Call this from the host app's JNI wrapper class before using any methods.
     */
    external fun registerOn(clazz: Class<*>)
}
