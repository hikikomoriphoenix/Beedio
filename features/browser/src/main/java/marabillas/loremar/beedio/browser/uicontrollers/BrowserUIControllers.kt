package marabillas.loremar.beedio.browser.uicontrollers

interface TitleControllerInterface {
    fun updateTitle(title: String?, url: String?)
    fun updateTitle(title: String?)
}

interface WebPageNavigatorInterface {
    fun goBack()
    fun goForward()
    fun reloadPage()
}