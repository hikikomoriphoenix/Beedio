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

package marabillas.loremar.beedio.browser.activity

import marabillas.loremar.beedio.browser.viewmodel.*

class BrowserListenersUpdater(private val activity: BrowserActivity,
                              private val webPageNavigationVM: WebPageNavigationVM,
                              private val webViewsControllerVM: WebViewsControllerVM,
                              private val titleStateVM: BrowserTitleStateVM,
                              private val searchWidgetControllerVM: BrowserSearchWidgetControllerVM,
                              private val videoDetectionVM: VideoDetectionVM,
                              private val historyVM: BrowserHistoryVM
) {

    fun update() {
        activity.browserWebViewClient.onWebPageChangedListener = activity.uiListener
        activity.browserWebViewClient.onLoadResourceListener = activity.uiListener
        activity.browserWebChromeClient.titleRecievedListener = activity.uiListener
        activity.browserWebChromeClient.onReceivedIconListener = activity.uiListener

        activity.uiListener.webViewsControllerVM = webViewsControllerVM
        activity.uiListener.titleStateVM = titleStateVM
        activity.uiListener.searchWidgetControllerVM = searchWidgetControllerVM
        activity.uiListener.videoDetectionVM = videoDetectionVM
        activity.uiListener.historyVM = historyVM
        activity.menuItemClickListener.webPageNavigation = webPageNavigationVM
        activity.menuItemClickListener.searchWidgetControllerVM = searchWidgetControllerVM
        activity.menuItemClickListener.webViewsController = webViewsControllerVM
    }
}