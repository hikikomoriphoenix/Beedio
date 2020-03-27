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
import wseemann.media.FFmpegMediaMetadataRetriever
import wseemann.media.FFmpegMediaMetadataRetriever.*
import java.text.DecimalFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow


class VideoDetailsFetcher {
    private val metadataRetriever = FFmpegMediaMetadataRetriever()
    private var isCancelled = false

    fun fetchDetails(url: String, fetchListener: FetchListener) {
        isCancelled = false
        try {
            metadataRetriever.setDataSource(url)
            val details = VideoDetails(
                    filename = extract(METADATA_KEY_FILENAME),
                    title = extract(METADATA_KEY_TITLE),
                    vcodec = extract(METADATA_KEY_VIDEO_CODEC),
                    acodec = extract(METADATA_KEY_AUDIO_CODEC),
                    duration = extract(METADATA_KEY_DURATION)?.formatDuration(),
                    filesize = extract(METADATA_KEY_FILESIZE)?.formatFilesize(),
                    width = extract(METADATA_KEY_VIDEO_WIDTH),
                    height = extract(METADATA_KEY_VIDEO_HEIGHT),
                    bitrate = extract(METADATA_KEY_VARIANT_BITRATE),
                    framerate = extract(METADATA_KEY_FRAMERATE),
                    encoder = extract(METADATA_KEY_ENCODER),
                    encodedBy = extract(METADATA_KEY_ENCODED_BY),
                    date = extract(METADATA_KEY_DATE),
                    creationTime = extract(METADATA_KEY_CREATION_TIME),
                    artist = extract(METADATA_KEY_ARTIST),
                    album = extract(METADATA_KEY_ALBUM),
                    albumArtist = extract(METADATA_KEY_ALBUM_ARTIST),
                    track = extract(METADATA_KEY_TRACK),
                    genre = extract(METADATA_KEY_GENRE),
                    composer = extract(METADATA_KEY_COMPOSER),
                    performer = extract(METADATA_KEY_PERFORMER),
                    copyright = extract(METADATA_KEY_COPYRIGHT),
                    publisher = extract(METADATA_KEY_PUBLISHER),
                    language = extract(METADATA_KEY_LANGUAGE)
            )
            if (details.vcodec != null)
                details.thumbnail = metadataRetriever.frameAtTime
            fetchListener.onFetched(details)
        } catch (e: CancelledException) {
            fetchListener.onUnFetched(e)
        } catch (e: IllegalArgumentException) {
            fetchListener.onUnFetched(e)
        } catch (e: Exception) {
            fetchListener.onUnFetched(e)
        }
    }

    fun fetchMiniDetails(url: String, fetchListener: FetchListener) {
        isCancelled = false
        try {
            metadataRetriever.setDataSource(url)
            val details = VideoDetails(
                    filename = extract(METADATA_KEY_FILENAME),
                    title = extract(METADATA_KEY_TITLE),
                    vcodec = extract(METADATA_KEY_VIDEO_CODEC),
                    duration = extract(METADATA_KEY_DURATION)?.formatDurationShort()
            )
            if (details.vcodec != null)
                details.thumbnail = metadataRetriever.frameAtTime
            fetchListener.onFetched(details)
        } catch (e: CancelledException) {
            fetchListener.onUnFetched(e)
        } catch (e: IllegalArgumentException) {
            fetchListener.onUnFetched(e)
        } catch (e: Exception) {
            fetchListener.onUnFetched(e)
        }
    }

    private fun extract(key: String) = if (!isCancelled) metadataRetriever.extractMetadata(key)
    else throw CancelledException()

    fun cancel() {
        isCancelled = true
    }

    private fun String.formatDuration(): String? {
        return try {
            val totalSecs = toLong() / 1000
            val mils = totalSecs % 1000
            val s = totalSecs % 60
            val m = totalSecs / 60 % 60
            val h = totalSecs / (60 * 60) % 24
            String.format(Locale.US, "%02d:%02d:%02d.%d", h, m, s, mils)
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun String.formatDurationShort(): String? {
        return try {
            val totalSecs = toLong() / 1000
            val s = totalSecs % 60
            val m = totalSecs / 60 % 60
            val h = totalSecs / (60 * 60) % 24
            when {
                h > 0 -> String.format(Locale.US, "%d:%02d:%02d", h, m, s)
                m > 0 -> String.format(Locale.US, "%d:%02d", m, s)
                else -> String.format(Locale.US, "%d seconds", s)
            }
        } catch (e: java.lang.NumberFormatException) {
            null
        }
    }

    private fun String.formatFilesize(): String? {
        return try {
            val size: Long = toLong()
            if (size <= 0) return "0"
            val units = arrayOf("B", "kB", "MB", "GB", "TB")
            val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
            DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))
                    .toString() + " " + units[digitGroups]
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun close() = metadataRetriever.release()

    interface FetchListener {
        fun onUnFetched(error: Throwable)
        fun onFetched(details: VideoDetails)
    }

    inner class CancelledException : Exception()
}

data class VideoDetails(
        val filename: String? = null,
        val title: String? = null,
        val vcodec: String? = null,
        var acodec: String? = null,
        val duration: String? = null,
        var filesize: String? = null,
        val width: String? = null,
        val height: String? = null,
        val bitrate: String? = null,
        val framerate: String? = null,
        val encoder: String? = null,
        val encodedBy: String? = null,
        val date: String? = null,
        val creationTime: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val albumArtist: String? = null,
        val track: String? = null,
        val genre: String? = null,
        val composer: String? = null,
        val performer: String? = null,
        val copyright: String? = null,
        val publisher: String? = null,
        val language: String? = null,
        var thumbnail: Bitmap? = null
)