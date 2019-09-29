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

package marabillas.loremar.beedio.browser.uicontrollers

import android.webkit.WebView

interface TitleControllerInterface {
    fun updateTitle(title: String?, url: String?)
    fun updateTitle(title: String?)
    fun updateTitle(webView: WebView?, title: String?, url: String?)
}

interface WebPageNavigatorInterface {
    fun goBack()
    fun goForward()
    fun reloadPage()
}

interface WebViewSwitcherInterface {
    val webViews: MutableList<WebView>
    val activeWebViewIndex: Int

    fun newWebView(url: String)
    fun switchWebView(index: Int)
    fun closeWebView()
}

interface BrowserSearchWidgetControllerInterface {
    fun showSearchWidget()
    fun onCloseBtnClicked()
}