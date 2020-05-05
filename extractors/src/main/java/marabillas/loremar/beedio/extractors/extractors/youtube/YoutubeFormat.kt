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

import marabillas.loremar.beedio.extractors.VideoFormat

data class YoutubeFormat(
        val id: String? = null,
        val url: String? = null,
        val ext: String? = null,
        val filesize: Int? = null,
        val resolution: String? = null,
        val width: Int? = null,
        val height: Int? = null,
        val formatNote: String? = null,
        val tbr: Float? = null,
        val abr: Int? = null,
        val vcodec: String? = null,
        val acodec: String? = null,
        val fps: Int? = null,
        val playerUrl: String? = null,
        val container: String? = null
) : VideoFormat