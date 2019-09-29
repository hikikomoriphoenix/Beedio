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
import marabillas.loremar.beedio.base.di.ActivityScope
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerInterface
import marabillas.loremar.beedio.browser.uicontrollers.TitleControllerInterface
import javax.inject.Inject

@ActivityScope
class BrowserUIEventsListener @Inject constructor() : OnWebPageChangedListener,
        OnWebPageTitleRecievedListener, BrowserSearchWidgetListener {

    var titleController: TitleControllerInterface? = null
    var searchWidgetController: BrowserSearchWidgetControllerInterface? = null

    override fun onWebPageChanged(title: String?, url: String?, favicon: Bitmap?) {
        titleController?.updateTitle(title, url)
    }

    override fun onWebPageChanged(webView: WebView?, url: String?, favicon: Bitmap?) {
        titleController?.updateTitle(webView, webView?.title, url)
    }

    override fun onWebPageTitleRecieved(title: String?) {
        titleController?.updateTitle(title)
    }

    override fun onWebPageTitleRecieved(webView: WebView?, title: String?) {
        titleController?.updateTitle(webView, title, null)
    }

    override fun onSearchCloseBtnClicked() {
        searchWidgetController?.onCloseBtnClicked()
    }
}