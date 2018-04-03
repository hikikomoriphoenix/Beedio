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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class DownloadQueues implements Serializable {
    private List<DownloadVideo> downloads;

    DownloadQueues() {
        downloads = new ArrayList<>();
    }

    void add(String size, String type, String link, String name, String page) {
        DownloadVideo video = new DownloadVideo();
        video.link = link;
        video.name = name;
        video.page = page;
        video.size = size;
        video.type = type;
        downloads.add(video);
    }

    List<DownloadVideo> getList() {
        return  downloads;
    }
}

