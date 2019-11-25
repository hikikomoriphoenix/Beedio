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

package marabillas.loremar.beedio.base

import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import marabillas.loremar.beedio.base.media.VideoAudioMuxer
import marabillas.loremar.beedio.base.media.VideoAudioMuxingListener
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch

class VideoAudioMuxerTest {
    @Test
    fun testMux() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val muxer = VideoAudioMuxer(context)
        val videoFile = File(Environment.getExternalStorageDirectory(), "youtube-dl test.mp4")
        val audioFile = File(Environment.getExternalStorageDirectory(), "youtube-dl test.m4a")
        val targetFile = File(Environment.getExternalStorageDirectory(), "youtubemuxsample")
        val muxWaiter = CountDownLatch(1)
        muxer.mux(videoFile.absolutePath, audioFile.absolutePath, targetFile.path,
                object : VideoAudioMuxingListener {
                    override fun ffmpegNotAvailable() {
                        println("FFmpeg not available")
                    }

                    override fun onStart() {
                        println("Muxing start")
                    }

                    override fun onFinish() {
                        println("Muxing finished")
                        muxWaiter.countDown()
                    }

                    override fun onSuccess(message: String) {
                        println("Muxing success: $message")
                        muxWaiter.countDown()
                    }

                    override fun onFailure(message: String) {
                        println("Muxing failed: $message")
                        muxWaiter.countDown()
                    }

                    override fun onProgress(message: String) {
                        println("Muxing progress: $message")
                    }
                })
        muxWaiter.await()
    }
}