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
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import java.util.concurrent.CountDownLatch

class VideoAudioMuxer(context: Context) {
    private val ffmpeg = FFmpeg.getInstance(context)
    private val loadBinaryWaiter = CountDownLatch(1)
    private var loadFinished = false
    private var loadFailed = false

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

    fun mux(videoPath: String, audioPath: String, targetPath: String,
            muxingListener: VideoAudioMuxingListener) {

        if (!loadFinished)
            loadBinaryWaiter.await()

        if (loadFailed) {
            muxingListener.ffmpegNotAvailable()
            return
        }

        /*val targetFile = File(targetPath)
        if (!targetFile.exists() && !targetFile.createNewFile()) {
            muxingListener.onFailure("Can't create target file")
            return
        }*/

        ffmpeg.execute(arrayOf("-i", videoPath, "-i", audioPath, "$targetPath.mp4"), object : ExecuteBinaryResponseHandler() {
            override fun onStart() = muxingListener.onStart()

            override fun onFinish() = muxingListener.onFinish()

            override fun onSuccess(message: String?) = message?.let { muxingListener.onSuccess(it) }
                    ?: muxingListener.onSuccess("Muxing into $targetPath Success")

            override fun onFailure(message: String?) = message?.let { muxingListener.onFailure(it) }
                    ?: muxingListener.onFailure("Muxing into $targetPath Failed")

            override fun onProgress(message: String?) {
                message?.let { muxingListener.onProgress(it) }
            }
        })
    }

    fun stopMuxer() = ffmpeg.killRunningProcesses()
}

interface VideoAudioMuxingListener {
    fun ffmpegNotAvailable()

    fun onStart()

    fun onFinish()

    fun onSuccess(message: String)

    fun onFailure(message: String)

    fun onProgress(message: String)
}