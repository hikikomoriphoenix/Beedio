package marabillas.loremar.beedio.browser.uicontrollers

import android.webkit.WebView
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.browser.R
import javax.inject.Inject

class WebPageNavigatorFragment @Inject constructor() : DaggerFragment(), WebPageNavigatorInterface {

    override fun goBack() {
        getWebView()?.goBack()
    }

    override fun goForward() {
        getWebView()?.goForward()
    }

    override fun reloadPage() {
        getWebView()?.reload()
    }

    private fun getWebView(): WebView? {
        return activity?.findViewById(R.id.browser_webview)
    }

}