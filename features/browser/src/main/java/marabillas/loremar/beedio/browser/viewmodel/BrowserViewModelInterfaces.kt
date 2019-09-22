package marabillas.loremar.beedio.browser.viewmodel

import android.webkit.WebBackForwardList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

interface BrowserTitleState {
    var title: String?
    var url: String?
    fun observeTitle(lifecycleOwner: LifecycleOwner, observer: Observer<String?>)
    fun observeUrl(lifecycleOwner: LifecycleOwner, observer: Observer<String?>)
}