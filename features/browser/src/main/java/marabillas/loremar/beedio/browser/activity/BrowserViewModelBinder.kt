package marabillas.loremar.beedio.browser.activity

import android.widget.TextView
import androidx.lifecycle.Observer
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding
import marabillas.loremar.beedio.browser.viewmodel.BrowserViewModel

class BrowserViewModelBinder(
        private val activity: BrowserActivity,
        private val actionBarUpdater: BrowserActionBarUpdater,
        private val viewModel: BrowserViewModel,
        private val binding: ActivityBrowserBinding) {

    fun bind() {
        val titleView = actionBarUpdater.customTitleView?.findViewById<TextView>(R.id.browser_title)
        val urlView = actionBarUpdater.customTitleView?.findViewById<TextView>(R.id.browser_url)
        viewModel.observeTitle(activity, Observer { titleView?.text = it })
        viewModel.observeUrl(activity, Observer { urlView?.text = it })
        binding.mainContentBrowser.appBarState = viewModel
        binding.mainContentBrowser.searchWidgetState = viewModel
        binding.mainContentBrowser.searchWidgetListener = activity.uiListener
    }
}