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

package marabillas.loremar.beedio.browser.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class BrowserTitleStateVMImpl : BrowserTitleStateVM() {

    private val titleData = MutableLiveData<String>()
    private val urlData = MutableLiveData<String>()

    override var title: String?
        get() = titleData.value
        set(value) {
            titleData.value = value
        }

    override var url: String?
        get() = urlData.value
        set(value) {
            urlData.value = value
        }

    override fun observeTitle(lifecycleOwner: LifecycleOwner, observer: Observer<String?>) {
        titleData.observe(lifecycleOwner, observer)
    }

    override fun observeUrl(lifecycleOwner: LifecycleOwner, observer: Observer<String?>) {
        urlData.observe(lifecycleOwner, observer)
    }
}