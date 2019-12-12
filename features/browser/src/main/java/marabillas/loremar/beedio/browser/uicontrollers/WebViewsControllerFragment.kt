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
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.viewmodel.BrowserTitleStateVM
import marabillas.loremar.beedio.browser.viewmodel.WebPageNavigationVM
import marabillas.loremar.beedio.browser.viewmodel.WebViewsControllerVM
import marabillas.loremar.beedio.browser.viewmodel.WebViewsCountIndicatorVM
import javax.inject.Inject

class WebViewsControllerFragment @Inject constructor() : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var webViewsControllerVM: WebViewsControllerVM? = null
    private var titleStateVM: BrowserTitleStateVM? = null
    private var webPageNavigationVM: WebPageNavigationVM? = null
    private var webViewsCountIndicatorVM: WebViewsCountIndicatorVM? = null

    private val webViews = mutableListOf<WebView>()
    private var activeWebView: WebView? = null
    private val activeWebViewIndex: Int; get() = webViews.indexOf(activeWebView)
    private var webViewsContainer: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (webViewsContainer == null) {
            webViewsContainer = FrameLayout(inflater.context)
            webViewsContainer?.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
        return webViewsContainer
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            webViewsControllerVM = ViewModelProviders.of(it, viewModelFactory).get(WebViewsControllerVM::class.java)
            titleStateVM = ViewModelProviders.of(it, viewModelFactory).get(BrowserTitleStateVM::class.java)
            webPageNavigationVM = ViewModelProviders.of(it, viewModelFactory).get(WebPageNavigationVM::class.java)
            webViewsCountIndicatorVM = ViewModelProviders.of(it, viewModelFactory).get(WebViewsCountIndicatorVM::class.java)
            observeWebViewControllerVM()
            observeWebPageNavigationVM()
        }

        val parentContainer = activity?.findViewById<ViewGroup>(R.id.browser_webview_containter)
        updateParent(webViewsContainer, parentContainer)

        if (webViews.isEmpty()) {
            val url = activity?.intent?.getStringExtra("url")
            url?.let { addWebView(it) }
        }

    }

    private fun observeWebViewControllerVM() {
        activity?.let { lifecycleOwer ->

            this.also { thisFragment ->

                webViewsControllerVM?.apply {
                    observeRequestUpdatedWebViews(lifecycleOwer, Observer { it(thisFragment.webViews, activeWebViewIndex) })
                    observeRequestActiveWebView(lifecycleOwer, Observer { it(thisFragment.activeWebView) })
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

        activeWebView = webViews[index]
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
        val index = webViews.indexOf(activeWebView)
        webViews.removeAt(index)
        if (webViews.isNotEmpty())
            if (index > 0) {
                switchWebView(index - 1)
            } else {
                switchWebView(index)
            }
        else
            activity?.finish()

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

    private fun updateParent(view: View?, parent: ViewGroup?) {
        (view?.parent as ViewGroup?)?.removeView(view)
        parent?.addView(view)
    }

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

    @SuppressLint("SetJavaScriptEnabled")
    private fun addWebView(url: String) {
        this.also { webViewsController ->
            activeWebView?.visibility = View.GONE
            activeWebView = WebView(activity).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                webViewClient = webViewsController.webViewClient
                webChromeClient = webViewsController.webChromeClient
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.domStorageEnabled = true
                loadUrl(url)
            }
        }

        webViewsContainer?.addView(activeWebView)
        activeWebView?.let { webViews.add(it) }
    }

    private fun updateWebViewsCountIndicator() {
        webViewsCountIndicatorVM?.webViewsCount = webViews.count()
    }

    override fun onStart() {
        super.onStart()
        updateWebViewsCountIndicator()
    }
}