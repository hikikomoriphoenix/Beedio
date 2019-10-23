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

import org.xmlpull.v1.XmlPullParser.*
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class XMLObject private constructor() {
    lateinit var tag: String; private set
    private val _attributes = mutableMapOf<String, String>()
    private val _children = mutableListOf<XMLObject>()
    var text: String? = null; private set
    private var parent: XMLObject? = null

    companion object {
        fun from(xmlString: String): XMLObject? {
            try {
                val xmlFactory = XmlPullParserFactory.newInstance()
                xmlFactory.isNamespaceAware = true
                val xmlParser = xmlFactory.newPullParser()

                xmlParser.setInput(StringReader(xmlString))
                var eventType = xmlParser.eventType
                var isRoot = true
                var curr = XMLObject()
                val root = curr
                var parent: XMLObject? = null
                while (eventType != END_DOCUMENT) {
                    when (eventType) {
                        START_TAG -> {
                            if (!isRoot) {
                                curr = XMLObject()
                                parent?._children?.add(curr)
                                curr.parent = parent
                                parent = curr
                            } else {
                                parent = curr
                                isRoot = false
                            }

                            curr.tag = xmlParser.name
                            for (i in 0 until xmlParser.attributeCount) {
                                curr._attributes[xmlParser.getAttributeName(i)] = xmlParser.getAttributeValue(i)
                            }
                        }
                        TEXT -> curr.text = xmlParser.text
                        END_TAG -> {
                            parent = parent?.parent
                        }
                    }
                    eventType = xmlParser.next()
                }
                return root
            } catch (e: XmlPullParserException) {
                return null
            }
        }
    }

    fun clear() {
        _attributes.clear()
        _children.clear()
        text = null
    }

    fun copyOfAttribs(): MutableMap<String, String> = mutableMapOf<String, String>().apply {
        putAll(_attributes)
    }

    fun get(key: String, default: String? = null) = _attributes[key] ?: default

    fun items() = _attributes.toMap()

    fun keys() = _attributes.keys

    operator fun set(key: String, value: String) {
        _attributes[key] = value
    }

    fun find(match: String, namespaces: String? = null) = _children.firstOrNull { it.tag == match }

    fun findAll(match: String, namespaces: String? = null) = _children.filter { it.tag == match }

    fun findText(match: String, default: String? = null, namespaces: String? = null): String? {
        val first = _children.firstOrNull { it.tag == match } ?: return default
        return first.text ?: ""
    }

    fun remove(child: XMLObject) {
        _children.remove(child)
    }
}