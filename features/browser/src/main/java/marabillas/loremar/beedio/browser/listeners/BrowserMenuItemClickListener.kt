package marabillas.loremar.beedio.browser.listeners

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.uicontrollers.WebPageNavigatorInterface
import javax.inject.Inject

class BrowserMenuItemClickListener @Inject constructor() : Toolbar.OnMenuItemClickListener {
    var webPageNavigator: WebPageNavigatorInterface? = null

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.browser_menu_back -> webPageNavigator?.goBack()
            R.id.browser_menu_forward -> webPageNavigator?.goForward()
            R.id.browser_menu_reload -> webPageNavigator?.reloadPage()
        }

        return true
    }
}