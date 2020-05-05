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

package marabillas.loremar.beedio.extractors.extractors.youtube

import marabillas.loremar.beedio.extractors.VideoInfo

data class YoutubeVideoInfo(
        val id: String? = null,
        val uploader: String? = null,
        val uploaderId: String? = null,
        val uploaderUrl: String? = null,
        val channelId: String? = null,
        val channelUrl: String? = null,
        val uploadDate: String? = null,
        val license: String? = null,
        val creator: String? = null,
        val title: String? = null,
        val altTitle: String? = null,
        val thumbnailUrl: String? = null,
        val description: String? = null,
        val categories: List<String>? = null,
        val tags: List<String>? = null,
        val duration: Float? = null,
        val ageLimit: Int? = null,
        // val chapters
        val webpageUrl: String? = null,
        val viewCount: Int? = null,
        val likeCount: Int? = null,
        val dislikeCount: Int? = null,
        val averageRating: Float? = null,
        override val formats: List<YoutubeFormat>? = null,
        val islive: Boolean? = null,
        val startTime: Int? = null,
        val endTime: Int? = null,
        val series: String? = null,
        val seasonNumber: Int? = null,
        val episodeNumber: Int? = null,
        val track: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val releaseDate: String? = null,
        val releaseYear: Int? = null
) : VideoInfo