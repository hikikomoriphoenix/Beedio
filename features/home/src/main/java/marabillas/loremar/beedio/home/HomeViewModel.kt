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

package marabillas.loremar.beedio.home

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel(), SearchWidgetStateHolder, HomeAppBarStateHolder {
    override val searchWidgetText = MutableLiveData<String>()
    override val editTextVisibility = MutableLiveData<Int>()
    override val searchWidgetWidth = MutableLiveData<Int>()
    override val searchWidgetVerticalBias = MutableLiveData<Float>()
    override val searchIconVisibility = MutableLiveData<Int>()
    override val searchCloseBtnVisibility = MutableLiveData<Int>()
    override val homeAppBarVisibility = MutableLiveData<Int>()

    init {
        searchWidgetText.value = ""
        editTextVisibility.value = View.GONE
        searchIconVisibility.value = View.VISIBLE
        searchCloseBtnVisibility.value = View.GONE
        homeAppBarVisibility.value = View.VISIBLE
    }
}