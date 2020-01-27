/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.base.download

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.Runnable
import marabillas.loremar.beedio.base.web.HttpNetwork
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.Channels


class VideoDownloader(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val http = HttpNetwork()
    private val gson = Gson()
    private lateinit var progressThread: HandlerThread
    private lateinit var progressHandler: Handler

    override fun doWork(): Result {
        val notifier = DownloadNotifier(applicationContext, inputData, prepareTargetDirectory())
        progressThread = object : HandlerThread("Progress Tracker Thread") {
            override fun start() {
                super.start()
                progressHandler = Handler(looper)
                startTrackingProgress(notifier)
            }
        }
        progressThread.start()
        try {
            inputData.apply {

                val isChunked = getBoolean(KEY_IS_CHUNKED, false)
                val audioUrl = getString(KEY_AUDIO_URL)

                if (!audioUrl.isNullOrBlank())
                    downloadVideoAudio()
                else if (isChunked)
                    downloadChunkedVideo()
                else
                    downloadDefault()
            }
            progressHandler.removeCallbacksAndMessages(null)
            progressThread.quitSafely()
            notifier.notifyFinish()
            return Result.success()
        } catch (e: DownloadException) {
            e.printStackTrace()
            progressHandler.removeCallbacksAndMessages(null)
            progressThread.quitSafely()
            notifier.stop()
            val eJson = gson.toJson(e)
            val file = File(applicationContext.cacheDir, DOWNLOAD_EXCEPTION_FILE)
            file.writeText(eJson)
            return Result.failure()
        }
    }

    private fun downloadVideoAudio() {
        TODO()
    }

    private fun downloadChunkedVideo() {
        try {
            val name = inputData.getString(KEY_NAME)
            val ext = inputData.getString(KEY_EXT)
            val targetFilename = "$name.$ext"
            val targetDirectory = prepareTargetDirectory().apply {
                if (!exists() && !mkdir() && !createNewFile())
                    throw DownloadException("unavailable target directory")
            }

            val progressFile = File(applicationContext.cacheDir, "$name.dat")
            val videoFile = File(targetDirectory, targetFilename)
            var totalChunks = 0L
            if (progressFile.exists()) {
                val input = FileInputStream(progressFile)
                val data = DataInputStream(input)
                totalChunks = data.readLong()
                data.close()
                input.close()
                if (!videoFile.exists()) {
                    if (!videoFile.createNewFile()) {
                        throw DownloadException("Can't create file for download.")
                    }
                }
            } else if (videoFile.exists()) {
                return
            } else {
                if (!videoFile.createNewFile()) {
                    throw DownloadException("Can't create file for download.")
                }
                if (!progressFile.createNewFile()) {
                    throw DownloadException("Can't create progress file.")
                }
            }

            if (videoFile.exists() && progressFile.exists()) {
                while (true) {
                    val website = inputData.getString(KEY_SOURCE_WEBSITE)
                    var chunkUrl: String? = null
                    when (website) {
                        "dailymotion.com" -> chunkUrl = getNextChunkWithDailymotionRule(totalChunks)
                        "vimeo.com" -> chunkUrl = getNextChunkWithVimeoRule(totalChunks)
                        "twitter.com" -> chunkUrl = getNextChunkWithM3U8Rule(totalChunks)
                        "metacafe.com" -> chunkUrl = getNextChunkWithM3U8Rule(totalChunks)
                        "myspace.com" -> chunkUrl = getNextChunkWithM3U8Rule(totalChunks)
                    }
                    if (chunkUrl == null) {
                        if (!progressFile.delete()) {
                            TODO("Log can't delete progressFile")
                        }
                        return
                    }
                    val bytesOfChunk = ByteArrayOutputStream()
                    try {
                        val conn = http.open(chunkUrl)
                        val readChannel = Channels.newChannel(conn.stream)
                        val writeChannel = Channels.newChannel(bytesOfChunk)
                        var read: Int
                        while (true) {
                            val buffer = ByteBuffer.allocateDirect(1024)
                            read = readChannel.read(buffer)
                            if (read != -1) {
                                buffer.flip()
                                writeChannel.write(buffer)
                            } else {
                                val vAddChunk = FileOutputStream(videoFile, true)
                                vAddChunk.write(bytesOfChunk.toByteArray())
                                val outputStream = FileOutputStream(progressFile, false)
                                val dataOutputStream = DataOutputStream(outputStream)
                                dataOutputStream.writeLong(++totalChunks)
                                dataOutputStream.close()
                                outputStream.close()
                                break
                            }
                        }
                        readChannel.close()
                        conn.stream?.close()
                        bytesOfChunk.close()
                    } catch (e: FileNotFoundException) {
                        if (!progressFile.delete()) {
                            TODO("Log can't delete progressFile")
                        }
                        return
                    } catch (e: IOException) {
                        throw DownloadException("IOException - ${e.message}")
                    }
                }
            }
        } catch (e: IOException) {
            throw DownloadException("IOException - ${e.message}", e)
        } catch (e: Exception) {
            throw DownloadException(e.message ?: "Exception on chunked download", e)
        }
    }

    private fun getNextChunkWithDailymotionRule(totalChunks: Long): String? {
        return inputData.getString(KEY_URL)?.replace("FRAGMENT".toRegex(), "frag(${totalChunks + 1})")
    }

    private fun getNextChunkWithVimeoRule(totalChunks: Long): String? {
        return inputData.getString(KEY_URL)?.replace("SEGMENT".toRegex(), "segment-${totalChunks + 1}")
    }

    private fun getNextChunkWithM3U8Rule(totalChunks: Long): String? {
        val url = inputData.getString(KEY_URL)
        val website = inputData.getString(KEY_SOURCE_WEBSITE)
        if (url == null) throw DownloadException("missing download url")

        var line: String? = null
        try {
            val m3u8Con = http.open(url)
            m3u8Con.stream?.bufferedReader()?.apply {
                while (true) {
                    line = readLine()
                    if (line == null)
                        break

                    if ((website == "twitter.com" || website == "myspace.com") && line!!.endsWith(".ts")) {
                        break
                    } else if (website == "metacafe.com" && line!!.endsWith(".mp4")) {
                        break
                    }
                }
                if (line != null) {
                    var l: Long = 1
                    while (l < totalChunks + 1) {
                        readLine()
                        line = readLine()
                        l++
                    }
                }
                close()
            }
            m3u8Con.stream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (line != null) {
            val prefix: String
            when (website) {
                "twitter.com" -> "https://video.twimg.com$line"
                "metacafe.com" -> {
                    prefix = url.substring(0, url.lastIndexOf("/") + 1)
                    prefix + line
                }
                "myspace.com" -> {
                    prefix = url.substring(0, url.lastIndexOf("/") + 1)
                    prefix + line
                }
                else -> null
            }
        } else {
            null
        }
    }

    private fun downloadDefault() {
        try {
            val url = inputData.getString(KEY_URL)
            val name = inputData.getString(KEY_NAME)
            val ext = inputData.getString(KEY_EXT)
            val totalSize = inputData.getLong(KEY_SIZE, 0)

            if (url == null)
                throw DownloadException("missing download url")

            val targetFilename = "$name.$ext"
            val targetDirectory = prepareTargetDirectory().apply {
                if (!exists() && !mkdir() && !createNewFile())
                    throw DownloadException("unavailable target directory")
            }

            val downloadFile = File(targetDirectory, targetFilename)
            val conn: HttpNetwork.Connection
            val out = if (downloadFile.exists()) {
                val headers = mapOf("Range" to "bytes=${downloadFile.length()}-")
                conn = http.open(url, headers)
                FileOutputStream(downloadFile.absolutePath, true)
            } else {
                conn = http.open(url)
                if (downloadFile.createNewFile())
                    FileOutputStream(downloadFile.absolutePath, true)
                else
                    throw DownloadException("can not create download file")
            }

            if (downloadFile.exists()) {
                val readChannel = Channels.newChannel(conn.stream)
                val fileChannel = out.channel
                while (downloadFile.length() < totalSize) {
                    fileChannel.transferFrom(readChannel, 0, 1024)
                }
                readChannel.close()
                out.flush()
                out.close()
                fileChannel.close()
            } else
                throw DownloadException("no download file")
        } catch (e: FileNotFoundException) {
            throw DownloadException(null, UnavailableException())
        } catch (e: IOException) {
            throw DownloadException("IOException - ${e.message}", e)
        } catch (e: Exception) {
            throw DownloadException(e.message ?: "Exception on default download", e)
        }
    }

    private fun startTrackingProgress(notifier: DownloadNotifier) {
        object : Runnable {
            override fun run() {
                notifier.notifyProgress()
                progressHandler.postDelayed(this, 1000)
            }
        }.run()
    }

    private fun prepareTargetDirectory(): File {
        val downloadFolder = getDownloadFolder(applicationContext)
        if (downloadFolder != null) return downloadFolder

        val message = getUnavailableDownloadFolderMessage(Environment.getExternalStorageState())
        throw DownloadException(message)
    }

    inner class DownloadException(message: String? = null, e: Throwable? = null)
        : Exception(message?.let { "DownloadException: $it" }, e)

    inner class UnavailableException : Exception()

    companion object {
        const val KEY_NAME = "key_name"
        const val KEY_URL = "key_url"
        const val KEY_EXT = "key_ext"
        const val KEY_SIZE = "key_size"
        const val KEY_SOURCE_WEBSITE = "key_source_website"
        const val KEY_IS_CHUNKED = "key_is_chunked"
        const val KEY_AUDIO_URL = "key_audio_url"

        const val DOWNLOAD_EXCEPTION_FILE = "download-exception.json"

        fun getDownloadFolder(context: Context): File? {
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (
                    downloadFolder != null
                    && (downloadFolder.exists() || downloadFolder.mkdir() || downloadFolder.createNewFile())
                    && downloadFolder.canWrite()
            ) {
                return downloadFolder
            }

            val externalStorage = Environment.getExternalStorageDirectory()
            val externalStorageState = Environment.getExternalStorageState()
            if (
                    externalStorage != null
                    && (externalStorage.exists() || externalStorage.mkdir() || externalStorage.createNewFile())
                    && externalStorage.canWrite()
                    && externalStorageState == Environment.MEDIA_MOUNTED
            ) {
                return File(externalStorage, "Download")
            }

            val appExternal = context.getExternalFilesDir(null)
            if (
                    appExternal != null
                    && (appExternal.exists() || appExternal.mkdir() || appExternal.createNewFile())
                    && appExternal.canWrite()) {
                return File(appExternal, "Download")
            }

            return null
        }

        fun getUnavailableDownloadFolderMessage(externalStorageState: String): String {
            return when (externalStorageState) {
                Environment.MEDIA_UNMOUNTABLE -> "External storage is un-mountable."
                Environment.MEDIA_SHARED -> "USB mass storage is turned on. Can not mount external storage."
                Environment.MEDIA_UNMOUNTED -> "External storage is not mounted."
                Environment.MEDIA_MOUNTED_READ_ONLY -> "External storage is mounted but has no write access."
                Environment.MEDIA_BAD_REMOVAL -> "External storage was removed without being properly ejected."
                Environment.MEDIA_REMOVED -> "External storage does not exist. Probably removed."
                Environment.MEDIA_NOFS -> "External storage is blank or has unsupported filesystem."
                Environment.MEDIA_CHECKING -> "Still checking for external storage."
                Environment.MEDIA_EJECTING -> "External storage is currently being ejected."
                Environment.MEDIA_UNKNOWN -> "External storage is not available for some unknown reason."
                Environment.MEDIA_MOUNTED -> "External storage is mounted but for some unknown reason is not" +
                        " available."
                else -> "External storage is not available. No reason."
            }
        }
    }
}