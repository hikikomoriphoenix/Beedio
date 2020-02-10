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

import android.graphics.Bitmap
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class VideoDetailsTypeAdapter : TypeAdapter<VideoDetails?>() {
    override fun write(out: JsonWriter?, value: VideoDetails?) {
        value?.apply {
            out?.apply {
                beginObject()
                filename?.let { name(FILENAME).value(it) }
                title?.let { name(TITLE).value(it) }
                vcodec?.let { name(VCODEC).value(it) }
                acodec?.let { name(ACODEC).value(it) }
                duration?.let { name(DURATION).value(it) }
                filesize?.let { name(FILESIZE).value(it) }
                width?.let { name(WIDTH).value(it) }
                height?.let { name(HEIGHT).value(it) }
                bitrate?.let { name(BITRATE).value(it) }
                framerate?.let { name(FRAMERATE).value(it) }
                encoder?.let { name(ENCODER).value(it) }
                encodedBy?.let { name(ENCODED_BY).value(it) }
                date?.let { name(DATE).value(it) }
                creationTime?.let { name(CREATION_TIME).value(it) }
                artist?.let { name(ARTIST).value(it) }
                album?.let { name(ALBUM).value(it) }
                albumArtist?.let { name(ALBUM_ARTIST).value(it) }
                track?.let { name(TRACK).value(it) }
                genre?.let { name(GENRE).value(it) }
                composer?.let { name(COMPOSER).value(it) }
                performer?.let { name(PERFORMER).value(it) }
                copyright?.let { name(COPYRIGHT).value(it) }
                publisher?.let { name(PUBLISHER).value(it) }
                language?.let { name(LANGUAGE).value(it) }
                thumbnail?.let {
                    name(THUMBNAIL).beginArray()
                    val pixelInts = IntArray(it.width * it.height)
                    it.getPixels(pixelInts, 0, it.width, 0, 0, it.width, it.height)
                    val storeInts = IntArray(pixelInts.size + 2)
                    pixelInts.copyInto(storeInts, 0, 0, pixelInts.size)
                    storeInts[storeInts.size - 2] = it.width
                    storeInts[storeInts.size - 1] = it.height
                    for (int in storeInts)
                        value(int)
                    endArray()
                }
                endObject()
            }
        }
    }

    override fun read(`in`: JsonReader?): VideoDetails? {
        return `in`?.run {
            beginObject()
            val entries = Array<String?>(24) { null }
            var thumbnail: Bitmap? = null
            while (hasNext()) {
                when (nextName()) {
                    FILENAME -> entries[0] = nextString()
                    TITLE -> entries[1] = nextString()
                    VCODEC -> entries[2] = nextString()
                    ACODEC -> entries[3] = nextString()
                    DURATION -> entries[4] = nextString()
                    FILESIZE -> entries[5] = nextString()
                    WIDTH -> entries[6] = nextString()
                    HEIGHT -> entries[7] = nextString()
                    BITRATE -> entries[8] = nextString()
                    FRAMERATE -> entries[9] = nextString()
                    ENCODER -> entries[10] = nextString()
                    ENCODED_BY -> entries[11] = nextString()
                    DATE -> entries[12] = nextString()
                    CREATION_TIME -> entries[13] = nextString()
                    ARTIST -> entries[14] = nextString()
                    ALBUM -> entries[15] = nextString()
                    ALBUM_ARTIST -> entries[16] = nextString()
                    TRACK -> entries[17] = nextString()
                    GENRE -> entries[18] = nextString()
                    COMPOSER -> entries[19] = nextString()
                    PERFORMER -> entries[20] = nextString()
                    COPYRIGHT -> entries[21] = nextString()
                    PUBLISHER -> entries[22] = nextString()
                    LANGUAGE -> entries[23] = nextString()
                    THUMBNAIL -> {
                        beginArray()
                        val intList = mutableListOf<Int>()
                        while (hasNext())
                            intList.add(nextInt())
                        endArray()
                        val storeInts = intList.toIntArray()
                        val pixelInts = IntArray(storeInts.size - 2)
                        storeInts.copyInto(pixelInts, 0, 0, storeInts.size - 2)
                        val width = storeInts[storeInts.size - 2]
                        val height = storeInts[storeInts.size - 1]
                        thumbnail = Bitmap.createBitmap(pixelInts, width, height, Bitmap.Config.ARGB_8888)
                    }
                }
            }
            endObject()
            VideoDetails(
                    entries[0],
                    entries[1],
                    entries[2],
                    entries[3],
                    entries[4],
                    entries[5],
                    entries[6],
                    entries[7],
                    entries[8],
                    entries[9],
                    entries[10],
                    entries[11],
                    entries[12],
                    entries[13],
                    entries[14],
                    entries[15],
                    entries[16],
                    entries[17],
                    entries[18],
                    entries[19],
                    entries[20],
                    entries[21],
                    entries[22],
                    entries[23],
                    thumbnail
            )
        }
    }

    companion object {
        const val FILENAME = "filename"
        const val TITLE = "title"
        const val VCODEC = "vcodec"
        const val ACODEC = "acodec"
        const val DURATION = "duration"
        const val FILESIZE = "filesize"
        const val WIDTH = "width"
        const val HEIGHT = "height"
        const val BITRATE = "bitrate"
        const val FRAMERATE = "framerate"
        const val ENCODER = "encoder"
        const val ENCODED_BY = "encodedBy"
        const val DATE = "date"
        const val CREATION_TIME = "creationTime"
        const val ARTIST = "artist"
        const val ALBUM = "album"
        const val ALBUM_ARTIST = "albumArtist"
        const val TRACK = "track"
        const val GENRE = "genre"
        const val COMPOSER = "composer"
        const val PERFORMER = "performer"
        const val COPYRIGHT = "copyright"
        const val PUBLISHER = "publisher"
        const val LANGUAGE = "language"
        const val THUMBNAIL = "thumbnail"
    }
}