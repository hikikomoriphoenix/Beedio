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

package marabillas.loremar.beedio.browser.web

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import marabillas.loremar.beedio.browser.listeners.OnLoadResourceListener
import marabillas.loremar.beedio.browser.listeners.OnPageProgressListener
import marabillas.loremar.beedio.browser.listeners.OnWebPageChangedListener
import javax.inject.Inject

class BrowserWebViewClient @Inject constructor() : WebViewClient() {

    var onWebPageChangedListener: OnWebPageChangedListener? = null
    var onLoadResourceListener: OnLoadResourceListener? = null
    var onPageProgressListener: OnPageProgressListener? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onWebPageChangedListener?.onWebPageChanged(view, url, favicon)
        onPageProgressListener?.onPageStarted()
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        onWebPageChangedListener?.onWebPageChanged(view, url, null)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onWebPageChangedListener?.onWebPageChanged(view, url, null)
        onPageProgressListener?.onPageFinished()
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        onLoadResourceListener?.onLoadResource(view, url)
    }
}