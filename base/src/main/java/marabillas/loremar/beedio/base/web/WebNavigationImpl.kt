package marabillas.loremar.beedio.base.web

import androidx.core.util.PatternsCompat

class WebNavigationImpl : WebNavigation {
    override fun navigateTo(dest: String): String {
        return if (PatternsCompat.WEB_URL.matcher(dest).matches()) {
            return if (!dest.startsWith("http://") || !dest.startsWith("https://")) {
                "http://$dest"
            } else {
                dest
            }
        } else {
            "https://google.com/search?q=$dest"
        }
    }
}