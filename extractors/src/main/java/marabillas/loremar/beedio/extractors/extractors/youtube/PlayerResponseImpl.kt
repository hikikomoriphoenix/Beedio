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
import marabillas.loremar.beedio.extractors.ExtractorUtils

class PlayerResponseImpl(json: String) : PlayerResponse {

    override val streamingData: StreamingData
        get() = _streamingData
    override val videoDetails: HashMap<String, String>
        get() = _videoDetails
    override val playabilityStatus: HashMap<String, String>
        get() = _playabilityStatus

    private val jsonObject = JsonParser.parseString(json).asJsonObject
    private var _streamingData = StreamingData(listOf(), listOf(), listOf(), listOf(), listOf())
    private val _videoDetails = hashMapOf<String, String>()
    private val _playabilityStatus = hashMapOf<String, String>()

    init {
        val streamData = jsonObject.getAsJsonObject("streamingData")
        val dashMUrl = streamData?.get("dashManifestUrl")
        val hlsMUrl = streamData?.get("hlsManifestUrl")
        val formats = streamData?.get("formats")?.asJsonArray
        val adaptiveFormats = streamData?.get("adaptiveFormats")?.asJsonArray
        val lInfos = streamData?.get("licenseInfos")


        val dashMUrlList = dashMUrl?.let { ExtractorUtils.jsonElementToStringList(it) }
        val hlsMUrlList = hlsMUrl?.let { ExtractorUtils.jsonElementToStringList(it) }
        val licenseInfosList = lInfos?.let { ExtractorUtils.jsonElementToStringList(it) }

        val formatsList = formats?.let { formatsArray ->
            mutableListOf<Map<String, String>>().also { list ->
                formatsArray.forEach { format ->
                    list.add(
                            format.asJsonObject.keySet().associateBy(
                                    { it },
                                    { format.asJsonObject.get(it).toString() }
                            )
                    )
                }
            }
        }

        val adaptiveFormatsList = adaptiveFormats?.let { formatsArray ->
            mutableListOf<Map<String, String>>().also { list ->
                formatsArray.forEach { format ->
                    list.add(
                            format.asJsonObject.keySet().associateBy(
                                    { it },
                                    { format.asJsonObject.get(it).toString() }
                            )
                    )
                }
            }
        }

        _streamingData = StreamingData(dashMUrlList, hlsMUrlList, formatsList?.toList(), adaptiveFormatsList, licenseInfosList)

        val detailsData = jsonObject.getAsJsonObject("videoDetails")
        detailsData?.keySet()?.forEach { key ->
            detailsData[key]?.let { value ->
                _videoDetails[key] = value.toString()
            }
        }

        val playabilityStatusData = jsonObject.getAsJsonObject("playabilityStatus")
        playabilityStatusData?.keySet()?.forEach { key ->
            playabilityStatusData[key]?.let { value ->
                _playabilityStatus[key] = value.toString()
            }
        }
    }
}