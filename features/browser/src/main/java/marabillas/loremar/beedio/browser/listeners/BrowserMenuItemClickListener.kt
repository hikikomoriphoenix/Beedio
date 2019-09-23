package marabillas.loremar.beedio.browser.listeners

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerInterface
import marabillas.loremar.beedio.browser.uicontrollers.WebPageNavigatorInterface
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherInterface
import javax.inject.Inject

class BrowserMenuItemClickListener @Inject constructor() : Toolbar.OnMenuItemClickListener {
    var webPageNavigator: WebPageNavigatorInterface? = null
    var searchWidgetController: BrowserSearchWidgetControllerInterface? = null
    var webViewSwitcher: WebViewSwitcherInterface? = null

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.browser_menu_back -> webPageNavigator?.goBack()
            R.id.browser_menu_forward -> webPageNavigator?.goForward()
            R.id.browser_menu_reload -> webPageNavigator?.reloadPage()
            R.id.browser_menu_add_window -> searchWidgetController?.showSearchWidget()
            R.id.browser_menu_close_window -> webViewSwitcher?.closeWebView()
        }

        return true
    }
}