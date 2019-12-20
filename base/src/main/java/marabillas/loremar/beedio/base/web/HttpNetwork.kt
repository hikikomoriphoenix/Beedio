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

package marabillas.loremar.beedio.base.web

import android.os.Build
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.net.URLConnection

class HttpNetwork {
    private lateinit var okhttp: OkHttpClient

    init {
        if (Build.VERSION.SDK_INT >= 21) {
            okhttp = OkHttpClient()
        }
    }

    fun open(url: String): Connection {
        return if (Build.VERSION.SDK_INT >= 21)
            OkHttpConnection(url)
        else
            BasicConnection(url)
    }

    inner class OkHttpConnection(url: String) : Connection {

        private val response: Response
        private val headers: Headers

        init {
            val request = Request.Builder().url(url).build()
            response = okhttp.newCall(request).execute()
            headers = response.headers
        }

        override fun getResponseHeader(name: String): String? = headers[name]

        override val content: String? get() = response.body?.string()

        override val stream: InputStream? get() = response.body?.byteStream()

        override val urlHandler: URLHandler by lazy {
            object : URLHandler {

                override fun toString(): String = this.url ?: ""

                override val url: String? get() = response.request.url.toString()

                override val protocol: String? get() = response.protocol.toString()

                override val host: String? get() = response.request.url.host

                override val query: String? get() = response.request.url.query

                override val ref: String? get() = response.request.url.fragment

                override val port: Int? get() = response.request.url.port
            }
        }

        override fun close() {
            response.close()
        }
    }

    inner class BasicConnection(url: String) : Connection {

        private val urlConn: URLConnection = URL(url).openConnection().apply { connect() }
        private val reader: Reader? by lazy { urlConn.getInputStream()?.reader() }

        override fun getResponseHeader(name: String): String? = urlConn.getHeaderField(name)

        override val content: String? by lazy { urlConn.getInputStream()?.reader()?.readText() }

        override val stream: InputStream? get() = urlConn.getInputStream()

        override val urlHandler: URLHandler by lazy {
            object : URLHandler {

                override fun toString(): String = this.url ?: ""

                override val url: String? get() = urlConn.url.toString()

                override val protocol: String? get() = urlConn.url.protocol

                override val host: String? get() = urlConn.url.host

                override val query: String? get() = urlConn.url.query

                override val ref: String? get() = urlConn.url.ref

                override val port: Int? get() = urlConn.url.port
            }
        }

        override fun close() {
            reader?.close()
        }
    }

    interface Connection {
        fun getResponseHeader(name: String): String?

        val content: String?

        val stream: InputStream?

        val urlHandler: URLHandler

        fun close()
    }

    interface URLHandler {
        val url: String?

        val protocol: String?

        val host: String?

        val query: String?

        val ref: String?

        val port: Int?
    }
}