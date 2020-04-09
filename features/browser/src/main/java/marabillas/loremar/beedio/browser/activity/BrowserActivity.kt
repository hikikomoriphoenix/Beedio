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
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding
import marabillas.loremar.beedio.browser.fragment.AddBookmarkFragment
import marabillas.loremar.beedio.browser.listeners.BrowserMenuItemClickListener
import marabillas.loremar.beedio.browser.listeners.BrowserUIEventsListener
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.ExpandingFoundVideosFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherSheetFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewsControllerFragment
import marabillas.loremar.beedio.browser.viewmodel.*
import marabillas.loremar.beedio.browser.web.BrowserWebChromeClient
import marabillas.loremar.beedio.browser.web.BrowserWebViewClient
import marabillas.loremar.beedio.sharedui.NavigationActivity
import timber.log.Timber
import javax.inject.Inject

class BrowserActivity : NavigationActivity(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var webViewsController: WebViewsControllerFragment
    @Inject
    lateinit var searchWidgeController: BrowserSearchWidgetControllerFragment
    @Inject
    lateinit var switcherSheet: WebViewSwitcherSheetFragment
    @Inject
    lateinit var expandingFoundVideosFragment: ExpandingFoundVideosFragment

    @Inject
    lateinit var addBookmarkFragment: AddBookmarkFragment
    @Inject
    lateinit var browserWebViewClient: BrowserWebViewClient
    @Inject
    lateinit var browserWebChromeClient: BrowserWebChromeClient
    @Inject
    lateinit var uiListener: BrowserUIEventsListener
    @Inject
    lateinit var menuItemClickListener: BrowserMenuItemClickListener

    private lateinit var binding: ActivityBrowserBinding

    private lateinit var titleStateVM: BrowserTitleStateVM
    private lateinit var webPageNavigationVM: WebPageNavigationVM
    private lateinit var webViewsControllerVM: WebViewsControllerVM
    private lateinit var searchWidgetControllerVM: BrowserSearchWidgetControllerVM
    private lateinit var actionBarStateVM: BrowserActionBarStateVM
    private lateinit var searchWidgetStateVM: BrowserSearchWidgetStateVM
    private lateinit var webViewsCountIndicatorVM: WebViewsCountIndicatorVM
    private lateinit var videoDetectionVM: VideoDetectionVM
    private lateinit var addBookmarkVM: AddBookmarkVM

    private lateinit var controllersUpdater: BrowserControllersUpdater
    private lateinit var actionBarUpdater: BrowserActionBarUpdater
    private lateinit var viewModelBinder: BrowserViewModelBinder
    private lateinit var listenersUpdater: BrowserListenersUpdater

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)
        binding.lifecycleOwner = this
        titleStateVM = ViewModelProviders.of(this, viewModelFactory).get(BrowserTitleStateVM::class.java)
        webPageNavigationVM = ViewModelProviders.of(this, viewModelFactory).get(WebPageNavigationVM::class.java)
        webViewsControllerVM = ViewModelProviders.of(this, viewModelFactory).get(WebViewsControllerVM::class.java)
        searchWidgetControllerVM = ViewModelProviders.of(this, viewModelFactory)
                .get(BrowserSearchWidgetControllerVM::class.java)
        actionBarStateVM = ViewModelProviders.of(this, viewModelFactory).get(BrowserActionBarStateVM::class.java)
        searchWidgetStateVM = ViewModelProviders.of(this, viewModelFactory).get(BrowserSearchWidgetStateVM::class.java)
        webViewsCountIndicatorVM = ViewModelProviders.of(this, viewModelFactory).get(WebViewsCountIndicatorVM::class.java)
        videoDetectionVM = ViewModelProvider(this::getViewModelStore, viewModelFactory)[VideoDetectionVM::class.java]
        addBookmarkVM = ViewModelProvider(this::getViewModelStore, viewModelFactory)[AddBookmarkVM::class.java]

        actionBarUpdater = BrowserActionBarUpdater(this, binding, webViewsCountIndicatorVM)
        viewModelBinder = BrowserViewModelBinder(this, actionBarUpdater, actionBarStateVM,
                searchWidgetStateVM, titleStateVM, binding)
        controllersUpdater = BrowserControllersUpdater(this)
        listenersUpdater = BrowserListenersUpdater(this, webPageNavigationVM,
                webViewsControllerVM, titleStateVM, searchWidgetControllerVM, videoDetectionVM)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        actionBarUpdater.setupOptionsMenu(menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        actionBarUpdater.update()
        viewModelBinder.bind()
        controllersUpdater.update()
        listenersUpdater.update()

        addBookmarkVM.observeOpenBookmarker(this, Observer {
            supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.main_content_browser, addBookmarkFragment, null)
                    .commit()
        })
    }

    override fun onDestroy() {
        videoDetectionVM.closeDetailsFetcher()
        super.onDestroy()
    }
}