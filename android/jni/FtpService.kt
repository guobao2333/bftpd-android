package com.example.bftpd

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.File

class FtpService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!BftpdJni.isRunning()) {
            // Package name comes from the .so itself — no hardcoding needed
            val pkg = BftpdJni.getPackageName() ?: packageName
            val conf = generateConfig(pkg)
            if (BftpdJni.start(conf.absolutePath) != 0) stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        BftpdJni.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun generateConfig(pkg: String): File {
        val dataRoot = getExternalFilesDir(null)?.absolutePath ?: filesDir.absolutePath
        val content = """
            PORT=2121
            DENY_LOGIN=no
            ANONYMOUS_USER=no
            USERFILE=${filesDir}/bftpd_users
            DATAROOT=$dataRoot
            LOGFILE=/dev/null
            WTMP_LOG=no
            DO_CHROOT=no
        """.trimIndent()
        return File(filesDir, "bftpd.conf").also { it.writeText(content) }
    }
}
