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

package marabillas.loremar.beedio.base.web

import androidx.core.util.PatternsCompat

class WebNavigationImpl : WebNavigation {
    override fun navigateTo(dest: String): String {
        return if (PatternsCompat.WEB_URL.matcher(dest).matches()) {
            return if (!dest.startsWith("http://") || !dest.startsWith("https://")) {
                "http://$dest"
            } else {
                dest
            }
        } else {
            "https://google.com/search?q=$dest"
        }
    }
}