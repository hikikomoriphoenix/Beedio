package marabillas.loremar.beedio.browser.uicontrollers

import android.webkit.WebView

interface TitleControllerInterface {
    fun updateTitle(title: String?, url: String?)
    fun updateTitle(title: String?)
    fun updateTitle(webView: WebView?, title: String?, url: String?)
}

interface WebPageNavigatorInterface {
    fun goBack()
    fun goForward()
    fun reloadPage()
}

interface WebViewSwitcherInterface {
    val webViews: MutableList<WebView>
    val activeWebViewIndex: Int

    fun newWebView(url: String)
    fun switchWebView(index: Int)
    fun closeWebView()
}

interface BrowserSearchWidgetControllerInterface {
    fun showSearchWidget()
    fun onCloseBtnClicked()
}