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

package marabillas.loremar.beedio.browser.listeners

import android.graphics.Bitmap
import android.webkit.WebView
import marabillas.loremar.beedio.base.extensions.toPixels
import marabillas.loremar.beedio.browser.viewmodel.*
import javax.inject.Inject

class BrowserUIEventsListener @Inject constructor() : OnWebPageChangedListener,
        OnWebPageTitleRecievedListener, BrowserSearchWidgetListener, OnLoadResourceListener,
        OnReceivedIconListener {

    var webViewsControllerVM: WebViewsControllerVM? = null
    var titleStateVM: BrowserTitleStateVM? = null
    var searchWidgetControllerVM: BrowserSearchWidgetControllerVM? = null
    var videoDetectionVM: VideoDetectionVM? = null
    var historyVM: BrowserHistoryVM? = null

    override fun onWebPageChanged(webView: WebView?, url: String?, favicon: Bitmap?) {
        val updateTitle = { activeWebView: WebView? ->
            if (activeWebView == webView) {
                titleStateVM?.title = webView?.title
                titleStateVM?.url = url
            }
        }
        webViewsControllerVM?.requestActiveWebView(updateTitle)
    }

    override fun onWebPageTitleRecieved(webView: WebView?, title: String?) {
        val updateTitle = { activeWebView: WebView? ->
            if (activeWebView == webView) {
                titleStateVM?.title = title
                webView?.apply {
                    historyVM?.addNewVisitedPage(url, title ?: this.title, favicon)
                }
            }
        }
        webViewsControllerVM?.requestActiveWebView(updateTitle)
    }

    override fun onSearchCloseBtnClicked() {
        searchWidgetControllerVM?.onCloseBtnClicked()
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        val page = view?.url
        val title = view?.title ?: ""
        if (url != null && page != null)
            videoDetectionVM?.analyzeUrlForVideo(url, title, page)
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap) {
        val dstSize = 32.toPixels(view.resources)
        Bitmap.createScaledBitmap(icon, dstSize, dstSize, false)
        historyVM?.updateVisitedPageIcon(view.url, icon)
    }
}