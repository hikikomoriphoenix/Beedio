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

package marabillas.loremar.beedio.base.download

import android.content.Context
import java.io.File

class DownloadFileValidator(private val context: Context) {
    private val downloadFolder: File? by lazy {
        VideoDownloader.getDownloadFolder(context)
    }

    fun validateName(name: String, ext: String, checkIfAlreadyExists: (String) -> Boolean): String {
        var mName = name.replace("[^\\w ()'!\\[\\]\\-]".toRegex(), "").trim()
        if (mName.length > 127) {//allowed filename length is 127
            mName = mName.substring(0, 127)
        } else if (mName == "") {
            mName = "video"
        }

        val downloadFolder = downloadFolder ?: return mName.getUniqueName(checkIfAlreadyExists)

        var i = 0
        var file = File(downloadFolder, "$mName.$ext")
        var incName = mName
        while (true) {
            if (!file.exists() && !checkIfAlreadyExists(incName))
                return incName
            incName = "$mName ${++i}"
            file = File(downloadFolder, "$incName.$ext")
        }
    }

    private fun String.getUniqueName(checkIfAlreadyExists: (String) -> Boolean): String {
        var name = this
        var i = 0
        while (checkIfAlreadyExists(name))
            name = "$name ${++i}"
        return name
    }
}