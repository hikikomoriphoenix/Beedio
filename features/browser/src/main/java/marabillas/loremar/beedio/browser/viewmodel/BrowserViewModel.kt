package marabillas.loremar.beedio.browser.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

class BrowserViewModel : ViewModel(), BrowserTitleState {

    private val titleData = MutableLiveData<String>()
    private val urlData = MutableLiveData<String>()

    override var title: String?
        get() = titleData.value
        set(value) {
            titleData.value = value
        }

    override var url: String?
        get() = urlData.value
        set(value) {
            urlData.value = value
        }

    override fun observeTitle(lifecycleOwner: LifecycleOwner, observer: Observer<String?>) {
        titleData.observe(lifecycleOwner, observer)
    }

    override fun observeUrl(lifecycleOwner: LifecycleOwner, observer: Observer<String?>) {
        urlData.observe(lifecycleOwner, observer)
    }
}