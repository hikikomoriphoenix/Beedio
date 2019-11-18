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

package marabillas.loremar.lmvideodownloader.browsing_feature;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayDeque;
import java.util.Queue;

final class VideoDetectionInitiator {
    private Queue<VideoSearch> reservedSearches = new ArrayDeque<>();
    private Handler handler;
    private BrowserWindow.ConcreteVideoContentSearch videoContentSearch;

    VideoDetectionInitiator(BrowserWindow.ConcreteVideoContentSearch videoContentSearch) {
        HandlerThread thread = new HandlerThread("Video Detect Thread");
        thread.start();
        handler = new Handler(thread.getLooper());

        this.videoContentSearch = videoContentSearch;
    }

    void reserve(String url, String page, String title) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.url = url;
        videoSearch.page = page;
        videoSearch.title = title;
        reservedSearches.add(videoSearch);
    }

    void initiate() {
        try {
            while (reservedSearches.size() != 0) {
                VideoSearch search = reservedSearches.remove();
                videoContentSearch.newSearch(search.url, search.page, search.title);
                handler.post(videoContentSearch);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    void clear() {
        handler.getLooper().quit();
        HandlerThread thread = new HandlerThread("Video Detect Thread");
        thread.start();
        handler = new Handler(thread.getLooper());
        reservedSearches.clear();
    }

    class VideoSearch {
        String url;
        String page;
        String title;
    }
}
