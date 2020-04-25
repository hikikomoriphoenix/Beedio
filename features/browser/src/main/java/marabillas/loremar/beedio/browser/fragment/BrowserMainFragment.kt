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

package marabillas.loremar.beedio.browser.fragment

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.MainContentBrowserBinding
import marabillas.loremar.beedio.browser.listeners.BrowserMenuItemClickListener
import marabillas.loremar.beedio.browser.listeners.BrowserUIEventsListener
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.ExpandingFoundVideosFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherSheetFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewsControllerFragment
import marabillas.loremar.beedio.browser.viewmodel.*
import marabillas.loremar.beedio.browser.web.BrowserWebChromeClient
import marabillas.loremar.beedio.browser.web.BrowserWebViewClient
import javax.inject.Inject

class BrowserMainFragment : DaggerFragment() {
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

    private lateinit var binding: MainContentBrowserBinding

    private lateinit var mainViewModel: MainViewModel
    private lateinit var titleStateVM: BrowserTitleStateVM
    private lateinit var webPageNavigationVM: WebPageNavigationVM
    private lateinit var webViewsControllerVM: WebViewsControllerVM
    private lateinit var searchWidgetControllerVM: BrowserSearchWidgetControllerVM
    private lateinit var actionBarStateVM: BrowserActionBarStateVM
    private lateinit var searchWidgetStateVM: BrowserSearchWidgetStateVM
    private lateinit var webViewsCountIndicatorVM: WebViewsCountIndicatorVM
    private lateinit var videoDetectionVM: VideoDetectionVM
    private lateinit var addBookmarkVM: AddBookmarkVM
    private lateinit var historyVM: BrowserHistoryVM

    private lateinit var customTitleView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_content_browser, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            mainViewModel = initViewModel(it, MainViewModel::class.java)
            titleStateVM = initViewModel(it, BrowserTitleStateVM::class.java)
            webPageNavigationVM = initViewModel(it, WebPageNavigationVM::class.java)
            webViewsControllerVM = initViewModel(it, WebViewsControllerVM::class.java)
            searchWidgetControllerVM = initViewModel(it, BrowserSearchWidgetControllerVM::class.java)
            actionBarStateVM = initViewModel(it, BrowserActionBarStateVM::class.java)
            searchWidgetStateVM = initViewModel(it, BrowserSearchWidgetStateVM::class.java)
            webViewsCountIndicatorVM = initViewModel(it, WebViewsCountIndicatorVM::class.java)
            videoDetectionVM = initViewModel(it, VideoDetectionVM::class.java)
            addBookmarkVM = initViewModel(it, AddBookmarkVM::class.java)
            historyVM = initViewModel(it, BrowserHistoryVM::class.java)
        }
    }

    override fun onStart() {
        super.onStart()
        initActionBar()
        bindViewModel()
        initControllers()
        initListeners()

        addBookmarkVM.observeOpenBookmarker(this, Observer {
            if (!addBookmarkFragment.isAdded)
                childFragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .add(R.id.browser_coordinator_layout, addBookmarkFragment, null)
                        .commit()
        })
    }

    private fun initActionBar() {
        setupActionBar()
        configureActionBar()
    }

    private fun setupActionBar() {
        val toolbar = binding.browserToolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            mainViewModel.setIsNavDrawerOpen(true)
        }
        toolbar.setOnMenuItemClickListener(menuItemClickListener)
    }

    private fun configureActionBar() {
        customTitleView = View.inflate(activity, R.layout.browser_toolbar_custom_view, null)
        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            customView = customTitleView
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.browser_menu, menu)

        val switchMenuItem = menu.findItem(R.id.browser_menu_switch_window)
        val switchView = TextView(requireContext())
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val textColor = ResourcesCompat.getColor(resources, R.color.yellow, null)

        switchView.apply {
            text = "${webViewsCountIndicatorVM.webViewsCount}"
            setTypeface(this.typeface, Typeface.BOLD)
            setTextColor(textColor)
            background = AppCompatResources.getDrawable(context, R.drawable.switch_window_icon)
            layoutParams = params
            setOnClickListener { openWebViewSwitcherSheet() }
        }
        switchMenuItem?.actionView = switchView

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        webViewsCountIndicatorVM.observeWebViewsCount(this,
                Observer { count -> switchView.text = "$count" })
    }

    private fun openWebViewSwitcherSheet() {
        if (!switcherSheet.isAdded) {
            switcherSheet.show(childFragmentManager, null)
        }
    }

    private fun bindViewModel() {
        val titleView = customTitleView.findViewById<TextView>(R.id.browser_title)
        val urlView = customTitleView.findViewById<TextView>(R.id.browser_url)
        titleStateVM.observeTitle(this, Observer { titleView?.text = it })
        titleStateVM.observeUrl(this, Observer { urlView?.text = it })

        binding.apply {
            actionBarState = actionBarStateVM
            searchWidgetState = searchWidgetStateVM
            searchWidgetListener = uiListener
        }
    }

    private fun initControllers() {
        setupWebViewsController()
        setSearchWidgetController()
        setupExpandingFoundVideosFragment()
    }

    private fun setupWebViewsController() {
        var fragment = childFragmentManager.findFragmentByTag("WebViewsControllerFragment")

        if (fragment == null) {
            val url = arguments?.getString("url")
            webViewsController.arguments = bundleOf("url" to url)
            childFragmentManager
                    .beginTransaction()
                    .add(R.id.browser_coordinator_layout, webViewsController, "WebViewsControllerFragment")
                    .commit()
            fragment = webViewsController
        }

        (fragment as WebViewsControllerFragment).also {
            it.webChromeClient = browserWebChromeClient
            it.webViewClient = browserWebViewClient
        }

    }

    private fun setSearchWidgetController() {
        childFragmentManager
                .beginTransaction()
                .add(searchWidgeController, null)
                .commit()
    }

    private fun setupExpandingFoundVideosFragment() {
        childFragmentManager
                .beginTransaction()
                .add(R.id.browser_coordinator_layout, expandingFoundVideosFragment)
                .commit()
    }

    private fun initListeners() {
        browserWebViewClient.onWebPageChangedListener = uiListener
        browserWebViewClient.onLoadResourceListener = uiListener
        browserWebChromeClient.titleRecievedListener = uiListener
        browserWebChromeClient.onReceivedIconListener = uiListener

        uiListener.webViewsControllerVM = webViewsControllerVM
        uiListener.titleStateVM = titleStateVM
        uiListener.searchWidgetControllerVM = searchWidgetControllerVM
        uiListener.videoDetectionVM = videoDetectionVM
        uiListener.historyVM = historyVM
        menuItemClickListener.webPageNavigation = webPageNavigationVM
        menuItemClickListener.searchWidgetControllerVM = searchWidgetControllerVM
        menuItemClickListener.webViewsController = webViewsControllerVM
    }

    private fun <T : ViewModel> initViewModel(activity: FragmentActivity, modelClass: Class<T>): T =
            ViewModelProvider(activity::getViewModelStore, viewModelFactory).get(modelClass)
}