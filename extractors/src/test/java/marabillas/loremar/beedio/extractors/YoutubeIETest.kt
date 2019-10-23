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

package marabillas.loremar.beedio.extractors

import com.google.common.truth.Truth.assertThat
import marabillas.loremar.beedio.extractors.ExtractorUtils.isAcodec
import marabillas.loremar.beedio.extractors.ExtractorUtils.isVcodec
import marabillas.loremar.beedio.extractors.extractors.youtube.YoutubeFormat
import marabillas.loremar.beedio.extractors.extractors.youtube.YoutubeIE
import marabillas.loremar.beedio.extractors.extractors.youtube.YoutubeVideoInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert
import org.junit.Test

class YoutubeIETest {
    private val youtube = YoutubeIE()
    private val okHttp = OkHttpClient()
    @Test
    fun test1() {
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=BaW_jenozKc&t=1s&end=9")
        assertYoutubeDLTestVideo(info)
        assertThat(info.startTime).isEqualTo(1)
        assertThat(info.endTime).isEqualTo(9)
        assertFormatsExist(info.formats)
    }

    private fun assertYoutubeDLTestVideo(info: YoutubeVideoInfo) {
        assertThat(info.id).isEqualTo("BaW_jenozKc")
        assertThat(info.title).isEqualTo("youtube-dl test video \\\"'/\\\\ä↭\uD835\uDD50")
        assertThat(info.uploader).isEqualTo("Philipp Hagemeister")
        assertThat(info.uploaderId).isEqualTo("phihag")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/phihag")
        assertThat(info.channelId).isEqualTo("UCLqxVugv74EIW3VWh2NOa3Q")
        assertThat(info.channelUrl).isEqualTo("http://www.youtube.com/channel/UCLqxVugv74EIW3VWh2NOa3Q")
        assertThat(info.uploadDate).isEqualTo("20121002")
        assertThat(info.description).isEqualTo("test chars: \"'/\\ä↭\uD835\uDD50 test URL:" +
                " https://github.com/rg3/youtube-dl/issues/1892 This is a test video for youtube-dl." +
                " For more information, contact phihag@phihag....")
        assertThat(info.categories?.get(0)).isEqualTo("Science & Technology")
        assertThat(info.tags?.get(0)).isEqualTo("youtube-dl")
        assertThat(info.duration).isEqualTo(10f)
        assertThat(info.viewCount).isAtLeast(21863)
        assertThat(info.likeCount).isAtLeast(119)
        assertThat(info.dislikeCount).isAtLeast(9)
    }

    @Test
    fun test2() {
        // Test generic use_cipher_signature video (#897)
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=UxxajLWwzqY")
        assertThat(info.id).isEqualTo("UxxajLWwzqY")
        assertThat(info.uploadDate).isEqualTo("20120506")
        assertThat(info.title).isEqualTo("Icona Pop - I Love It (feat. Charli XCX) [OFFICIAL VIDEO]")
        assertThat(info.altTitle).isEqualTo("I Love It (feat. Charli XCX)")
        assertThat(info.description).isEqualTo("New single 'Next Mistake' available now on " +
                "all music platforms: https://ffm.to/nextmistake Hi Icons! Follow us on Instagram! " +
                "\uD83D\uDE80 http://instagram.com/iconapop ...")
        assertThat(info.tags).containsExactly("Icona Pop i love it", "sweden", "pop music",
                "big beat records", "big beat", "charli", "xcx", "charli xcx", "girls", "hbo", "i love it",
                "i don't care", "icona", "pop", "iconic ep", "iconic", "love", "it")
        assertThat(info.duration).isEqualTo(180f)
        assertThat(info.uploader).isEqualTo("Icona Pop")
        assertThat(info.uploaderId).isEqualTo("IconaPop")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/IconaPop")
        assertThat(info.creator).isEqualTo("Icona Pop")
        assertThat(info.track).isEqualTo("I Love It (feat. Charli XCX)")
        assertThat(info.artist).isEqualTo("Icona Pop")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test3() {
        // Use the first video ID in the URL
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=BaW_jenozKc&v=UxxajLWwzqY")
        assertYoutubeDLTestVideo(info)
        assertFormatsExist(info.formats)
    }

    @Test
    fun test4() {
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=a9LDPn-MO4I")
        assertThat(info.id).isEqualTo("a9LDPn-MO4I")
        assertThat(info.uploadDate).isEqualTo("20121002")
        assertThat(info.uploaderId).isEqualTo("8KVIDEO")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/8KVIDEO")
        assertThat(info.description).isEqualTo("")
        assertThat(info.uploader).isEqualTo("8KVIDEO")
        assertThat(info.title).isEqualTo("UHDTV TEST 8K VIDEO.mp4")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test5() {
        // DASH manifest with encrypted signature
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=IB3lcPjvWLA")
        assertThat(info.id).isEqualTo("IB3lcPjvWLA")
        assertThat(info.title).isEqualTo("Afrojack, Spree Wilson - The Spark (Official Music Video) ft. Spree Wilson")
        assertThat(info.altTitle).isEqualTo("The Spark")
        assertThat(info.description).isEqualTo("‘The Spark' is the brand new video from Afrojack " +
                "featuring Spree Wilson. Available on iTunes http://smarturl.it/TheSpark_iTunes and Spotify " +
                "http://smarturl.i...")
        assertThat(info.duration).isEqualTo(244f)
        assertThat(info.uploader).isEqualTo("AfrojackVEVO")
        assertThat(info.uploaderId).isEqualTo("AfrojackVEVO")
        assertThat(info.uploadDate).isEqualTo("20131011")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test6() {
        // JS player signature function name containing $
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=nfWlot6h_JM")
        assertThat(info.id).isEqualTo("nfWlot6h_JM")
        assertThat(info.title).isEqualTo("Taylor Swift - Shake It Off")
        assertThat(info.description).isEqualTo("Music video by Taylor Swift performing Shake" +
                " It Off. (C) 2014 Big Machine Records, LLC. New single ME! (feat. Brendon Urie of" +
                " Panic! At The Disco) available ...")
        assertThat(info.duration).isEqualTo(242f)
        assertThat(info.uploader).isEqualTo("TaylorSwiftVEVO")
        assertThat(info.uploaderId).isEqualTo("TaylorSwiftVEVO")
        assertThat(info.uploadDate).isEqualTo("20140818")
        assertThat(info.creator).isEqualTo("Taylor Swift")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test7() {
        // Controversy video
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=T4XJQO3qol8")
        assertThat(info.id).isEqualTo("T4XJQO3qol8")
        assertThat(info.duration).isEqualTo(219f)
        assertThat(info.uploadDate).isEqualTo("20100909")
        assertThat(info.uploader).isEqualTo("Amazing Atheist")
        assertThat(info.uploaderId).isEqualTo("TheAmazingAtheist")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/TheAmazingAtheist")
        assertThat(info.title).isEqualTo("Burning Everyone's Koran")
        assertThat(info.description).isEqualTo("SUBSCRIBE: http://www.youtube.com/saturninefilms " +
                "Even Obama has taken a stand against freedom on this issue: http://www.huffingtonpost.com/2010/09/09/obama-g...")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test8() {
        /*# video_info is None (https://github.com/ytdl-org/youtube-dl/issues/4421)
        # YouTube Red ad is not captured for creator*/
        val info = youtube.extractVideoInfo("https://youtube.com/watch?v=__2ABJjxzNo")
        assertThat(info.id).isEqualTo("__2ABJjxzNo")
        assertThat(info.duration).isEqualTo(266f)
        assertThat(info.uploadDate).isEqualTo("20100430")
        assertThat(info.uploaderId).isEqualTo("deadmau5")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/deadmau5")
        //assertThat(info.creator).isEqualTo("deadmau5")
        assertThat(info.description).isEqualTo("deadmau5 \"album title goes here\": Download" +
                " at iTunes: http://smarturl.it/albumtitlegoeshere Buy a physical copy: " +
                "http://smarturl.it/atgh-physical Available t...")
        assertThat(info.uploader).isEqualTo("deadmau5")
        assertThat(info.title).isEqualTo("Deadmau5 - Some Chords (HD)")
        //assertThat(info.altTitle).isEqualTo("Some Chords")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test9() {
        // Olympics (https://github.com/ytdl-org/youtube-dl/issues/4431)
        val info = youtube.extractVideoInfo("https://youtube.com/watch?v=lqQg6PlCWgI")
        assertThat(info.id).isEqualTo("lqQg6PlCWgI")
        assertThat(info.duration).isEqualTo(6085f)
        assertThat(info.uploadDate).isEqualTo("20150827")
        assertThat(info.uploaderId).isEqualTo("olympic")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/olympic")
        assertThat(info.description).isEqualTo("HO09 - Women - GER-AUS - Hockey - 31 July " +
                "2012 - London 2012 Olympic Games")
        assertThat(info.uploader).isEqualTo("Olympic")
        assertThat(info.title).isEqualTo("Hockey - Women -  GER-AUS - London 2012 Olympic Games")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test10() {
        // Non-square pixels
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=_b-2C3KPAM0")
        assertThat(info.id).isEqualTo("_b-2C3KPAM0")
        assertThat(info.duration).isEqualTo(85)
        assertThat(info.uploadDate).isEqualTo("20110310")
        assertThat(info.uploaderId).isEqualTo("AllenMeow")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/AllenMeow")
        assertThat(info.description).isEqualTo("made by Wacom from Korea | 字幕&加油添醋 by " +
                "TY's Allen | 感謝heylisa00cavey1001同學熱情提供梗及翻譯")
        assertThat(info.uploader).isEqualTo("孫ᄋᄅ")
        assertThat(info.title).isEqualTo("[A-made] 變態妍字幕版 太妍 我就是這樣的人")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test11() {
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=FIl7x6_3R5Y")
        assertThat(info.id).isEqualTo("FIl7x6_3R5Y")
        assertThat(info.title).isEqualTo("[60fps] 150614  마마무 솔라 'Mr. 애매모호' 라이브 직캠 @대학로 게릴라 콘서트")
        assertThat(info.description).isEqualTo("Google Chrome 480p60 / 20150614 MAMAMOO," +
                " Solar \"Mr.Ambiguous\" LIVE! @Daehakro Guerilla Concert, Hyehwa Station / Fancam by 도라삐 /")
        assertThat(info.duration).isEqualTo(220f)
        assertThat(info.uploadDate).isEqualTo("20150625")
        assertThat(info.uploaderId).isEqualTo("dorappi2000")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/dorappi2000")
        assertThat(info.uploader).isEqualTo("dorappi2000")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test12() {
        val info = youtube.extractVideoInfo("https://vid.plus/FlRa-iH7PGw")
        assertThat(info.id).isEqualTo("FlRa-iH7PGw")
        assertThat(info.title).isEqualTo("!!Con 2015 - Kevin Lynagh: I made a cell phone! (DON'T TELL THE FCC KTHX!)")
        assertThat(info.uploader).isEqualTo("!!Con")
        assertThat(info.uploaderId).isEqualTo("UC2kxl-dcUYQQvTCuQtfuChQ")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/channel/UC2kxl-dcUYQQvTCuQtfuChQ")
        assertThat(info.uploadDate).isEqualTo("20150815")
        assertThat(info.description).isEqualTo("Presented at !!Con 2015: http://bangbangcon.com " +
                "!!Con 2015 - Kevin Lynagh: I made a cell phone! (DON'T TELL THE FCC KTHX!)")
        assertThat(info.duration).isEqualTo(724f)
        assertFormatsExist(info.formats)
    }

    @Test
    fun test13() {
        val info = youtube.extractVideoInfo("https://zwearz.com/watch/9lWxNJF-ufM/electra-woman-dyna-girl-official-trailer-grace-helbig.html")
        assertThat(info.id).isEqualTo("9lWxNJF-ufM")
        assertThat(info.title).isEqualTo("ELECTRA WOMAN & DYNA GIRL OFFICIAL TRAILER // Grace Helbig")
        assertThat(info.altTitle).isEqualTo("Take Me Away-Extreme Music")
        assertThat(info.uploader).isEqualTo("Grace Helbig")
        assertThat(info.uploaderId).isEqualTo("graciehinabox")
        assertThat(info.uploadDate).isEqualTo("20160329")
        assertThat(info.description).isEqualTo("The full length feature version of Electra" +
                " Woman & Dyna Girl is coming June 7th 2016! Go to http://www.electrawomandynagirl.com" +
                " for all the important info! G...")
        assertThat(info.duration).isEqualTo(249f)
        assertThat(info.creator).isEqualTo("Wendy Page / Dolph Taylor / James Fenton Marr")
        assertThat(info.track).isEqualTo("Take Me Away-Extreme Music")
        assertThat(info.artist).isEqualTo("Wendy Page / Dolph Taylor / James Fenton Marr")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test14() {
        /*# Title with JS-like syntax "};" (see https://github.com/ytdl-org/youtube-dl/issues/7468)
            # Also tests cut-off URL expansion in video description (see
            # https://github.com/ytdl-org/youtube-dl/issues/1892,
            # https://github.com/ytdl-org/youtube-dl/issues/8164)*/
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=lsguqyKfVQg")
        assertThat(info.id).isEqualTo("lsguqyKfVQg")
        assertThat(info.title).isEqualTo("{dark walk}; Loki/AC/Dishonored; collab w/Elflover21")
        assertThat(info.altTitle).isEqualTo("Dark Walk - Position Music")
        assertThat(info.description).isEqualTo("ugh, as much as I like the song, try and " +
                "skip my part, will ya? xD Just get to the half of the vid and you`ll definitely" +
                " love my epic friend's part!! https:/...")
        assertThat(info.duration).isEqualTo(133f)
        assertThat(info.uploadDate).isEqualTo("20151119")
        assertThat(info.uploaderId).isEqualTo("IronSoulElf")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/IronSoulElf")
        assertThat(info.uploader).isEqualTo("IronSoulElf")
        assertThat(info.creator).isEqualTo("Todd Haberman,  Daniel Law Heath and Aaron Kaplan")
        assertThat(info.track).isEqualTo("Dark Walk - Position Music")
        assertThat(info.artist).isEqualTo("Todd Haberman,  Daniel Law Heath and Aaron Kaplan")
        assertThat(info.album).isEqualTo("Position Music - Production Music Vol. 143 - Dark Walk")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test15() {
        // Video licensed under Creative Commons
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=M4gD1WSo5mA")
        assertThat(info.id).isEqualTo("M4gD1WSo5mA")
        assertThat(info.title).isEqualTo("William Fisher, CopyrightX: Lecture 3.2, The" +
                " Subject Matter of Copyright: Drama and choreography")
        assertThat(info.description).isEqualTo("The terms on which this lecture may be used" +
                " or modified are available at http://copyx.org/permission. The lecture was prepared " +
                "for a Harvard Law School cours...")
        assertThat(info.duration).isEqualTo(721)
        assertThat(info.uploadDate).isEqualTo("20150127")
        assertThat(info.uploaderId).isEqualTo("BerkmanCenter")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/BerkmanCenter")
        assertThat(info.uploader).isEqualTo("The Berkman Klein Center for Internet & Society")
        assertThat(info.license).isEqualTo("Creative Commons Attribution license (reuse allowed)")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test16() {
        // Channel-like uploader_url
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=eQcmzGIKrzg")
        assertThat(info.id).isEqualTo("eQcmzGIKrzg")
        assertThat(info.title).isEqualTo("Democratic Socialism and Foreign Policy | Bernie Sanders")
        assertThat(info.description).isEqualTo("Bernie Sanders delivers his long-awaited " +
                "speech on Democratic Socialism at Georgetown University. He also speaks about his " +
                "vision for bringing American forei...")
        assertThat(info.duration).isEqualTo(4060f)
        assertThat(info.uploadDate).isEqualTo("20151119")
        assertThat(info.uploader).isEqualTo("Bernie Sanders")
        assertThat(info.uploaderId).isEqualTo("UCH1dpzjCEiGAt8CXkryhkZg")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/channel/UCH1dpzjCEiGAt8CXkryhkZg")
        assertThat(info.license).isEqualTo("Creative Commons Attribution license (reuse allowed)")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test17() {
        // YouTube Red paid video (https://github.com/ytdl-org/youtube-dl/issues/10059)
        try {
            val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=i1Ko8UG-Tdo")
        } catch (e: ExtractorException) {
            assertThat(e.message).isEqualTo("\"This video requires payment to watch.\"")
        }
    }

    @Test
    fun test18() {
        // YouTube Red video with episode data
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=iqKdEhx-dD4")
        assertThat(info.id).isEqualTo("iqKdEhx-dD4")
        assertThat(info.title).isEqualTo("Isolation - Mind Field (Ep 1)")
        assertThat(info.description).isEqualTo("What happens when your brain is deprived" +
                " of stimulation? What effect does being cut off from interaction with the outside" +
                " world have on a person? What effect...")
        assertThat(info.duration).isEqualTo(2085f)
        assertThat(info.uploadDate).isEqualTo("20170118")
        assertThat(info.uploader).isEqualTo("Vsauce")
        assertThat(info.uploaderId).isEqualTo("Vsauce")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/user/Vsauce")
        assertThat(info.series).isEqualTo("Mind Field")
        assertThat(info.seasonNumber).isEqualTo(1)
        assertThat(info.episodeNumber).isEqualTo(1)
        assertFormatsExist(info.formats)
    }

    @Test
    fun test19() {
        /*# The following content has been identified by the YouTube community
            # as inappropriate or offensive to some audiences.*/
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=6SJNVb0GnPI")
        assertThat(info.id).isEqualTo("6SJNVb0GnPI")
        assertThat(info.title).isEqualTo("Race Differences in Intelligence")
        assertThat(info.description).isEqualTo("Jared Taylor, editor of American Renaissance, " +
                "discusses the evidence. References: John Baker, Race (London: Oxford University " +
                "Press, 1974), pp. 360-400. \"The...")
        assertThat(info.duration).isEqualTo(965)
        assertThat(info.uploadDate).isEqualTo("20140124")
        assertThat(info.uploader).isEqualTo("New Century Foundation")
        assertThat(info.uploaderId).isEqualTo("UCEJYpZGqgUob0zVVEaLhvVg")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/channel/UCEJYpZGqgUob0zVVEaLhvVg")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test20() {
        val info = youtube.extractVideoInfo("https://invidio.us/watch?v=BaW_jenozKc")
        assertYoutubeDLTestVideo(info)
        assertFormatsExist(info.formats)
    }

    @Test
    fun test21() {
        // Video with unsupported adaptive stream type formats
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=Z4Vy8R84T1U")
        assertThat(info.id).isEqualTo("Z4Vy8R84T1U")
        assertThat(info.title).isEqualTo("saman SMAN 53 Jakarta(Sancety) opening COFFEE4th at SMAN 53 Jakarta")
        assertThat(info.description).isEqualTo("")
        assertThat(info.duration).isEqualTo(433f)
        assertThat(info.uploadDate).isEqualTo("20130923")
        assertThat(info.uploader).isEqualTo("Amelia Putri Harwita")
        assertThat(info.uploaderId).isEqualTo("UCpOxM49HJxmC1qCalXyB3_Q")
        assertThat(info.uploaderUrl).isEqualTo("http://www.youtube.com/channel/UCpOxM49HJxmC1qCalXyB3_Q")
        assertFormatsExist(info.formats)
    }

    @Test
    fun test22() {
        // Youtube Music Auto-generated description
        val info = youtube.extractVideoInfo("https://music.youtube.com/watch?v=MgNrAu2pzNs")
        assertThat(info.id).isEqualTo("MgNrAu2pzNs")
        assertThat(info.title).isEqualTo("Voyeur Girl")
        assertThat(info.description).isEqualTo("Provided to YouTube by EDM (District) " +
                "Voyeur Girl · Stephen it's too much love to know my dear ℗ Stephen Released on: " +
                "2019-03-13 Auto-generated by YouTube.")
        assertThat(info.uploadDate).isEqualTo("20190312")
        assertThat(info.uploader).isEqualTo("Stephen - Topic")
        assertThat(info.uploaderId).isEqualTo("UC-pWHpBjdGG69N9mM2auIAA")
        assertThat(info.artist).isEqualTo("Stephen")
        assertThat(info.track).isEqualTo("Voyeur Girl")
        assertThat(info.album).isEqualTo("it's too much love to know my dear")
        assertThat(info.releaseDate).isEqualTo("20190313")
        assertThat(info.releaseYear).isEqualTo(2019)
        assertFormatsExist(info.formats)
    }

    @Test
    fun test23() {
        /*# Youtube Music Auto-generated description
            # Retrieve 'artist' field from 'Artist:' in video description
            # when it is present on youtube music video*/
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=k0jLE7tTwjY")
        assertThat(info.id).isEqualTo("k0jLE7tTwjY")
        assertThat(info.title).isEqualTo("Latch Feat. Sam Smith")
        assertThat(info.description).isEqualTo("Provided to YouTube by Republic of Music" +
                " Latch Feat. Sam Smith · Disclosure featuring Sam Smith Latch Featuring Sam Smith" +
                " ℗ PMR Records Released on: 2012-10-...")
        assertThat(info.uploadDate).isEqualTo("20150110")
        assertThat(info.uploader).isEqualTo("Various Artists - Topic")
        assertThat(info.uploaderId).isEqualTo("UCNkEcmYdjrH4RqtNgh7BZ9w")
        assertThat(info.artist).isEqualTo("Disclosure")
        assertThat(info.track).isEqualTo("Latch Feat. Sam Smith")
        assertThat(info.album).isEqualTo("Latch Featuring Sam Smith")
        assertThat(info.releaseDate).isEqualTo("20121008")
        assertThat(info.releaseYear).isEqualTo(2012)
        assertFormatsExist(info.formats)
    }

    @Test
    fun test24() {
        /* # Youtube Music Auto-generated description
            # handle multiple artists on youtube music video*/
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=74qn0eJSjpA")
        assertThat(info.id).isEqualTo("74qn0eJSjpA")
        assertThat(info.title).isEqualTo("Eastside")
        assertThat(info.description).isEqualTo("Provided to YouTube by Universal Music" +
                " Group Eastside · benny blanco · Halsey · Khalid Eastside ℗ 2018 Friends Keep " +
                "Secrets/Interscope Records Released on: 2...")
        assertThat(info.uploadDate).isEqualTo("20180710")
        assertThat(info.uploader).isEqualTo("Benny Blanco - Topic")
        assertThat(info.uploaderId).isEqualTo("UCzqz_ksRu_WkIzmivMdIS7A")
        assertThat(info.artist).isEqualTo("benny blanco · Halsey · Khalid")
        assertThat(info.track).isEqualTo("Eastside")
        assertThat(info.album).isEqualTo("Eastside")
        assertThat(info.releaseDate).isEqualTo("20180713")
        assertThat(info.releaseYear).isEqualTo(2018)
        assertFormatsExist(info.formats)
    }

    @Test
    fun test25() {
        /*# Youtube Music Auto-generated description
            # handle youtube music video with release_year and no release_date*/
        val info = youtube.extractVideoInfo("https://www.youtube.com/watch?v=-hcAI0g-f5M")
        assertThat(info.id).isEqualTo("-hcAI0g-f5M")
        assertThat(info.title).isEqualTo("Put It On Me")
        assertThat(info.description).isEqualTo("Provided to YouTube by Neon Gold/Atlantic" +
                " Put It On Me · Matt Maeson The Hearse ℗ 2018 Atlantic Recording Corporation" +
                " Vocals: Matt Maeson Writer: Alex Hope W...")
        assertThat(info.uploadDate).isEqualTo("20180426")
        assertThat(info.uploader).isEqualTo("Matt Maeson - Topic")
        assertThat(info.uploaderId).isEqualTo("UCnEkIGqtGcQMLk73Kp-Q5LQ")
        assertThat(info.artist).isEqualTo("Matt Maeson")
        assertThat(info.track).isEqualTo("Put It On Me")
        assertThat(info.album).isEqualTo("The Hearse")
        assertThat(info.releaseDate).isNull()
        assertThat(info.releaseYear).isEqualTo(2018)
        assertFormatsExist(info.formats)
    }

    private fun assertFormatsExist(formats: List<YoutubeFormat>?) {
        if (formats.isNullOrEmpty()) Assert.fail("No existing formats")
        val validFormats = formats?.filter {
            it.run {
                if (id.isNullOrBlank())
                    invalidFormat("missing format id")
                else if (url.isNullOrBlank())
                    invalidFormat("missing url")
                else if (vcodec.isNullOrBlank() && acodec.isNullOrBlank())
                    invalidFormat("missing both vcodec and acodec")
                else if (vcodec != null && !youtube.formatContains(id.toString(), vcodec as Any)) {
                    if (isVcodec(vcodec))
                        true
                    else
                        invalidFormat("invalid vcodec $vcodec")
                } else if (acodec != null && !youtube.formatContains(id.toString(), acodec as Any)) {
                    if (isAcodec(acodec))
                        true
                    else invalidFormat("invalid acodec $acodec")
                }
                else {

                    url?.let { url ->
                        Request.Builder()
                                .url(url)
                                .method("GET", null)
                                .build()
                    }
                            ?.let { request ->
                                val response = okHttp.newCall(request).execute()
                                val contentType = response.header("Content-Type")
                                if (contentType == null) {
                                    response.close()
                                    invalidFormat("can't retrieve Content-Type")
                                } else if (
                                        !contentType.contains("video", true)
                                        && !contentType.contains("audio", true)
                                        && !contentType.contains("mp4", true)
                                        && !contentType.contains("mpeg", true)
                                ) {
                                    val code = response.code
                                    response.close()
                                    if (code != 200)
                                        invalidFormat("url returns ${code}")
                                    else
                                        invalidFormat("url does not point to a valid video or audio file")
                                } else {
                                    response.close()
                                    true
                                }
                            }
                            ?: invalidFormat("can't connect to url")
                }
            }
        }
        if (validFormats.isNullOrEmpty())
            Assert.fail("No existing valid formats")
        else
            println("${validFormats.count()} valid formats extracted")
    }

    private fun invalidFormat(msg: String): Boolean {
        println("Invalid format: $msg")
        return false
    }
}