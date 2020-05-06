/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.extractors

import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.math.BigInteger
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

object ExtractorUtils {
    private val httpClient = OkHttpClient()

    fun unsmuggleUrl(smugUrl: String): String {
        if (!smugUrl.contains("#__youtubedl_smuggle"))
            return smugUrl
        else
            TODO("No implementation for smuggled url")
    }

    fun contentOf(url: String): String? {
        return try {
            val request = Request.Builder().url(url).method("GET", null).build()
            val response = httpClient.newCall(request).execute()
            val content = if (response.code == 429) HTTP_ERROR_429 else response.body?.string()
            response.close()
            content
        } catch (e: IOException) {
            null
        }
    }

    fun extractResponseFrom(url: String): Response? {
        return try {
            val request = Request.Builder().url(url).method("GET", null).build()
            httpClient.newCall(request).execute()
        } catch (e: IOException) {
            null
        }
    }

    fun queryStringFrom(map: HashMap<String, String>): String {
        return map.map { (k, v) -> "${k.encodeUtf8()}=${v.encodeUtf8()}" }.joinToString("&")
    }

    private fun String.encodeUtf8(): String = URLEncoder.encode(this, "UTF-8")

    fun parseQueryString(queryString: String): HashMap<String, List<String>> {
        val map = hashMapOf<String, List<String>>()
        queryString.split("&").forEach {
            val key = it.substringBefore("=")
            val values = it.substringAfter("=").decodeUtf8()
            val valuesList = values.split(",")
            map[key] = valuesList
        }
        return map
    }

    private fun String.decodeUtf8(): String = URLDecoder.decode(this, "UTF-8")

    fun uppercaseEscape(s: String): String {
        return s.replace("""\\U[0-9a-fA-F]{8}""".toRegex()) {
            val unicode = it.value.substringAfter("\\U")
            String(BigInteger(unicode, 16).toByteArray())
        }
    }

    fun jsonElementToStringList(element: JsonElement): List<String> {
        val list = mutableListOf<String>()
        if (element.isJsonArray) {
            element.asJsonArray.forEach { list.add(it.asString) }
        } else {
            list.add(element.asString)
        }
        return list
    }

    /**
     * Clean an html snippet into a readable string
     */
    fun cleanHtml(html: String): String {
        // Newline vs <br />
        var cleaned = html.replace("\n", " ")
        cleaned = cleaned.replace("""(?u)\s*<\s*br\s*/?\s*>\s*""".toRegex(), "\n")
        cleaned = cleaned.replace("""(?u)<\s*/\s*p\s*>\s*<\s*p[^>]*>""".toRegex(), "\n")
        // Strip html tags
        cleaned = cleaned.replace("<.*?>".toRegex(), "")
        // Replace html entities
        cleaned = unescapeHtml(cleaned)
        return cleaned.trim()
    }

    fun unescapeHtml(s: String): String {
        return s.replace("&([^&;]+;)".toRegex()) { m ->
            m.groups[1]?.value?.let { n -> htmlEntityTransform(n) } as CharSequence
        }
    }

    // Transform and html entity to character
    fun htmlEntityTransform(s: String): String {
        val entity = s.substring(0, s.lastIndex)

        // Known non-numeric HTML entity
        val code = HtmlEntities.name2Code[entity]
        if (code != null)
            return code

        //HTML5 allows entities without a semicolon. For example,
        //'&Eacuteric' should be decoded as 'Éric'.
        val html5Entity = HtmlEntities.html5Entities[s]
        if (html5Entity != null)
            return html5Entity

        val mobj = """#(x[0-9a-fA-F]+|[0-9]+)""".toRegex().find(entity)
        if (mobj != null && entity.startsWith(mobj.value)) {
            mobj.groups[1]?.value?.let {
                var numstr = it
                val base: Int
                if (numstr.startsWith('x')) {
                    base = 16
                    numstr = "0$numstr"
                } else
                    base = 10
                // See https://github.com/ytdl-org/youtube-dl/issues/7518
                return Integer.parseInt(numstr, base).toChar().toString()
            }
        }

        // Unknown entity in name, return its literal representation
        return "&$entity"
    }

    /**
     * Return the content of the tag with the specified ID in the passed HTML document
     */
    fun getElementById(id: String, html: String): String? {
        return getElementByAttribute("id", id, html)
    }

    /**
     * Return the content of the first tag with the specified class in the passed HTML document
     */
    fun getElementByClass(className: String, html: String): String? {
        val retval = getElementsByClass(className, html)
        return if (retval.isNotEmpty()) retval[0] else null
    }

    fun getElementByAttribute(attribute: String, value: String, html: String, escapeValue: Boolean = true): String? {
        val retval = getElementsByAttribute(attribute, value, html, escapeValue)
        return if (retval.isNotEmpty()) retval[0] else null
    }

    /**
     * Return the content of all tags with the specified class in the passed HTML document as a list
     */
    fun getElementsByClass(className: String, html: String): List<String> {
        return getElementsByAttribute(
                "class", escape("""[^\'"]*\b$className\b[^\'"]*"""),
                html, escapeValue = false
        )
    }

    /**
     * Return the content of the tag with the specified attribute in the passed HTML document
     */
    fun getElementsByAttribute(attribute: String, value: String, html: String, escapeValue: Boolean = true)
            : List<String> {
        val attrValue = if (escapeValue) escape(value) else value
        val retList = mutableListOf<String>()
        """(?xs)
        <([a-zA-Z0-9:._-]+)
         (?:\s+[a-zA-Z0-9:._-]+(?:=[a-zA-Z0-9:._-]*|="[^"]*"|='[^']*'|))*?
         \s+${escape(attribute)}=['"]?$attrValue['"]?
         (?:\s+[a-zA-Z0-9:._-]+(?:=[a-zA-Z0-9:._-]*|="[^"]*"|='[^']*'|))*?
        \s*>
        \\(.*?)
        </\1>
        """.toRegex().findAll(html).forEach { m ->
            m.groups.last()?.value?.also { content ->
                var res = content
                if (res.startsWith('"') || res.startsWith("'"))
                    res = res.substring(1, res.lastIndex)

                retList.add(unescapeHtml(res))
            }
        }

        return retList
    }

    /**
     * Escape all the characters in pattern except ASCII letters, numbers and '_'.
     */
    fun escape(pattern: String): String {
        return pattern.replace("[^A-Za-z0-9_]".toRegex()) {
            if (it.value == "\u0000")
                "\\000"
            else
                "\\${it.value}"
        }
    }

    fun removeQuotes(s: String?): String? {
        if (s.isNullOrEmpty() || s.length < 2)
            return s
        for (quote in listOf('"', "'")) {
            if (s.first() == quote && s.last() == quote)
                return s.substring(1, s.lastIndex)
        }
        return s
    }

    fun unquote(s: String?): String? {
        val noQuote = removeQuotes(s)
        if (noQuote.isNullOrBlank())
            return noQuote
        return noQuote.replace("\\\"", "\"")
    }

    fun mimetype2ext(mt: String?): String? {
        if (mt.isNullOrBlank())
            return null

        val ext = hashMapOf(
                "audio/mp4" to "m4a",
                /*# Per RFC 3003, audio/mpeg can be .mp1, .mp2 or .mp3. Here use .mp3 as
                # it's the most popular one*/
                "audio/mpeg" to "mp3"
        )[mt]
        if (ext != null)
            return ext
        val res = mt.substringAfterLast("/").split(";")[0].trim().toLowerCase()

        return hashMapOf(
                "3gpp" to "3gp",
                "smptett+xml" to "tt",
                "ttaf+xml" to "dfxp",
                "ttml+xml" to "ttml",
                "x-flv" to "flv",
                "x-mp4-fragmented" to "mp4",
                "x-ms-sami" to "sami",
                "x-ms-wmv" to "wmv",
                "mpegurl" to "m3u8",
                "x-mpegurl" to "m3u8",
                "vnd.apple.mpegurl" to "m3u8",
                "dash+xml" to "mpd",
                "f4m+xml" to "f4m",
                "hds+xml" to "f4m",
                "vnd.ms-sstr+xml" to "ism",
                "quicktime" to "mov",
                "mp2t" to "ts"
        )[res] ?: res
    }

    fun parseCodecs(codecsStr: String?): HashMap<String, Any?> {
        // http://tools.ietf.org/html/rfc6381
        if (codecsStr.isNullOrBlank())
            return hashMapOf()
        val splitedCodecs = mutableListOf<String?>()
                .apply {
                    addAll(codecsStr.trim().trim(',').split(","))
                }
                .filter { !it.isNullOrBlank() }
                .map { it?.trim() as String }
        var (vcodec, acodec) = listOf<String?>(null, null)
        for (fullCodec in splitedCodecs) {
            val codec = fullCodec.split(".")[0]
            if (listOf("avc1", "avc2", "avc3", "avc4", "vp9", "vp8", "hev1", "hev2", "h263", "h264",
                            "mp4v", "hvc1", "av01", "theora").contains(codec)) {
                if (vcodec.isNullOrBlank())
                    vcodec = fullCodec
            } else if (listOf("mp4a", "opus", "vorbis", "mp3", "aac", "ac-3", "ec-3", "eac3", "dtsc",
                            "dtse", "dtsh", "dtsl").contains(codec)) {
                if (acodec.isNullOrBlank())
                    acodec = fullCodec
            } else {
                // TODO write_string('WARNING: Unknown codec %s\n' % full_codec, sys.stderr)
                // TODO Log.w(javaClass.name, "WARNING: Unknown codec $fullCodec")
            }
        }
        if (vcodec.isNullOrBlank() && acodec.isNullOrBlank()) {
            if (splitedCodecs.count() == 2)
                return hashMapOf(
                        "vcodec" to splitedCodecs[0],
                        "acodec" to splitedCodecs[1]
                )
        } else {
            return hashMapOf(
                    "vcodec" to vcodec,
                    "acodec" to acodec
            )
        }
        return hashMapOf()
    }

    fun isVcodec(codec: String?): Boolean {
        if (codec.isNullOrBlank())
            return false
        val mCodec = codec.split(".")[0]
        return listOf("avc1", "avc2", "avc3", "avc4", "vp9", "vp8", "hev1", "hev2", "h263", "h264",
                "mp4v", "hvc1", "av01", "theora").contains(mCodec)
    }

    fun isAcodec(codec: String?): Boolean {
        if (codec.isNullOrBlank())
            return false
        val mCodec = codec.split(".")[0]
        return listOf("mp4a", "opus", "vorbis", "mp3", "aac", "ac-3", "ec-3", "eac3", "dtsc",
                "dtse", "dtsh", "dtsl").contains(mCodec)
    }

    fun parseM3u8Attributes(attrib: String): HashMap<String, Any?> {
        val info = hashMapOf<String, Any?>()
        val mObjs = """([A-Z0-9-]+)=("[^"]+"|[^",]+)(?:,|${'$'})""".toRegex().findAll(attrib)
        for (mObj in mObjs) {
            val key = mObj.groups[1]?.value
            var value = mObj.groups[2]?.value
            if (value != null && value.startsWith('"'))
                value = value.substring(1, value.lastIndex)
            if (key != null)
                info[key] = value
        }
        return info
    }

    /**
     * Return a string with the date in the format YYYYMMDD if possible
     */
    fun unifiedStrDate(dateStr: String?, dayFirst: Boolean = true): String? {
        if (dateStr.isNullOrBlank())
            return null
        //var uploadDate: String? = null
        // Replace commas
        var mDateStr = dateStr.replace(",", " ")
        // Remove AM/PM + timezone
        mDateStr = mDateStr.replace("""(?i)\s*(?:AM|PM)(?:\s+[A-Z]+)?""".toRegex(), "")
        extractTimezone(mDateStr)?.let {
            mDateStr = it
        }

        for (expression in dateFormats(dayFirst)) {
            try {
                val srcFormat = SimpleDateFormat(expression, Locale.US)
                val targetFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
                return targetFormat.format(srcFormat.parse(mDateStr))
            } catch (e: Exception) {
            }
        }

        // TODO if uploadDate is null, parse mDateStr to timetuple

        return mDateStr
    }

    fun extractTimezone(dateStr: String): String? {
        val m = """^.{8,}?(Z${'$'}| ?([+\-])([0-9]{2}):?([0-9]{2})${'$'})"""
                .toRegex().find(dateStr)
        return if (m != null) {
            val len = m.groups[1]?.value?.length
            if (len != null)
                dateStr.substring(0, dateStr.length - len)
            else
                null
        } else
            null
        // TODO return timezone object as well
    }

    fun dateFormats(dayFirst: Boolean = true) = if (dayFirst) DATE_FORMATS_DAY_FIRST else DATE_FORMATS_MONTH_FIRST

    fun parseDuration(s: Any?): Float? {
        if (s !is String)
            return null

        val mS = s.trim()

        var m = """(?:(?:(?:([0-9]+):)?([0-9]+):)?([0-9]+):)?([0-9]+)(\.[0-9]+)?Z?${'$'}"""
                .toRegex().find(mS)

        if (m != null && mS.startsWith(m.value)) {
            val (_, days, hours, mins, secs, ms) = m.groupValues
            return totalDuration(days, hours, mins, secs, ms)
        } else {
            m = """(?ix)(?:P?
                (?:
                    [0-9]+\s*y(?:ears?)?\s*
                )?
                (?:
                    [0-9]+\s*m(?:onths?)?\s*
                )?
                (?:
                    [0-9]+\s*w(?:eeks?)?\s*
                )?
                (?:
                    ([0-9]+)\s*d(?:ays?)?\s*
                )?
                T)?
                (?:
                    ([0-9]+)\s*h(?:ours?)?\s*
                )?
                (?:
                    ([0-9]+)\s*m(?:in(?:ute)?s?)?\s*
                )?
                (?:
                    ([0-9]+)(\.[0-9]+)?\s*s(?:ec(?:ond)?s?)?\s*
                )?Z?${'$'}""".toRegex().find(mS)

            return if (m != null && mS.startsWith(m.value)) {
                val (_, days, hours, mins, secs, ms) = m.groupValues
                totalDuration(days, hours, mins, secs, ms)
            } else {
                m = """(?i)(?:([0-9.]+)\s*(?:hours?)|([0-9.]+)\s*(?:mins?\.?|minutes?)\s*)Z?${'$'}"""
                        .toRegex().find(mS)
                if (m != null && mS.startsWith(m.value)) {
                    val (_, hours, mins) = m.groupValues
                    totalDuration("", hours, mins, "", "")
                } else
                    null
            }
        }
    }

    private fun totalDuration(days: String, hours: String, mins: String, secs: String, ms: String): Float {
        var duration = 0f
        if (secs.isNotBlank()) duration += secs.toFloat()
        if (mins.isNotBlank()) duration += (mins.toFloat() * 60)
        if (hours.isNotBlank()) duration += (hours.toFloat() * 60 * 60)
        if (days.isNotBlank()) duration += (days.toFloat() * 24 * 60 * 60)
        if (ms.isNotBlank()) duration += ms.toFloat()
        return duration
    }

    fun stringToInt(s: String?): Int? {
        if (s.isNullOrBlank())
            return null
        return s.replace("""[,.+]""".toRegex(), "").toIntOrNull()
    }

    fun urlOrNull(url: String?): String? {
        if (url.isNullOrBlank())
            return null
        return removeQuotes(url.trim())?.let {
            val m = """^(?:[a-zA-Z][\da-zA-Z.+-]*:)?//""".toRegex().find(it)
            if (m != null && it.startsWith(m.value))
                it
            else
                null
        }
    }

    fun intOrNull(s: String?): Int? {
        if (s.isNullOrBlank())
            return null
        return removeQuotes(s.trim())?.toIntOrNull()
    }

    fun floatOrNull(s: String?): Float? {
        if (s.isNullOrBlank())
            return null
        return removeQuotes(s.trim())?.toFloatOrNull()
    }

    fun stringOrNull(s: String?): String? {
        if (s.isNullOrBlank())
            return null
        return removeQuotes(s.trim())
    }

    fun unescapeJsString(str: String?): String? {
        if (str == null) return null
        var s = str
        s = s.replace("\\'", "'")
        s = s.replace("\\\"", "\"")
        s = s.replace("\\\\", "\\")
        s = s.replace("\\b", "\b")
        s = s.replace("\\f", "\\u000C")
        s = s.replace("\\n", "\n")
        s = s.replace("\\r", "\r")
        s = s.replace("\\t", "\t")
        return s
    }

    fun baseUrl(url: String): String? {
        val m = """https?://[^?#&]+/""".toRegex().find(url)?.value
        return if (m != null && url.startsWith(m)) m else null
    }

    fun throwCancel() {
        throw ExtractorCanceledException()
    }
}

private operator fun List<String>.component6() = this[5]

val DATE_FORMATS = listOf(
        "dd MMMM yyyy",
        "dd MMM yyyy",
        "MMMM dd yyyy",
        "MMMM ddst yyyy",
        "MMMM ddnd yyyy",
        "MMMM ddth yyyy",
        "MMM dd yyyy",
        "MMM ddst yyyy",
        "MMM ddnd yyyy",
        "MMM ddth yyyy",
        "MMM ddst yyyy hh:mm",
        "MMM ddnd yyyy hh:mm",
        "MMM ddth yyyy hh:mm",
        "yyyy MM dd",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "yyyy/MM/dd HH:mm",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        "dd.MM.yyyy HH:mm",
        "dd.MM.yyyy HH.mm",
        "yyyy-MM-ddTH:mm:ssZ",
        "yyyy-MM-ddTH:mm:ss.SSSSSSZ",
        "yyyy-MM-ddTH:mm:ss.SSSSSS0Z",
        "yyyy-MM-ddTH:mm:ss",
        "yyyy-MM-ddTH:mm:ss.SSSSSS",
        "yyyy-MM-ddTH:mm",
        "MMM dd yyyy at HH:mm",
        "MMM dd yyyy at HH:mm:ss",
        "MMMM dd yyyy at HH:mm",
        "MMMM dd yyyy at HH:mm:ss")

val DATE_FORMATS_DAY_FIRST = mutableListOf<String>().apply {
    addAll(DATE_FORMATS)
    addAll(listOf("dd-MM-yyyy", "dd.MM.yyyy", "dd.MM.yy", "dd/MM/yyyy", "dd/MM/yy", "dd/MM/yyyy HH:mm:ss"))
}.toList()

val DATE_FORMATS_MONTH_FIRST = mutableListOf<String>().apply {
    addAll(DATE_FORMATS)
    addAll(listOf("MM-dd-yyyy", "MM.dd.yyyy", "MM/dd/yyyy", "MM/dd/yy", "MM/dd/yyyy HH:mm:ss"))
}

const val HTTP_ERROR_429 = "YOUTUBE HTTP ERROR 429"