/*
 *     LM videodownloader is a browser app for android, made to easily
 *     download videos.
 *     Copyright (C) 2018 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.lmvideodownloader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoStreamingSitesList extends RecyclerView.Adapter<VideoStreamingSitesList.VideoStreamingSiteItem> {
    private List<Site> sites;
    private LMvdActivity activity;

    class Site {
        int drawable;
        String title;
        String url;

        Site(int drawable, String title, String url) {
            this.drawable = drawable;
            this.title = title;
            this.url = url;
        }
    }

    VideoStreamingSitesList(LMvdActivity activity) {
        this.activity = activity;
        sites = new ArrayList<>();
        sites.add(new Site(R.drawable.favicon_youtube, "youtube", "https://m.youtube.com"));
        sites.add(new Site(R.drawable.favicon_facebook, "facebook", "https://m.facebook.com"));
        sites.add(new Site(R.drawable.favicon_instagram, "instagram", "https://www.instagram" +
                ".com"));
        sites.add(new Site(R.drawable.favicon_twitter, "twitter", "https://mobile.twitter.com"));
        sites.add(new Site(R.drawable.favicon_dailymotion, "dailymotion", "https://www" +
                ".dailymotion.com"));
        sites.add(new Site(R.drawable.favicon_veoh, "veoh", "https://www.veoh.com"));
        sites.add(new Site(R.drawable.favicon_vimeo, "vimeo", "https://vimeo.com"));
        sites.add(new Site(R.drawable.favicon_vk, "vk", "https://m.vk.com"));
        sites.add(new Site(R.drawable.favicon_fc2, "fc2", "https://video.fc2.com"));
        sites.add(new Site(R.drawable.favicon_vlive, "vlive", "https://m.vlive.tv"));
        sites.add(new Site(R.drawable.favicon_naver, "naver", "https://m.tv.naver.com"));
        sites.add(new Site(R.drawable.favicon_metacafe, "metacafe", "https://www.metacafe.com"));
        sites.add(new Site(R.drawable.favicon_tudou, "tudou", "https://www.tudou.com"));
        sites.add(new Site(R.drawable.favicon_youku, "youku", "https://m.youku.com"));
        sites.add(new Site(R.drawable.favicon_myspace, "myspace", "https://myspace.com"));
        sites.add(new Site(R.drawable.favicon_vine, "vine", "https://vine.co"));
        sites.add(new Site(R.drawable.favicon_tumblr, "tumblr", "https://www.tumblr.com"));
    }

    @NonNull
    @Override
    public VideoStreamingSiteItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(LMvdApp.getInstance().getApplicationContext
                ());
        return new VideoStreamingSiteItem(inflater.inflate(R.layout.video_site, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoStreamingSiteItem holder, int position) {
        holder.bind(sites.get(position));
    }

    @Override
    public int getItemCount() {
        return sites.size();
    }

    class VideoStreamingSiteItem extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;

        VideoStreamingSiteItem(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.videoSiteIcon);
            title = itemView.findViewById(R.id.videoSiteTitle);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.getBrowserManager().newWindow(sites.get(getAdapterPosition()).url);
                }
            });
        }

        void bind(Site site) {
            icon.setImageDrawable(LMvdApp.getInstance().getResources().getDrawable(site.drawable));
            title.setText(site.title);
        }
    }
}
