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

import marabillas.loremar.beedio.browser.viewmodel.BrowserViewModel

class BrowserControllersUpdater(private val activity: BrowserActivity, private val viewModel: BrowserViewModel) {

    fun update() {
        setupTitleController()
        setupWebViewsController()
        setSearchWidgetController()
    }

    private fun setupTitleController() {
        activity.titleController.titleState = viewModel
        activity.supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, activity.titleController)
                .commit()
    }

    private fun setupWebViewsController() {
        activity.webViewsController.webChromClient = activity.browserWebChromeClient
        activity.webViewsController.webViewClient = activity.browserWebViewClient
        activity.webViewsController.titleState = viewModel

        if (activity.supportFragmentManager.findFragmentByTag("WebViewsControllerFragment") == null) {
            activity.supportFragmentManager
                    .beginTransaction()
                    .add(activity.webViewsController, "WebViewsControllerFragment")
                    .commit()
        }
    }

    private fun setSearchWidgetController() {
        activity.searchWidgeController.webViewSwitcher = activity.webViewsController
        activity.searchWidgeController.webNavigation = activity.webNavigation

        activity.supportFragmentManager
                .beginTransaction()
                .add(activity.searchWidgeController, null)
                .commit()
    }
}