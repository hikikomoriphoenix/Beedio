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

package marabillas.loremar.beedio.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.base.extensions.drawable
import javax.inject.Inject

class HomeRecommendedAdapter @Inject constructor() :
        RecyclerView.Adapter<HomeRecommendedAdapter.HomeRecommendedViewHolder>() {

    var onWebsiteSelectedListener: OnWebsiteSelectedListener? = null

    private val websites = listOf(
            "youtube.com" to R.drawable.logo_youtube,
            "facebook.com" to R.drawable.logo_facebook,
            "twitter.com" to R.drawable.logo_twitter,
            "twitch.tv" to R.drawable.logo_twitch,
            "instagram.com" to R.drawable.logo_instagram,
            "www.dailymotion.com" to R.drawable.logo_dailymotion,
            "www.veoh.com" to R.drawable.logo_veoh,
            "vimeo.com/watch" to R.drawable.logo_vimeo,
            "ted.com" to R.drawable.logo_ted,
            "bitchute.com" to R.drawable.logo_bitchute,
            "archive.org" to R.drawable.logo_internet_archive,
            "tiktok.com" to R.drawable.logo_tiktok,
            "metacafe.com" to R.drawable.logo_metacafe,
            "9gag.com" to R.drawable.logo_9gag,
            "vlive.tv" to R.drawable.logo_vlive,
            "tv.naver.com" to R.drawable.logo_navertv,
            "afreecatv.com" to R.drawable.logo_afreecatv,
            "rutube.ru" to R.drawable.logo_rutube,
            "m.vk.com" to R.drawable.logo_vk,
            "m.youku.com" to R.drawable.logo_youku,
            "bilibili.com" to R.drawable.logo_bilibili,
            "www.tudou.com" to R.drawable.logo_tudou,
            "video.fc2.com" to R.drawable.logo_fc2)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecommendedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_recommended, parent, false)
        return HomeRecommendedViewHolder(view)
    }

    override fun getItemCount(): Int {
        return websites.count()
    }

    override fun onBindViewHolder(holder: HomeRecommendedViewHolder, position: Int) {
        val website = websites[position]
        holder.bind(website.first, website.second)
    }

    inner class HomeRecommendedViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var _url: String? = null

        init {
            itemView.setOnClickListener {
                _url?.let {
                    onWebsiteSelectedListener?.onWebsiteSelected(it)
                }
            }
        }

        fun bind(url: String, drawableId: Int) {
            val drawable = itemView.resources.drawable(drawableId)
            itemView.findViewById<ImageView>(R.id.image_recommended_site_logo)
                    ?.setImageDrawable(drawable)
            _url = url
        }
    }

    interface OnWebsiteSelectedListener {
        fun onWebsiteSelected(url: String)
    }
}