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

package marabillas.loremar.beedio.base.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _isNavDrawerOpen = MutableLiveData<Boolean>()

    val isNavDrawerOpenLiveData = _isNavDrawerOpen as LiveData<Boolean>

    val goToHomeEvent = ActionLiveData()
    val goToBrowserEvent = SendLiveData<String>()

    fun setIsNavDrawerOpen(value: Boolean) {
        _isNavDrawerOpen.value = value
    }

    fun goToHome() = goToHomeEvent.go()

    fun goToBrowser(url: String) = goToBrowserEvent.send(url)
}