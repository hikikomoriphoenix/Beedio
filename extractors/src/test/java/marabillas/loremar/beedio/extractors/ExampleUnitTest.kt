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

import marabillas.loremar.beedio.extractors.extractors.youtube.YoutubeIE
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test() {
        val info = YoutubeIE().extractVideoInfo("https://www.youtube.com/watch?v=lsguqyKfVQg")
        println("id = ${info.id}")
        println("title = ${info.title}")
        println("alt title = ${info.altTitle}")
        println("uploader = ${info.uploader}")
        println("uploader id = ${info.uploaderId}")
        println("uploader url = ${info.uploaderUrl}")
        println("channel id = ${info.channelId}")
        println("channel url = ${info.channelUrl}")
        println("upload date = ${info.uploadDate}")
        println("description = ${info.description}")
        println("categories = ${info.categories}")
        println("tags = ${info.tags}")
        println("duration = ${info.duration}")
        println("view count = ${info.viewCount}")
        println("like count = ${info.likeCount}")
        println("dislike count = ${info.dislikeCount}")
        println("start time = ${info.startTime}")
        println("end time = ${info.endTime}")
        println("creator = ${info.creator}")
        println("track = ${info.track}")
        println("artist = ${info.artist}")
        println("album = ${info.album}")
        println("license = ${info.license}")
        println("formats = ${info.formats}")
    }
}
