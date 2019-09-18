package marabillas.loremar.beedio.browser

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding

class BrowserActivity : DaggerAppCompatActivity() {

    lateinit var binding: ActivityBrowserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)
        binding.lifecycleOwner = this

        setupActionBar()
        configureActionBar()
    }

    private fun setupActionBar() {
        val actionBar = binding.mainContentBrowser.browserToolbar
        actionBar.setNavigationOnClickListener { binding.navDrawerBrowser.openDrawer(GravityCompat.START) }
        setSupportActionBar(actionBar)
    }

    private fun configureActionBar() {
        val customView = View.inflate(this, R.layout.browser_toolbar_custom_view, null)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            this.customView = customView
        }
    }

    override fun onStart() {
        super.onStart()
        val url = intent.getStringExtra("url")
        setupWebView(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String) {
        binding.mainContentBrowser
                .browserWebview
                .apply {
                    settings.javaScriptEnabled = true
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.browser_menu, menu)
        val switchMenuItem = menu?.findItem(R.id.browser_switch_tool)
        val switchView = TextView(this)
        val params = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        switchView.apply {
            text = "6"
            setTextColor(Color.WHITE)
            background = AppCompatResources.getDrawable(context, R.drawable.switch_window_icon)
            layoutParams = params
        }
        switchMenuItem?.actionView = switchView

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }
}