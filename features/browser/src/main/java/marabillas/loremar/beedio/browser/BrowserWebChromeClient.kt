package marabillas.loremar.beedio.browser

import android.webkit.WebChromeClient
import android.webkit.WebView
import marabillas.loremar.beedio.browser.listeners.OnWebPageTitleRecievedListener
import javax.inject.Inject

class BrowserWebChromeClient @Inject constructor() : WebChromeClient() {

    var titleRecievedListener: OnWebPageTitleRecievedListener? = null

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        titleRecievedListener?.onWebPageTitleRecieved(title)
    }
}