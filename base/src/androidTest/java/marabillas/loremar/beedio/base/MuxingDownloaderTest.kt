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

import android.Manifest.permission.INTERNET
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import marabillas.loremar.beedio.base.media.MuxingDownloadListener
import marabillas.loremar.beedio.base.media.MuxingDownloader
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch

class MuxingDownloaderTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE, INTERNET)

    @Test
    fun testDownload() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val downloader = MuxingDownloader(context)
        val file = File(Environment.getExternalStorageDirectory(), "youtubesample")
        val downloadWaiter = CountDownLatch(1)
        downloader.download(
                videoUrl = "https://r3---sn-bavcx-hoak.googlevideo.com/videoplayback?expire=1574807061&ei=tVHdXebmBoKHqAGYuZyADg&ip=110.54.250.166&id=o-ANt-pm6w6Q7Pg1rL9b4l8S6gCRxW3V-RShBZxfAIG7--&itag=136&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C278&source=youtube&requiressl=yes&mime=video%2Fmp4&gir=yes&clen=1673012&dur=9.800&lmt=1387961826998447&fvip=3&keepalive=yes&fexp=23842630&c=WEB&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cmime%2Cgir%2Cclen%2Cdur%2Clmt&sig=ALgxI2wwRgIhAMCZuhznDRILBgUEBnaOpMuzBOca6xQVqA6-3tlnqGIYAiEA35h8vTqvNo0XEOhg20ckf3QjJRm9YA4uX3w9s1DknEo%3D&ratebypass=yes&redirect_counter=1&cm2rm=sn-bavcx-jxcs7s&req_id=2a5e02855fdba3ee&cms_redirect=yes&mm=29&mn=sn-bavcx-hoak&ms=rdu&mt=1574786343&mv=m&mvi=2&pl=24&lsparams=mm,mn,ms,mv,mvi,pl&lsig=AHylml4wRQIgYD3-TifkAxrZx47SU-e9ApSEus73JvCm0wJyefaU1WQCIQCS3NAm9VETBzWJ7Clf6Pb02DMwSxJdC5NYdZ5BzLaPYw==",
                audioUrl = "https://r3---sn-bavcx-hoak.googlevideo.com/videoplayback?expire=1574807061&ei=tVHdXebmBoKHqAGYuZyADg&ip=110.54.250.166&id=o-ANt-pm6w6Q7Pg1rL9b4l8S6gCRxW3V-RShBZxfAIG7--&itag=140&source=youtube&requiressl=yes&mime=audio%2Fmp4&gir=yes&clen=157753&dur=9.891&lmt=1387961817989105&fvip=3&keepalive=yes&fexp=23842630&c=WEB&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cmime%2Cgir%2Cclen%2Cdur%2Clmt&sig=ALgxI2wwRgIhAMEjsDREs3c9ZLC53CYNb1lzo42zwFqquKH-xW9ejAKAAiEAlwtbwa_w0_UKCEYSKkpx_VmLSVlky-AQwAf1GyzrMkc%3D&ratebypass=yes&redirect_counter=1&cm2rm=sn-bavcx-jxcs7s&req_id=d026e6d0204a3ee&cms_redirect=yes&mm=29&mn=sn-bavcx-hoak&ms=rdu&mt=1574786435&mv=m&mvi=2&pl=24&lsparams=mm,mn,ms,mv,mvi,pl&lsig=AHylml4wRQIhAOfzlX5CnTsAUbctoXtMH2pUkGhXmA4NbsJMYbIAtyHmAiBZS_ppUmE6quJr3jxgNNpErACipg7VgyG7K6xgz1OAMg==",
                targetPath = file.absolutePath,
                muxingDownloadListener = object : MuxingDownloadListener {
                    override fun ffmpegNotAvailable() {
                        println("FFmpeg not available")
                    }

                    override fun onStart() {
                        println("Start download task")
                    }

                    override fun onFinish() {
                        println("Finished download task")
                    }

                    override fun onSuccess(message: String) {
                        println("Download success: $message")
                        downloadWaiter.countDown()
                    }

                    override fun onFailure(message: String) {
                        println("Download failed: $message")
                        downloadWaiter.countDown()
                    }

                    override fun onProgress(message: String) {
                        println("Download progress: $message")
                    }
                }
        )
        downloadWaiter.await()
    }

/*    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val downloader = MuxingDownloader(context)
        val file = File(Environment.getExternalStorageDirectory(), "facebooksample.mp4")
        downloader.download(
                videoUrl = "https://video.xx.fbcdn.net/v/t42.9040-2/10000000_1976137875761549_5465691667682033664_n.mp4?_nc_cat=104&efg=eyJ2ZW5jb2RlX3RhZyI6ImRhc2hfdjRfaHEyX2ZyYWdfMl92aWRlbyJ9&_nc_ohc=8TqT73daxz0AQkyNQAI2pS3R1qCMzeLOttWYPXQkdj25VBQQC12U6zc-w&_nc_ht=video.fcrk3-1.fna&oh=8a9e9c2de032a1d994f25715f046fdf8&oe=5DDCFD7D",
                audioUrl = "https://video.xx.fbcdn.net/v/t42.1790-2/32682660_454782554936119_5849343023011332096_n.mp4?_nc_cat=100&efg=eyJ2ZW5jb2RlX3RhZyI6ImRhc2hfdjRfaHExX2ZyYWdfMl9hdWRpbyJ9&_nc_ohc=fYBUpUUkEc0AQl34fdK4nnA7HpzFNi9iNVqs2pudcZWSEPk1WCcJY2MfA&_nc_ht=video.fcrk3-1.fna&oh=da7413c5aadc6d9647597ca344c90bc9&oe=5DDCFCA6",
                targetPath = file.absolutePath,
                outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        )
    }*/
}