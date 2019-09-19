package marabillas.loremar.beedio.home

import androidx.lifecycle.MutableLiveData

interface SearchWidgetStateHolder {
    val searchWidgetText: MutableLiveData<String>
    val editTextVisibility: MutableLiveData<Int>
    val searchWidgetWidth: MutableLiveData<Int>
    val searchWidgetVerticalBias: MutableLiveData<Float>
    val searchIconVisibility: MutableLiveData<Int>
    val searchCloseBtnVisibility: MutableLiveData<Int>
}