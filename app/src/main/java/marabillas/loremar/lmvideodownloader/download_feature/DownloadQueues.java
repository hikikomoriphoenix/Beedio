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

package marabillas.loremar.lmvideodownloader.download_feature;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DownloadQueues implements Serializable {
    private List<DownloadVideo> downloads;

    public DownloadQueues() {
        downloads = new ArrayList<>();
    }

    public void add(String size, String type, String link, String name, String page) {
        name = name.replaceAll("[^\\w ()'!\\[\\]\\-]", "");
        name = name.trim();
        if (name.equals("")) name = "video";
        int i = 0;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DOWNLOADS), name + "." + type);
        StringBuilder nameBuilder = new StringBuilder(name);
        while(file.exists()) {
            i++;
            nameBuilder = new StringBuilder(name);
            nameBuilder.append(" ").append(i);
            file = new File(Environment.getExternalStoragePublicDirectory(Environment
                    .DIRECTORY_DOWNLOADS), nameBuilder + "." + type);
        }
        while(nameAlreadyExists(nameBuilder.toString())) {
            i++;
            nameBuilder = new StringBuilder(name);
            nameBuilder.append(" ").append(i);
        }
        name = nameBuilder.toString();

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

    private boolean nameAlreadyExists(String name) {
        for(DownloadVideo video: downloads) {
            if(video.name.equals(name)) return true;
        }
        return false;
    }

    public void saveQueues(Context context) {
        try {
            File file = new File(context.getFilesDir(), "downloads.dat");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

