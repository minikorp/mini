package com.minivac.mini.log

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Factory for [FileTree].
 * Multiple instances of this controller over the same folder are not safe.
 *
 * @param relativeLogFolderPath Path for the folder containing multiple log files.
 */
class LogController(
        val context: Context,
        val relativeLogFolderPath: String = "logs",
        val minLogLevel: Int = Log.VERBOSE) {

    companion object {
        private val FILE_NAME_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
    }

    private var _currentFileTree: FileTree? = null
    val currentFileTree: FileTree
        get() = _currentFileTree!!

    private val logsFolder: File? by lazy {
        var root = context.getExternalFilesDir(null)
        if (root == null) {
            //Fall back to private directory
            root = context.filesDir
        }
        val logRootDirectory = File(root.absolutePath, relativeLogFolderPath)
        if (!logRootDirectory.exists()) {
            if (!logRootDirectory.mkdir()) {
                Grove.e { "Unable to create log directory, nothing will be written on disk" }
                return@lazy null
            }
        }
        return@lazy logsFolder
    }

    /**
     * Create a new fileTree and close the previous one if present.
     *
     * This operation may block, consider executing it in another thread.
     *
     * @return The logger, or null if the file could not be created.
     */
    fun newFileTree(): FileTree? {
        if (logsFolder == null) return null

        val logFileName = String.format("log-${FILE_NAME_DATE_FORMAT.format(Date())}.txt")
        val logFile = File(logsFolder, logFileName)
        Grove.d { "New session, logs will be stored in: ${logFile.absolutePath}" }
        _currentFileTree?.exit()
        _currentFileTree = FileTree(logFile, minLogLevel)
        return _currentFileTree
    }

    /**
     * Delete any log files created under [logsFolder] older that `maxAge` in ms.
     *
     * This operation may block, consider executing it in another thread.
     *
     * Current log file wont be deleted.
     */
    fun deleteOldLogs(maxAge: Long, maxCount: Int = Int.MAX_VALUE): Int {
        var deleted = 0
        val files = logsFolder?.listFiles()
        files?.apply {
            for ((i, file) in this.withIndex()) {
                val lastModified = System.currentTimeMillis() - file.lastModified()
                if (lastModified > maxAge || i > maxCount) {
                    val isCurrentLogFile = _currentFileTree?.file?.absolutePath == file.absolutePath
                    if (!isCurrentLogFile && file.delete()) deleted++
                }
            }
        }
        return deleted
    }
}