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
import android.webkit.WebChromeClient
import android.webkit.WebView
import marabillas.loremar.beedio.browser.listeners.OnPageProgressListener
import marabillas.loremar.beedio.browser.listeners.OnReceivedIconListener
import marabillas.loremar.beedio.browser.listeners.OnWebPageTitleRecievedListener
import javax.inject.Inject

class BrowserWebChromeClient @Inject constructor() : WebChromeClient() {

    var titleRecievedListener: OnWebPageTitleRecievedListener? = null
    var onReceivedIconListener: OnReceivedIconListener? = null
    var onPageProgressListener: OnPageProgressListener? = null

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        titleRecievedListener?.onWebPageTitleRecieved(view, title)
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        if (view != null && icon != null)
            onReceivedIconListener?.onReceivedIcon(view, icon)
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onPageProgressListener?.onPageProgress(newProgress)
    }
}