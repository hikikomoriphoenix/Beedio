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

import android.webkit.WebView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import marabillas.loremar.beedio.base.mvvm.ActionLiveData
import marabillas.loremar.beedio.base.mvvm.SendLiveData

class WebViewsControllerVMImpl : WebViewsControllerVM() {

    private val requestUpdatedWebView = SendLiveData<(List<WebView>, Int) -> Unit>()
    private val requestActiveWebView = SendLiveData<(WebView?) -> Unit>()
    private val newWebView = SendLiveData<String>()
    private val switchWebView = SendLiveData<Int>()
    private val closeWebView = ActionLiveData()
    private val openBookmarker = ActionLiveData()

    override fun requestUpdatedWebViews(callback: (List<WebView>, Int) -> Unit) {
        requestUpdatedWebView.send(callback)
    }

    override fun observeRequestUpdatedWebViews(lifecycleOwner: LifecycleOwner, observer: Observer<(List<WebView>, Int) -> Unit>) {
        requestUpdatedWebView.observeSend(lifecycleOwner, observer)
    }

    override fun requestActiveWebView(callback: (WebView?) -> Unit) {
        requestActiveWebView.send(callback)
    }

    override fun observeRequestActiveWebView(lifecycleOwner: LifecycleOwner, observer: Observer<(WebView?) -> Unit>) {
        requestActiveWebView.observeSend(lifecycleOwner, observer)
    }

    override fun newWebView(url: String) {
        newWebView.send(url)
    }

    override fun switchWebView(index: Int) {
        switchWebView.send(index)
    }

    override fun closeWebView() {
        closeWebView.go()
    }

    override fun openBookmarker() {
        openBookmarker.go()
    }

    override fun observeNewWebView(lifecycleOwner: LifecycleOwner, observer: Observer<String>) {
        newWebView.observeSend(lifecycleOwner, observer)
    }

    override fun observeSwitchWebView(lifecycleOwner: LifecycleOwner, observer: Observer<Int>) {
        switchWebView.observeSend(lifecycleOwner, observer)
    }

    override fun observeCloseWebView(lifecycleOwner: LifecycleOwner, observer: Observer<Any>) {
        closeWebView.observe(lifecycleOwner, observer)
    }

    override fun observeOpenBookmarker(lifecycleOwner: LifecycleOwner, observer: Observer<Any>) {
        openBookmarker.observe(lifecycleOwner, observer)
    }

}