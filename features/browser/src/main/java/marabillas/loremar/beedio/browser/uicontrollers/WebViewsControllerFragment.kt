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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.appbar.AppBarLayout
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import marabillas.loremar.beedio.browser.viewmodel.BrowserTitleStateVM
import marabillas.loremar.beedio.browser.viewmodel.WebPageNavigationVM
import marabillas.loremar.beedio.browser.viewmodel.WebViewsControllerVM
import marabillas.loremar.beedio.browser.viewmodel.WebViewsCountIndicatorVM
import javax.inject.Inject

class WebViewsControllerFragment @Inject constructor() : DaggerFragment() {

    var webChromeClient: WebChromeClient? = null
        set(value) {
            field = value
            webViews.forEach { it.webChromeClient = value }
        }

    var webViewClient: WebViewClient? = null
        set(value) {
            field = value
            webViews.forEach { it.webViewClient = value }
        }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mainViewModel: MainViewModel? = null
    private var webViewsControllerVM: WebViewsControllerVM? = null
    private var titleStateVM: BrowserTitleStateVM? = null
    private var webPageNavigationVM: WebPageNavigationVM? = null
    private var webViewsCountIndicatorVM: WebViewsCountIndicatorVM? = null

    private var webViewsContainer: FrameLayout? = null
    private val webViews; get() = mainViewModel?.webViews ?: mutableListOf()
    private var activeWebViewIndex
        get() = mainViewModel?.activeWebViewIndex ?: -1
        set(value) {
            mainViewModel?.activeWebViewIndex = value
        }
    private val activeWebView: WebView?
        get() = if (activeWebViewIndex != -1)
            webViews[activeWebViewIndex]
        else
            null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (webViewsContainer == null) {
            webViewsContainer = FrameLayout(inflater.context).apply {
                layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
            }
        }
        return webViewsContainer
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            mainViewModel = ViewModelProvider(it, viewModelFactory).get(MainViewModel::class.java)
            webViewsControllerVM = ViewModelProviders.of(it, viewModelFactory).get(WebViewsControllerVM::class.java)
            titleStateVM = ViewModelProviders.of(it, viewModelFactory).get(BrowserTitleStateVM::class.java)
            webPageNavigationVM = ViewModelProviders.of(it, viewModelFactory).get(WebPageNavigationVM::class.java)
            webViewsCountIndicatorVM = ViewModelProviders.of(it, viewModelFactory).get(WebViewsCountIndicatorVM::class.java)
            observeWebViewControllerVM()
            observeWebPageNavigationVM()
        }
    }

    private fun observeWebViewControllerVM() {
        activity?.let { lifecycleOwer ->

            this.also { thisFragment ->

                webViewsControllerVM?.apply {
                    observeRequestUpdatedWebViews(lifecycleOwer, Observer {
                        it(thisFragment.webViews, activeWebViewIndex)
                    }
                    )
                    observeRequestActiveWebView(lifecycleOwer, Observer {
                        it(thisFragment.activeWebView)
                    })
                    observeNewWebView(lifecycleOwer, Observer { thisFragment.newWebView(it) })
                    observeSwitchWebView(lifecycleOwer, Observer { thisFragment.switchWebView(it) })
                    observeCloseWebView(lifecycleOwer, Observer { thisFragment.closeWebView() })
                }
            }
        }
    }

    private fun newWebView(url: String) {
        addWebView(url)
        updateWebViewsCountIndicator()
    }

    private fun switchWebView(index: Int) {
        activeWebView?.visibility = View.GONE
        activeWebViewIndex = index
        (activeWebView?.parent as ViewGroup?)?.removeView(activeWebView)
        webViewsContainer?.addView(activeWebView)

        activeWebView?.let {
            it.visibility = View.VISIBLE
            titleStateVM?.title = it.title
            titleStateVM?.url = it.url
        }

        updateWebViewsCountIndicator()
    }

    private fun closeWebView() {
        (activeWebView?.parent as ViewGroup?)?.removeView(activeWebView)
        val index = activeWebViewIndex
        webViews.removeAt(index)
        activeWebViewIndex--
        if (webViews.isNotEmpty())
            if (index > 0) {
                switchWebView(index - 1)
            } else {
                switchWebView(index)
            }
        else
            mainViewModel?.goToHome()

        updateWebViewsCountIndicator()
    }

    private fun observeWebPageNavigationVM() {

        activity?.let { lifecycleOwner ->

            webPageNavigationVM?.apply {
                observeGoBack(lifecycleOwner, Observer { activeWebView?.goBack() })
                observeGoForward(lifecycleOwner, Observer { activeWebView?.goForward() })
                observeReloadPage(lifecycleOwner, Observer { activeWebView?.reload() })
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun addWebView(url: String) {
        this.also { webViewsController ->
            activeWebView?.visibility = View.GONE
            WebView(activity).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                webViewClient = webViewsController.webViewClient
                webChromeClient = webViewsController.webChromeClient
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.domStorageEnabled = true

                webViews.add(this)
                activeWebViewIndex = webViews.indexOf(this)
                webViewsContainer?.addView(activeWebView)

                loadUrl(url)
            }
        }
    }

    private fun updateWebViewsCountIndicator() {
        webViewsCountIndicatorVM?.webViewsCount = webViews.count()
    }

    override fun onStart() {
        super.onStart()

        if (webViews.isEmpty()) {
            arguments?.getString("url")?.let {
                addWebView(it)
            }
        } else {
            webViewsContainer?.apply {
                webViews.forEach {
                    (it.parent as ViewGroup).removeView(it)
                    addView(it)
                }
                activeWebView?.let {
                    (it.parent as ViewGroup).removeView(it)
                    addView(it)
                    it.reload()
                }
            }
        }

        updateWebViewsCountIndicator()
    }
}