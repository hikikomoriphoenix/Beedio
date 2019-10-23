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

package marabillas.loremar.beedio.extractors.extractors.youtube

import com.google.gson.JsonParser
import marabillas.loremar.beedio.extractors.ExtractorUtils.jsonElementToStringList

class YtplayerConfigImpl(json: String) : YtplayerConfig {

    override val args: Args?
        get() = _args
    override val sts: String?
        get() = _sts

    private val jsonObject = JsonParser.parseString(json).asJsonObject
    private var _args: Args? = null
    private var _sts: String? = null

    init {
        _sts = jsonObject.getAsJsonPrimitive("sts")?.asString

        val argsObj = jsonObject.getAsJsonObject("args")
        val urlEncodedFmtStreamMap = argsObj.get("url_encoded_fmt_stream_map")
        val hlsvp = argsObj.get("hslvp")
        val dashMpd = argsObj.get("dashmpd")
        val ypcVid = argsObj.get("ypc_vid")?.asString
        val livestream = argsObj.get("livestream")?.asString
        val livePlayback = argsObj.get("live_playback")?.asInt
        val playerResponse = argsObj.get("player_response")?.asString
        _args = Args(
                urlEncodedFmtStreamMap = urlEncodedFmtStreamMap?.let { jsonElementToStringList(urlEncodedFmtStreamMap) }
                        ?: listOf(),
                hlsvp = hlsvp?.let { jsonElementToStringList(it) } ?: listOf(),
                dashMpd = dashMpd?.let { jsonElementToStringList(it) } ?: listOf(),
                ypcVid = ypcVid,
                livestream = livestream,
                livePlayback = livePlayback,
                playerResponse = playerResponse
        )
    }

    override fun getArgsItems(): Map<String, List<String>> {
        return mutableMapOf<String, List<String>>().apply {
            val argsObj = jsonObject.getAsJsonObject("args")
            for (key in argsObj.keySet()) {
                put(key, listOf(argsObj[key].asString))
            }
        }
    }
}