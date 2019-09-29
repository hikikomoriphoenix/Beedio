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

import android.os.Bundle
import android.view.Menu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.base.web.WebNavigation
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding
import marabillas.loremar.beedio.browser.listeners.BrowserMenuItemClickListener
import marabillas.loremar.beedio.browser.listeners.BrowserUIEventsListener
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.BrowserTitleControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherSheetFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewsControllerFragment
import marabillas.loremar.beedio.browser.viewmodel.BrowserViewModel
import marabillas.loremar.beedio.browser.web.BrowserWebChromeClient
import marabillas.loremar.beedio.browser.web.BrowserWebViewClient
import javax.inject.Inject

class BrowserActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var titleController: BrowserTitleControllerFragment
    @Inject
    lateinit var webViewsController: WebViewsControllerFragment
    @Inject
    lateinit var searchWidgeController: BrowserSearchWidgetControllerFragment
    @Inject
    lateinit var switcherSheet: WebViewSwitcherSheetFragment
    @Inject
    lateinit var browserWebViewClient: BrowserWebViewClient
    @Inject
    lateinit var browserWebChromeClient: BrowserWebChromeClient
    @Inject
    lateinit var uiListener: BrowserUIEventsListener
    @Inject
    lateinit var menuItemClickListener: BrowserMenuItemClickListener
    @Inject
    lateinit var webNavigation: WebNavigation

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var viewModel: BrowserViewModel
    private lateinit var controllersUpdater: BrowserControllersUpdater
    private lateinit var actionBarUpdater: BrowserActionBarUpdater
    private lateinit var viewModelBinder: BrowserViewModelBinder
    private lateinit var listenersUpdater: BrowserListenersUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this).get(BrowserViewModel::class.java)

        actionBarUpdater = BrowserActionBarUpdater(this, binding)
        viewModelBinder = BrowserViewModelBinder(this, actionBarUpdater, viewModel, binding)
        controllersUpdater = BrowserControllersUpdater(this, viewModel)
        listenersUpdater = BrowserListenersUpdater(this)

        actionBarUpdater.update()
        viewModelBinder.bind()
        controllersUpdater.update()
        listenersUpdater.update()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        actionBarUpdater.setupOptionsMenu(menu)
        return true
    }

}