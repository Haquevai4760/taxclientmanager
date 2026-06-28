package com.taxclientmanager.app.utils

import android.content.Context
import com.taxclientmanager.app.data.database.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupUtils {

    fun exportDatabase(context: Context, outStream: java.io.OutputStream): Boolean {
        return try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (dbFile.exists()) {
                FileInputStream(dbFile).use { input ->
                    input.copyTo(outStream)
                }
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(context: Context, inStream: java.io.InputStream): Boolean {
        return try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val dbDir = dbFile.parentFile
            if (dbDir != null && !dbDir.exists()) {
                dbDir.mkdirs()
            }
            
            // Close database before restoring if needed (usually handled by restarting app)
            FileOutputStream(dbFile).use { output ->
                inStream.copyTo(output)
            }
            // Also handle -shm and -wal files if they exist
            context.getDatabasePath("${AppDatabase.DATABASE_NAME}-shm").delete()
            context.getDatabasePath("${AppDatabase.DATABASE_NAME}-wal").delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

