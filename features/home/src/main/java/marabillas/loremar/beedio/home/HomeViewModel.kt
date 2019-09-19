package marabillas.loremar.beedio.home

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel(), SearchWidgetStateHolder, HomeAppBarStateHolder {
    override val searchWidgetText = MutableLiveData<String>()
    override val editTextVisibility = MutableLiveData<Int>()
    override val searchWidgetWidth = MutableLiveData<Int>()
    override val searchWidgetVerticalBias = MutableLiveData<Float>()
    override val searchIconVisibility = MutableLiveData<Int>()
    override val searchCloseBtnVisibility = MutableLiveData<Int>()
    override val homeAppBarVisibility = MutableLiveData<Int>()

    init {
        searchWidgetText.value = ""
        editTextVisibility.value = View.GONE
        searchIconVisibility.value = View.VISIBLE
        searchCloseBtnVisibility.value = View.GONE
        homeAppBarVisibility.value = View.VISIBLE
    }
}