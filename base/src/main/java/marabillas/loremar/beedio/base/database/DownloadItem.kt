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

package marabillas.loremar.beedio.base.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadItem(
        @PrimaryKey
        var uid: Int,
        @ColumnInfo(name = "name")
        val name: String,
        @ColumnInfo(name = "video_url")
        val videoUrl: String,
        @ColumnInfo(name = "ext")
        val ext: String,
        @ColumnInfo(name = "size")
        val size: Long,
        @ColumnInfo(name = "source_webpage")
        val sourceWebpage: String,
        @ColumnInfo(name = "source_website")
        val sourceWebsite: String,
        @ColumnInfo(name = "is_chunked")
        val isChunked: Boolean = false,
        @ColumnInfo(name = "audio_url")
        val audioUrl: String? = null,
        @ColumnInfo(name = "is_audio_chunked")
        val isAudioChunked: Boolean = false
)