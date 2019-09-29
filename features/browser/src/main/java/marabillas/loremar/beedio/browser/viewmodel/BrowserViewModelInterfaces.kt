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

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

interface BrowserTitleState {
    var title: String?
    var url: String?
    fun observeTitle(lifecycleOwner: LifecycleOwner, observer: Observer<String?>)
    fun observeUrl(lifecycleOwner: LifecycleOwner, observer: Observer<String?>)
}

interface BrowserAppBarState : Observable {
    @Bindable
    fun getAppBarVisibility(): Int

    fun setAppBarVisibility(value: Int)
}

interface BrowserSearchWidgetState : Observable {

    @Bindable
    fun getSearchWidgetContainerVisibility(): Int

    fun setSearchWidgetContainerVisibility(value: Int)

    @Bindable
    fun getSearchWidgetGravity(): Int

    fun setSearchWidgetGravity(value: Int)

    @Bindable
    fun getSearchWidgetWidth(): Int

    fun setSearchWidgetWidth(value: Int)

    @Bindable
    fun getSearchEditTextVisibility(): Int

    fun setSearchEditTextVisibility(value: Int)

    @Bindable
    fun getSearchCloseBtnVisibility(): Int

    fun setSearchCloseBtnVisibility(value: Int)
}