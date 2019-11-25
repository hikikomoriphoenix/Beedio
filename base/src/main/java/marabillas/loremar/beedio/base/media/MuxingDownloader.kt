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

package marabillas.loremar.beedio.base.media

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC
import android.media.MediaFormat.KEY_MAX_INPUT_SIZE
import android.media.MediaMuxer
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

class MuxingDownloader(private val context: Context) {
    private val ffmpeg = FFmpeg.getInstance(context)
    private val loadBinaryWaiter = CountDownLatch(1)
    private var loadFinished = false
    private var loadFailed = false
    private val okhttp = OkHttpClient()

    init {
        try {
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFinish() {
                    loadFinished = true
                    loadBinaryWaiter.countDown()
                }

                override fun onSuccess() {
                    loadFailed = false
                    loadFinished = true
                    loadBinaryWaiter.countDown()
                }

                override fun onFailure() {
                    loadFailed = true
                    loadFinished = true
                    loadBinaryWaiter.countDown()
                }
            })
        } catch (e: FFmpegNotSupportedException) {
        }
    }

    fun download(videoUrl: String, audioUrl: String, targetPath: String,
                 muxingDownloadListener: MuxingDownloadListener) {


        if (!loadFinished)
            loadBinaryWaiter.await()

        if (loadFailed) {
            muxingDownloadListener.ffmpegNotAvailable()
            return
        }

        ffmpeg.execute(arrayOf("-i", videoUrl, "-i", audioUrl, "$targetPath.mp4"), object : ExecuteBinaryResponseHandler() {
            override fun onStart() = muxingDownloadListener.onStart()

            override fun onFinish() = muxingDownloadListener.onFinish()

            override fun onSuccess(message: String?) = message?.let { muxingDownloadListener.onSuccess(it) }
                    ?: muxingDownloadListener.onSuccess("Download Success")

            override fun onFailure(message: String?) = message?.let { muxingDownloadListener.onFailure(it) }
                    ?: muxingDownloadListener.onFailure("Download Failed")

            override fun onProgress(message: String?) {
                message?.let { muxingDownloadListener.onProgress(it) }
            }
        })
    }

    private fun downloadVideo(videoUrl: String, targetPath: String) {
        val request = Request.Builder().url(videoUrl).method("GET", null).build()
        val response = okhttp.newCall(request).execute()
        val stream = response.body?.byteStream()
        val fileOut = FileOutputStream(File(context.externalCacheDir, targetPath))
        val bytes = stream?.readBytes()
        fileOut.write(bytes)
    }

    @Deprecated("This function does not work")
    fun download(videoUrl: String, audioUrl: String, targetPath: String, outputFormat: Int) {
        val muxer = MediaMuxer(targetPath, outputFormat)


        val audioTrackIndex: Int
        val audioBuffer: ByteBuffer
        val audioExtractor = MediaExtractor().apply {
            setDataSource(audioUrl)
            selectTrack(0)
            val format = getTrackFormat(0)
            audioTrackIndex = muxer.addTrack(format)
            val bufferSize = format.getInteger(KEY_MAX_INPUT_SIZE)
            audioBuffer = ByteBuffer.allocate(bufferSize)
            seekTo(0, SEEK_TO_CLOSEST_SYNC)
        }

        val videoTrackIndex: Int
        val videoBuffer: ByteBuffer
        val videoExtractor = MediaExtractor().apply {
            setDataSource(videoUrl)
            selectTrack(0)
            val format = getTrackFormat(0)
            videoTrackIndex = muxer.addTrack(format)
            val bufferSize = format.getInteger(KEY_MAX_INPUT_SIZE)
            videoBuffer = ByteBuffer.allocate(bufferSize)
            seekTo(0, SEEK_TO_CLOSEST_SYNC)
        }

        val audioBufferInfo = MediaCodec.BufferInfo()
        val videoBufferInfo = MediaCodec.BufferInfo()
        var lastAudioTimestamp = 0L
        var lastVideoTimestamp = 0L
        var endOfAudio = false
        var endOfVideo = false


        muxer.start()
        while (true) {
            audioExtractor.run {
                audioBufferInfo.run {
                    if (sampleTrackIndex == -1) endOfAudio = true

                    if (!endOfAudio) {
                        size = readSampleData(audioBuffer, 0)

                        if (size >= 0) {
                            flags = sampleFlags
                            presentationTimeUs = sampleTime
                            offset = 0

                            if (presentationTimeUs > lastAudioTimestamp) {
                                muxer.writeSampleData(audioTrackIndex, audioBuffer, this)
                                lastAudioTimestamp = presentationTimeUs
                            }

                            endOfAudio = !advance()
                            audioBuffer.rewind()
                        } else {
                            endOfAudio = true
                        }
                    }
                }
            }


            videoExtractor.run {
                videoBufferInfo.run {
                    if (sampleTrackIndex == -1) endOfVideo = true

                    if (!endOfVideo) {
                        size = readSampleData(videoBuffer, 0)

                        if (size >= 0) {
                            flags = sampleFlags
                            presentationTimeUs = sampleTime
                            offset = 0

                            if (presentationTimeUs > lastVideoTimestamp) {
                                muxer.writeSampleData(videoTrackIndex, videoBuffer, this)
                                lastVideoTimestamp = presentationTimeUs
                            }

                            endOfVideo = !advance()
                            videoBuffer.rewind()
                        } else {
                            endOfVideo = true
                        }
                    }
                }
            }

            if (endOfAudio && endOfVideo) break
        }

        muxer.stop()
        muxer.release()
        audioExtractor.release()
        videoExtractor.release()
    }
}

interface MuxingDownloadListener {
    fun ffmpegNotAvailable()

    fun onStart()

    fun onFinish()

    fun onSuccess(message: String)

    fun onFailure(message: String)

    fun onProgress(message: String)
}