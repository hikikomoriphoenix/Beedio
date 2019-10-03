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

import android.view.Gravity
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.MutableLiveData

class BrowserSearchWidgetStateVMImpl : BrowserSearchWidgetStateVM() {

    private val searchWidgetContainerVisibilityData = MutableLiveData<Int>()
    private val searchWidgetGravityData = MutableLiveData<Int>()
    private val searchWidgetWidthData = MutableLiveData<Int>()
    private val searchEditTextVisibilityData = MutableLiveData<Int>()
    private val searchCloseBtnVisibilityData = MutableLiveData<Int>()

    init {
        searchWidgetContainerVisibilityData.value = View.GONE
        searchWidgetGravityData.value = Gravity.END
        searchWidgetWidthData.value = 0
        searchEditTextVisibilityData.value = View.INVISIBLE
        searchCloseBtnVisibilityData.value = View.INVISIBLE
    }

    @Bindable
    override fun getSearchWidgetContainerVisibility(): Int {
        return searchWidgetContainerVisibilityData.value ?: View.GONE
    }

    override fun setSearchWidgetContainerVisibility(value: Int) {
        searchWidgetContainerVisibilityData.value = value
        notifyChange(BR.searchWidgetContainerVisibility)
    }

    @Bindable
    override fun getSearchWidgetGravity(): Int {
        return searchWidgetGravityData.value ?: Gravity.END
    }

    override fun setSearchWidgetGravity(value: Int) {
        searchWidgetGravityData.value = value
        notifyChange(BR.searchWidgetGravity)
    }

    @Bindable
    override fun getSearchWidgetWidth(): Int {
        return searchWidgetWidthData.value ?: 0
    }

    override fun setSearchWidgetWidth(value: Int) {
        searchWidgetWidthData.value = value
        notifyChange(BR.searchWidgetWidth)
    }

    @Bindable
    override fun getSearchEditTextVisibility(): Int {
        return searchEditTextVisibilityData.value ?: View.INVISIBLE
    }

    override fun setSearchEditTextVisibility(value: Int) {
        searchEditTextVisibilityData.value = value
        notifyChange(BR.searchEditTextVisibility)
    }

    @Bindable
    override fun getSearchCloseBtnVisibility(): Int {
        return searchCloseBtnVisibilityData.value ?: View.INVISIBLE
    }

    override fun setSearchCloseBtnVisibility(value: Int) {
        searchCloseBtnVisibilityData.value = value
        notifyChange(BR.searchCloseBtnVisibility)
    }
}