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

import marabillas.loremar.beedio.extractors.ExtractorUtils.baseUrl
import marabillas.loremar.beedio.extractors.ExtractorUtils.extractResponseFrom
import marabillas.loremar.beedio.extractors.ExtractorUtils.floatOrNull
import marabillas.loremar.beedio.extractors.ExtractorUtils.intOrNull
import marabillas.loremar.beedio.extractors.ExtractorUtils.mimetype2ext
import marabillas.loremar.beedio.extractors.ExtractorUtils.parseCodecs
import marabillas.loremar.beedio.extractors.ExtractorUtils.parseDuration
import marabillas.loremar.beedio.extractors.ExtractorUtils.parseM3u8Attributes
import okhttp3.Response
import java.net.URL
import kotlin.math.ceil

abstract class InfoExtractor {

    fun initialize() {
        // TODO
    }

    abstract fun extract(url: String): Map<String, Any?>

    protected abstract fun realExtract(url: String): Map<String, Any?>

    fun urlResult(url: String, ie: String? = null, videoId: String? = null, videoTitle: String? = null)
            : HashMap<String, List<String>> {
        /*Returns a URL that points to a page that should be processed*/
        // TODO ie should be the class used for getting the info
        return hashMapOf(
                "_type" to listOf("url"),
                "url" to listOf(url)
        ).apply {
            ie?.let { put("ie_key", listOf(it)) }
            videoId?.let { put("id", listOf(it)) }
            videoTitle?.let { put("title", listOf(it)) }
        }
    }

    /**
     * Regex search a string and return the first matching group value
     */
    fun searchRegex(pattern: Regex, string: String, group: Int? = null): String? {
        val mobj = pattern.find(string)
        if (mobj != null) {
            if (group == null) {
                for (i in 1..mobj.groupValues.lastIndex) {
                    if (mobj.groupValues[i].isNotEmpty())
                        return mobj.groupValues[i]
                }
            } else {
                return mobj.groupValues[group]
            }
        }
        return null
    }

    fun htmlSearchMeta(name: String, html: String, displayName: String? = null, fatal: Boolean = false)
            : String? {
        return htmlSearchMeta(listOf(name), html, displayName, fatal)
    }

    fun htmlSearchMeta(name: List<String>, html: String, displayName: String? = null, fatal: Boolean = false)
            : String? {
        var dispName = displayName
        if (dispName == null)
            dispName = name[0]
        return htmlSearchRegex(metaRegex(name[0]), html, 3) // 4 is content group
    }

    fun htmlSearchRegex(pattern: Regex, s: String, group: Int? = null): String? {
        return searchRegex(pattern, s, group)?.let {
            ExtractorUtils.cleanHtml(it).trim()
        }
    }

    fun metaRegex(prop: String): Regex = """(?isx)<meta
                    (?=[^>]+(?:itemprop|name|property|id|http-equiv)=(["']?)${ExtractorUtils.escape(prop)}\1)
                    [^>]+?content=(["'])(.*?)\2""".toRegex()

    fun extractM3u8Formats(m3u8Url: String, videoId: String, ext: String? = null, entryProtocol: String = "m3u8",
                           preference: String? = null, m3u8Id: String? = null, not: String? = null, errnote: String? = null,
                           fatal: Boolean = true, live: Boolean = false)
            : List<HashMap<String, Any?>> {
        val res = ExtractorUtils.extractResponseFrom(m3u8Url) ?: return listOf()

        val m3u8Doc = res.body.toString()
        val urlh = res.request
        val m3u8Urlx = urlh.url.toString()

        return parseM3u8Formats(m3u8Doc, m3u8Urlx,
                ext = ext, entryProtocol = entryProtocol, preference = preference, m3u8Id = m3u8Id,
                live = live)
    }

    fun parseM3u8Formats(m3u8Doc: String, m3u8Url: String, ext: String? = null, entryProtocol: String? = "m3u8",
                         preference: String? = null, m3u8Id: String? = null, live: Boolean = false)
            : List<HashMap<String, Any?>> {
        if (m3u8Doc.contains("#EXT-X-FAXS-CM:"))
            return listOf()
        if ("""#EXT-X-SESSION-KEY:.*?URI="skd://""".toRegex().find(m3u8Doc) != null)
            return listOf()

        val formats = mutableListOf<HashMap<String, Any?>>()

        val formatUrl: (u: String) -> String = {
            val mObj = """^https?://""".toRegex().find(it)
            if (mObj != null && it.startsWith(mObj.value))
                it
            else
                URL(URL(m3u8Url), it).toString()
        }

        /*# References:
        # 1. https://tools.ietf.org/html/draft-pantos-http-live-streaming-21
        # 2. https://github.com/ytdl-org/youtube-dl/issues/12211
        # 3. https://github.com/ytdl-org/youtube-dl/issues/18923

        # We should try extracting formats only from master playlists [1, 4.3.4],
        # i.e. playlists that describe available qualities. On the other hand
        # media playlists [1, 4.3.3] should be returned as is since they contain
        # just the media without qualities renditions.
        # Fortunately, master playlist can be easily distinguished from media
        # playlist based on particular tags availability. As of [1, 4.3.3, 4.3.4]
        # master playlist tags MUST NOT appear in a media playist and vice versa.
        # As of [1, 4.3.3.1] #EXT-X-TARGETDURATION tag is REQUIRED for every
        # media playlist and MUST NOT appear in master playlist thus we can
        # clearly detect media playlist with this criterion.*/

        if (m3u8Doc.contains("#EXT-X-TARGETDURATION"))
            return listOf(hashMapOf(
                    "url" to m3u8Url as Any?,
                    "format_id" to m3u8Id as Any?,
                    "ext" to ext as Any?,
                    "protocol" to entryProtocol as Any?,
                    "preference" to preference as Any?
            ))

        val groups = hashMapOf<String, MutableList<HashMap<String, Any?>>>()
        var lastStreamInf = hashMapOf<String, Any?>()

        fun extracMedia(xMediaLine: String) {
            val media = parseM3u8Attributes(xMediaLine)
            // As per [1, 4.3.4.1] TYPE, GROUP-ID and NAME are REQUIRED
            val mediaType = media["TYPE"]
            val groupId = media["GROUP-ID"]
            val name = media["NAME"]
            if (mediaType == null && groupId == null && name == null)
                return
            if (groupId is String) {
                groups.getOrPut(groupId, { mutableListOf() }).add(media)
            }
            if (mediaType != "VIDEO" && mediaType != "AUDIO")
                return
            val mediaUrl = media["URI"]
            if (mediaUrl != null) {
                val formatId = mutableListOf<Any?>()
                for (v in listOf(m3u8Id, groupId, name)) {
                    if (v != null)
                        formatId.add(v)
                }
                val f = hashMapOf<String, Any?>().apply {
                    put("format_id", formatId.joinToString("-"))
                    if (mediaUrl is String)
                        put("url", formatUrl(mediaUrl))
                    put("manifest_url", m3u8Url)
                    put("language", media["LANGUAGE"])
                    put("ext", ext)
                    put("protocol", entryProtocol)
                    put("preference", preference)
                }
                if (mediaType == "AUDIO")
                    f["vcodec"] = null
                formats.add(f)
            }
        }

        fun buildStreamName(): String? {
            /*# Despite specification does not mention NAME attribute for
            # EXT-X-STREAM-INF tag it still sometimes may be present (see [1]
            # or vidio test in TestInfoExtractor.test_parse_m3u8_formats)
            # 1. http://www.vidio.com/watch/165683-dj_ambred-booyah-live-2015*/
            var streamName = lastStreamInf["NAME"]
            if (streamName is String && streamName.isNotBlank())
                return streamName
            /*# If there is no NAME in EXT-X-STREAM-INF it will be obtained
            # from corresponding rendition group*/
            val streamGroupId = lastStreamInf["VIDEO"]
            if (streamGroupId == null || (streamGroupId is String && streamGroupId.isBlank()))
                return null
            val streamGroup = groups[streamGroupId]
            if (streamGroup.isNullOrEmpty() && streamGroupId is String)
                return streamGroupId
            val rendition = streamGroup?.get(0)
            streamName = rendition?.get("NAME")
            return when {
                streamName is String -> streamName
                streamGroupId is String -> streamGroupId
                else -> null
            }
        }

        /*# parse EXT-X-MEDIA tags before EXT-X-STREAM-INF in order to have the
        # chance to detect video only formats when EXT-X-STREAM-INF tags
        # precede EXT-X-MEDIA tags in HLS manifest such as [3].*/
        for (line in m3u8Doc.split("""[\n\r]+""".toRegex()))
            if (line.startsWith("#EXT-X-MEDIA:"))
                extracMedia(line)

        for (line in m3u8Doc.split("""[\n\r]+""".toRegex())) {
            if (line.startsWith("#EXT-X-STREAM-INF:"))
                lastStreamInf = parseM3u8Attributes(line)
            else if (line.startsWith("#") || line.isBlank())
                continue
            else {
                val tbr = (lastStreamInf["AVERAGE-BANDWIDTH"] ?: lastStreamInf["BANDWIDTH"])?.let {
                    if (it is String) it.toFloat() / 1000f else null
                }
                val formatId = mutableListOf<String>()
                if (!m3u8Id.isNullOrBlank())
                    formatId.add(m3u8Id)
                val streamName = buildStreamName()
                /*# Bandwidth of live streams may differ over time thus making
                # format_id unpredictable. So it's better to keep provided
                # format_id intact.*/
                if (!live)
                    formatId.add(
                            if (!streamName.isNullOrBlank())
                                streamName
                            else
                                "${tbr ?: formats.count()}"
                    )
                val manifestUrl = formatUrl(line.trim())
                val f = hashMapOf(
                        "format_id" to formatId.joinToString("-"),
                        "url" to manifestUrl,
                        "manifest_url" to m3u8Url,
                        "tbr" to tbr,
                        "ext" to ext,
                        "protocol" to entryProtocol,
                        "preference" to preference
                ).apply {
                    val fps = lastStreamInf["FRAME-RATE"]
                    if (fps is String)
                        put("fps", fps.toFloat())
                }
                val resolution = lastStreamInf["RESOLUTION"]
                if (resolution is String && resolution.isNotBlank()) {
                    val mobj = """(\d+)[xX](\d+)""".toRegex().find(resolution)
                    if (mobj != null) {
                        f["width"] = mobj.groups[1]?.value?.toInt()
                        f["height"] = mobj.groups[2]?.value?.toInt()
                    }
                }
                // Unified Streaming Platform
                val mobj = """audio.*?(?:%3D|=)(\d+)(?:-video.*?(?:%3D|=)(\d+))?""".toRegex()
                        .find(f["url"].toString())
                if (mobj != null) {
                    val abr = mobj.groups[1]?.value?.toFloat()?.div(1000f)
                    val vbr = mobj.groups[2]?.value?.toFloat()?.div(1000f)
                    f.putAll(hashMapOf("vbr" to vbr, "abr" to abr))
                }
                val codecs = parseCodecs(lastStreamInf["CODECS"] as String?)
                f.putAll(codecs)
                val audioGroupId = lastStreamInf["AUDIO"]
                /*# As per [1, 4.3.4.1.1] any EXT-X-STREAM-INF tag which
                # references a rendition group MUST have a CODECS attribute.
                # However, this is not always respected, for example, [2]
                # contains EXT-X-STREAM-INF tag which references AUDIO
                # rendition group but does not have CODECS and despite
                # referencing an audio group it represents a complete
                # (with audio and video) format. So, for such cases we will
                # ignore references to rendition groups and treat them
                # as complete formats.*/
                val vcodec = f["vcodec"]
                if (audioGroupId is String && audioGroupId.isNotBlank() && !codecs.isNullOrEmpty()
                        && vcodec is String && vcodec.isNotBlank() && vcodec != "none") {
                    val audioGroup = groups[audioGroupId]
                    if (!audioGroup.isNullOrEmpty() && audioGroup[0]["URI"] != null) {
                        /*# TODO: update acodec for audio only formats with
                        # the same GROUP-ID*/
                        f["acodec"] = null
                    }
                    formats.add(f)
                    lastStreamInf = hashMapOf()
                }
            }
        }
        return formats
    }

    fun extractMpdFormats(mpdUrl: String, videoId: String, mpdId: String? = null, note: String? = null,
                          errnote: String? = null, fatal: Boolean = true,
                          formatsDict: HashMap<String, HashMap<String, Any>> = hashMapOf())
            : List<HashMap<String, Any?>> {
        val res = downloadXmlHandle(mpdUrl, videoId, note = note ?: "Downloading MPD manifest",
                errnote = errnote ?: "Failed to download MPD manifest", fatal = fatal)
                ?: return listOf()
        val (mpdDoc, urlh) = res
        if (mpdDoc == null) return listOf()
        val mpdBaseUrl = baseUrl(urlh.request.url.toString())

        return parseMpdFormats(mpdDoc, mpdId = mpdId, mpdBaseUrl = mpdBaseUrl, formatsDict = formatsDict,
                mpdUrl = mpdUrl)
    }

    /**
     * Parse formats from MPD manifest.
     * References:
     * 1. MPEG-DASH Standard, ISO/IEC 23009-1:2014(E),
     * http://standards.iso.org/ittf/PubliclyAvailableStandards/c065274_ISO_IEC_23009-1_2014.zip
     * 2. https://en.wikipedia.org/wiki/Dynamic_Adaptive_Streaming_over_HTTP
     */
    fun parseMpdFormats(mpdDoc: XMLObject, mpdId: String? = null, mpdBaseUrl: String? = null,
                        formatsDict: HashMap<String, HashMap<String, Any>>, mpdUrl: String? = null)
            : List<HashMap<String, Any?>> {
        var mMpdBaseUrl = mpdBaseUrl
        if (mpdDoc.get("type") == "dynamic")
            return listOf()

        val namespace = searchRegex("""(?i)^{([^}]+)?}MPD${'$'}""".toRegex(), mpdDoc.tag)

        val addNs: (String) -> String = { path -> xpathNs(path, namespace) }

        val isDrmProtected: (XMLObject) -> Boolean = { elem -> elem.find(addNs("ContentProtection")) != null }

        val extractMutliSegmentInfo: (XMLObject, HashMap<String, Any>) -> HashMap<String, Any> = { element: XMLObject, msParentInfo: HashMap<String, Any> ->
            val msInfo = hashMapOf<String, Any>().apply { putAll(msParentInfo) }

            /*# As per [1, 5.3.9.2.2] SegmentList and SegmentTemplate share some
            # common attributes and elements.  We will only extract relevant
            # for us.*/
            val extractCommon: (XMLObject) -> Unit = { source ->
                val segmentTimeline = source.find(addNs("SegmentTimeline"))
                if (segmentTimeline != null) {
                    val sE = segmentTimeline.findAll(addNs("S"))
                    if (!sE.isNullOrEmpty()) {
                        msInfo["total_number"] = 0
                        val sValue = mutableMapOf<String, Int>()
                        for (s in sE) {
                            val r = s.get("r", "0")?.toInt() ?: 0
                            msInfo["total_number"] = msInfo["total_number"] as Int + 1 + r
                            sValue.putAll(mapOf(
                                    "t" to (s.get("t")?.toInt() ?: 0),
                                    // @d is mandatory (see [1, 5.3.9.6.2, Table 17, page 60])
                                    "d" to (s.get("d")?.toInt() ?: 0),
                                    "r" to r
                            ))
                        }
                        msInfo["s"] = sValue
                    }
                }
                val startNumber = source.get("startNumber")
                if (!startNumber.isNullOrBlank())
                    msInfo["start_number"] = startNumber.toInt()
                val timescale = source.get("timescale")
                if (!timescale.isNullOrBlank())
                    msInfo["timescale"] = timescale.toInt()
                val segmentDuration = source.get("duration")
                if (!segmentDuration.isNullOrBlank())
                    msInfo["segment_duration"] = segmentDuration.toFloat()
            }

            val extractInitialization: (XMLObject) -> Unit = { source ->
                val initialization = source.find(addNs("Initialization"))
                if (initialization != null)
                    initialization.get("sourceURL")?.let {
                        msInfo["initialization_url"] = it
                    }
            }

            val segmentList = element.find(addNs("SegmentList"))
            if (segmentList != null) {
                extractCommon(segmentList)
                extractInitialization(segmentList)
                val segmentUrlsE = segmentList.findAll(addNs("SegmentURL"))
                if (!segmentUrlsE.isNullOrEmpty()) {
                    val segmentUrls = mutableListOf<String>()
                    for (segment in segmentUrlsE) {
                        segment.get("media")?.let { segmentUrls.add(it) }
                    }
                    msInfo["segment_urls"] = segmentUrls
                }
            } else {
                val segmentTemplate = element.find(addNs("SegmentTemplate"))
                if (segmentTemplate != null) {
                    extractCommon(segmentTemplate)
                    val media = segmentTemplate.get("media")
                    if (!media.isNullOrBlank()) {
                        msInfo["media"] = media
                    }
                    val initialization = segmentTemplate.get("initialization")
                    if (!initialization.isNullOrBlank())
                        msInfo["initialization"] = initialization
                    else
                        extractInitialization(segmentTemplate)
                }
            }
            msInfo
        }

        val mpdDuration = parseDuration(mpdDoc.get("mediaPresentationDuration"))
        val formats = mutableListOf<HashMap<String, Any?>>()
        for (period in mpdDoc.findAll(addNs("Period"))) {
            val periodDuration = parseDuration(period.get("duration")) ?: mpdDuration
            val periodMsInfo = extractMutliSegmentInfo(period, hashMapOf(
                    "start_number" to 1,
                    "timescale" to 1
            ))
            for (adaptationSet in period.findAll(addNs("AdaptationSet"))) {
                if (isDrmProtected(adaptationSet))
                    continue
                val adaptionSetMsInfo = extractMutliSegmentInfo(adaptationSet, periodMsInfo)
                for (representation in adaptationSet.findAll(addNs("Representation"))) {
                    if (isDrmProtected(representation))
                        continue
                    val representationAttrib = adaptationSet.copyOfAttribs()
                    representationAttrib.putAll(representationAttrib)
                    // According to [1, 5.3.7.2, Table 9, page 41], @mimeType is mandatory
                    val mimeType = representationAttrib["mimeType"]
                    val contentType = mimeType?.split("/")?.get(0)
                    if (contentType == "text") {
                        TODO("implement WebVTT downloading")
                    } else if (listOf("video", "audio").contains(contentType)) {
                        var baseUrl = ""
                        for (element in (listOf(representation, adaptationSet, period, mpdDoc))) {
                            val baseUrlE = element.find(addNs("BaseURL"))
                            if (baseUrlE != null) {
                                baseUrl = baseUrlE.text + baseUrl
                                val m = """^https?://""".toRegex().find(baseUrl)?.value
                                if (m != null && baseUrl.startsWith(m))
                                    break
                            }
                        }
                        val baseUrlM = """^https?://""".toRegex().find(baseUrl)?.value
                        val isBaseUrlM = baseUrlM != null && baseUrl.startsWith(baseUrlM)
                        if (!mMpdBaseUrl.isNullOrBlank() && !isBaseUrlM) {
                            if (!mMpdBaseUrl.endsWith("/") && !baseUrl.startsWith("/"))
                                mMpdBaseUrl += "/"
                            baseUrl = mMpdBaseUrl + baseUrl
                        }
                        val representationId = representationAttrib["id"]
                        val lang = representationAttrib["lang"]
                        val filesize = intOrNull(
                                representation.find(addNs("BaseURL"))
                                        ?.get("{http://youtube.com/yt/2012/10/10}contentLength"))
                        val bandwidth = intOrNull(representationAttrib["bandwidth"])
                        val f = hashMapOf<String, Any?>(
                                "format_id" to (
                                        if (!mpdId.isNullOrBlank())
                                            "$mpdId-$representationId"
                                        else
                                            representationId
                                        ),
                                "manifest_url" to mpdUrl,
                                "ext" to mimetype2ext(mimeType),
                                "width" to intOrNull(representationAttrib["width"]),
                                "height" to intOrNull(representationAttrib["height"]),
                                "tbr" to ((bandwidth?.toFloat())?.div(1000f)),
                                "asr" to intOrNull(representationAttrib["audioSamplingRate"]),
                                "fps" to intOrNull(representationAttrib["frameRate"]),
                                "language" to (
                                        if (listOf("mul", "und", "zxx", "mis").contains(lang))
                                            lang
                                        else null
                                        ),
                                "format_note" to "DASH $contentType",
                                "filesize" to filesize,
                                "container" to mimetype2ext(mimeType) + "_dash"
                        )
                        f.putAll(parseCodecs(representationAttrib["codecs"]))
                        val representationMsInfo = extractMutliSegmentInfo(representation, adaptionSetMsInfo)

                        val prepareTemplate: (String, List<String>) -> String = { templateName: String, identifiers: List<String> ->
                            val tmpl = representationMsInfo[templateName]
                            /*# First of, % characters outside $...$ templates
                            # must be escaped by doubling for proper processing
                            # by % operator string formatting used further (see
                            # https://github.com/ytdl-org/youtube-dl/issues/16867).*/
                            var t = ""
                            var inTemplate = false
                            if (tmpl is String) {
                                tmpl.forEach { c ->
                                    t += c
                                    if (c == '$')
                                        inTemplate = !inTemplate
                                    else if (c == '%' && !inTemplate)
                                        t += c
                                    /*# Next, $...$ templates are translated to their
                                    # %(...) counterparts to be used with % operator*/
                                    val joinedIdentifiers = identifiers.joinToString("|")
                                    representationId?.let { t = t.replace("${'$'}RepresentationID\${'$'}", it) }
                                    t = t.replace("""\\${'$'}($joinedIdentifiers)\\${'$'}""".toRegex(),
                                            """%($joinedIdentifiers)d""")
                                    t = t.replace("""\\${'$'}($joinedIdentifiers)%%([^${'$'}]+)\\${'$'}""".toRegex()) {
                                        """%(${it.groupValues[1]})${it.groupValues[2]}"""
                                    }
                                    t.replace("$$", "$")
                                }
                            }
                            TODO()
                        }

                        /*# @initialization is a regular template like @media one
                        # so it should be handled just the same way (see
                        # https://github.com/ytdl-org/youtube-dl/issues/11605)*/
                        if (representationMsInfo.contains("initialization")) {
                            val initializationTemplate = prepareTemplate(
                                    "initialization",
                                    /*# As per [1, 5.3.9.4.2, Table 15, page 54] $Number$ and
                                    # $Time$ shall not be included for @initialization thus
                                    # only $Bandwidth$ remains*/
                                    listOf("Bandwidth", ""))
                            representationMsInfo["initialization_url"] = initializationTemplate
                                    .replace("%(Bandwidth)s", bandwidth.toString())
                        }

                        val locationKey: (String) -> String = { location ->
                            val m = """^https?://""".toRegex().find(location)?.value
                            if (m != null && location.startsWith(m))
                                "url"
                            else
                                "path"
                        }

                        if (!representationMsInfo.contains("segment_urls") && representationMsInfo.contains("media")) {
                            val mediaTemplate = prepareTemplate("media", listOf("Number", "Bandwidth", "Time"))
                            val mediaLocationKey = locationKey(mediaTemplate)

                            // As per [1, 5.3.9.4.4, Table 16, page 55] $Number$ and $Time$
                            // Can't be used at the same time
                            if (mediaTemplate.contains("%(Number") && !representationMsInfo.contains("s")) {
                                var segmentDuration: Float? = null
                                if (!representationMsInfo.contains("total_number") && representationMsInfo.contains("segment_duration")) {
                                    segmentDuration = floatOrNull(representationMsInfo["segment_duration"]?.toString())
                                    segmentDuration = floatOrNull(representationMsInfo["timescale"]?.toString())?.let { ts ->
                                        segmentDuration?.let { sd -> sd / ts }
                                    }
                                    periodDuration?.let { pd ->
                                        segmentDuration?.let { sd ->
                                            ceil((pd / sd).toDouble())
                                        }?.let {
                                            representationMsInfo["total_number"] = it
                                        }
                                    }
                                }
                                val startNumber = intOrNull(representationMsInfo["start_number"]?.toString())
                                val totalNumber = intOrNull(representationMsInfo["total_number"]?.toString())
                                val endNumber = totalNumber?.let { t -> startNumber?.let { s -> t + s } }
                                startNumber?.let { start ->
                                    endNumber?.let { end ->
                                        val fragments = mutableListOf<HashMap<String, Any?>>()
                                        for (segmentNumber in start until end) {
                                            val mediaLocation = mediaTemplate.replace("%(Number)s", segmentNumber.toString()).run {
                                                replace("%(Bandwidth)s", bandwidth.toString())
                                            }
                                            fragments.add(hashMapOf(
                                                    mediaLocationKey to mediaLocation,
                                                    "duration" to segmentDuration
                                            ))
                                        }
                                        representationMsInfo["fragments"] = fragments
                                    }
                                }
                            } else {
                                /*# $Number*$ or $Time$ in media template with S list available
                                # Example $Number*$: http://www.svtplay.se/klipp/9023742/stopptid-om-bjorn-borg
                                # Example $Time$: https://play.arkena.com/embed/avp/v2/player/media/b41dda37-d8e7-4d3f-b1b5-9a9db578bdfe/1/129411*/
                                val fragments = mutableListOf<HashMap<String, Any?>>()
                                var segmentTime = 0.0
                                var segmentD: Int? = null
                                var segmentNumber = representationMsInfo["start_number"]?.toString()?.toInt()

                                val addSegmentUrl: () -> Unit = {
                                    val segmentUrl = mediaTemplate.replace("Time", segmentTime.toString())
                                            .run { replace("Bandwidth", bandwidth.toString()) }
                                            .run { replace("Number", segmentNumber.toString()) }

                                    val duration = floatOrNull(segmentD?.toString())?.let { segD ->
                                        representationMsInfo["timescale"]?.let { ts ->
                                            segD / ts.toString().toFloat()
                                        }
                                    }
                                    fragments.add(hashMapOf(
                                            mediaLocationKey to segmentUrl,
                                            "duration" to duration
                                    ))
                                    representationMsInfo["fragments"] = fragments
                                }

                                val ses = representationMsInfo["s"]
                                if (ses is List<*>) {
                                    ses.forEachIndexed { i, s ->
                                        if (s is HashMap<*, *>) {
                                            segmentTime = s["t"]?.toString()?.toDouble()
                                                    ?: segmentTime
                                            segmentD = s["d"]?.toString()?.toInt()
                                            addSegmentUrl()
                                            segmentNumber = segmentNumber?.plus(1)
                                            val range = s["r"]?.toString()?.toInt() ?: 0
                                            for (r in 0 until range) {
                                                segmentD?.let { segmentTime += it }
                                                addSegmentUrl()
                                                segmentNumber = segmentNumber?.plus(1)
                                            }
                                            segmentD?.let { segmentTime += it }
                                        }
                                    }
                                }
                            }
                        } else if (representationMsInfo.contains("segment_urls") && representationMsInfo.contains("s")) {
                            /* # No media template
                            # Example: https://www.youtube.com/watch?v=iXZV5uAYMJI
                            # or any YouTube dashsegments video*/
                            val fragments = mutableListOf<HashMap<String, Any?>>()
                            var segmentIndex = 0
                            val timescale = representationMsInfo["timescale"]?.toString()?.toFloat()
                            val ses = representationMsInfo["s"]
                            if (ses is List<*>) {
                                for (s in ses) {
                                    if (s is HashMap<*, *>) {
                                        val duration = timescale?.let { floatOrNull(s["d"]?.toString())?.div(it) }
                                        val range = s["r"]?.toString()?.toInt() ?: 0
                                        for (r in 0 until (range + 1)) {
                                            val segmentUrls = representationMsInfo["segment_urls"]
                                            if (segmentUrls is List<*>) {
                                                val segmentUri = segmentUrls[segmentIndex]
                                                fragments.add(hashMapOf(
                                                        locationKey(segmentUri.toString()) to segmentUri,
                                                        "duration" to duration
                                                ))
                                                segmentIndex += 1
                                            }
                                        }
                                        representationMsInfo["fragments"] = fragments
                                    }
                                }
                            }
                        } else if (representationMsInfo.contains("segment_urls")) {
                            /*# Segment URLs with no SegmentTimeline
                            # Example: https://www.seznam.cz/zpravy/clanek/cesko-zasahne-vitr-o-sile-vichrice-muze-byt-i-zivotu-nebezpecny-39091
                            # https://github.com/ytdl-org/youtube-dl/pull/14844*/
                            val fragments = mutableListOf<HashMap<String, Any?>>()
                            val timescale = if (representationMsInfo.contains("segment_duration"))
                                representationMsInfo["timescale"] else null
                            val segmentDuration = timescale?.let {
                                floatOrNull("segment_duration")?.div(it.toString().toFloat())
                            }
                            val segmentUrls = representationMsInfo["segment_urls"]
                            if (segmentUrls is List<*>) {
                                for (segmentUrl in segmentUrls) {
                                    val fragment = hashMapOf(
                                            locationKey(segmentUrl.toString()) to segmentUrl
                                    )
                                    if (segmentDuration != null)
                                        fragment["duration"] = segmentDuration
                                    fragments.add(fragment)
                                }
                            }
                            representationMsInfo["fragments"] = fragments
                        }

                        /*# If there is a fragments key available then we correctly recognized fragmented media.
                        # Otherwise we will assume unfragmented media with direct access. Technically, such
                        # assumption is not necessarily correct since we may simply have no support for
                        # some forms of fragmented media renditions yet, but for now we'll use this fallback.*/
                        if (representationMsInfo.contains("fragments")) {
                            f.putAll(hashMapOf(
                                    // NB: mpd_url may be empty when MPD manifest is parsed from a string
                                    "url" to (mpdUrl ?: baseUrl),
                                    "fragment_base_url" to baseUrl,
                                    "fragments" to listOf<HashMap<String, Any?>>(),
                                    "protocol" to "http_dash_segments"
                            ))
                            if (representationMsInfo.contains("initialization_url")) {
                                val initiallizationUrl = representationMsInfo["initialization_url"]
                                if (f["url"] == null)
                                    f["url"] = initiallizationUrl
                                val fragments = f["fragments"]
                                if (fragments is List<*>) {
                                    val mFragments = fragments.toMutableList().add(hashMapOf(
                                            locationKey(initiallizationUrl.toString()) to initiallizationUrl
                                    ))
                                    f["fragments"] = mFragments
                                }
                            }
                            val fragments = f["fragments"]
                            val rFragments = representationMsInfo["fragments"]
                            if (fragments is List<*> && rFragments is List<*>) {
                                val mFragments = fragments.toMutableList()
                                val mRFragments = rFragments.toMutableList()
                                mFragments.addAll(mRFragments)
                                f["fragments"] = mFragments
                            }
                        } else {
                            // Assuming direct URL to unfragmented media.
                            f["url"] = baseUrl
                        }

                        /*# According to [1, 5.3.5.2, Table 7, page 35] @id of Representation
                        # is not necessarily unique within a Period thus formats with
                        # the same `format_id` are quite possible. There are numerous examples
                        # of such manifests (see https://github.com/ytdl-org/youtube-dl/issues/15111,
                        # https://github.com/ytdl-org/youtube-dl/issues/13919)*/
                        val fullInfo = hashMapOf<String, Any?>().apply {
                            formatsDict[representationId]?.forEach {
                                put(it.key, it.value)
                            }
                        }
                        fullInfo.putAll(f)
                        formats.add(fullInfo)
                    } else {
                        // Log.w("Unknown MIME type $mimeType in DASH manifest")
                    }
                }
            }
        }
        return formats
    }

    fun downloadXmlHandle(url: String, videoId: String, note: String = "Downloading XML",
                          errnote: String = "Unable to download XML", transformSource: Any? = null,
                          fatal: Boolean = true, encoding: String? = null)
            : Pair<XMLObject?, Response>? {
        val res = extractResponseFrom(url) ?: return null
        return res.body?.let {
            Pair(parseXml(it.string()), res)
        }
    }

    fun parseXml(xmlString: String) = XMLObject.from(xmlString)

    fun xpathNs(path: String, nameSpace: String? = null): String {
        if (nameSpace.isNullOrBlank()) return path
        val out = mutableListOf<String>()
        for (c in path.split("/")) {
            if (c.isBlank() || c == ".")
                out.add(c)
            else
                out.add("{$nameSpace}$c")
        }
        return out.joinToString("/")
    }
}