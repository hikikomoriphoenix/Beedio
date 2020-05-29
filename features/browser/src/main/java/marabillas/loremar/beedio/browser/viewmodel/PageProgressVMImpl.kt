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

class PageProgressVMImpl : PageProgressVM() {
    private val pageProgressVisibility = MutableLiveData<Boolean>()
    private val pageProgress = MutableLiveData<Int>()

    init {
        pageProgressVisibility.value = false
        pageProgress.value = 0
    }

    override fun setPageProgressBarVisibility(isVisible: Boolean) {
        pageProgressVisibility.value = isVisible
    }

    override fun observePageProgressBarVisibility(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
        pageProgressVisibility.observe(lifecycleOwner, observer)
    }

    override fun setPageProgress(progress: Int) {
        pageProgress.value = progress
    }

    override fun observePageProgress(lifecycleOwner: LifecycleOwner, observer: Observer<Int>) {
        pageProgress.observe(lifecycleOwner, observer)
    }
}