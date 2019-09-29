package marabillas.loremar.beedio.browser.activity

import android.graphics.Typeface
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding

class BrowserActionBarUpdater(
        private val activity: BrowserActivity,
        private val binding: ActivityBrowserBinding) {

    var customTitleView: View? = null; private set

    fun update() {
        setupActionBar()
        configureActionBar()
    }

    private fun setupActionBar() {
        val actionBar = binding.mainContentBrowser.browserToolbar
        activity.setSupportActionBar(actionBar)
        actionBar.setNavigationOnClickListener { binding.navDrawerBrowser.openDrawer(GravityCompat.START) }
        actionBar.setOnMenuItemClickListener(activity.menuItemClickListener)
    }

    private fun configureActionBar() {
        customTitleView = View.inflate(activity, R.layout.browser_toolbar_custom_view, null)
        activity.supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            customView = customTitleView
        }
    }

    fun setupOptionsMenu(menu: Menu?) {
        activity.menuInflater.inflate(R.menu.browser_menu, menu)

        val switchMenuItem = menu?.findItem(R.id.browser_menu_switch_window)
        val switchView = TextView(activity)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val textColor = ResourcesCompat.getColor(activity.resources, R.color.yellow, null)
        switchView.apply {
            text = "1"
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

        activity.webViewsController.onUpdateWebViewsCountIndicator = { count -> switchView.text = "$count" }
    }

    private fun openWebViewSwitcherSheet() {
        activity.switcherSheet.webViewSwitcher = activity.webViewsController
        if (!activity.switcherSheet.isAdded) {
            activity.switcherSheet.show(activity.supportFragmentManager, null)
        }
    }
}