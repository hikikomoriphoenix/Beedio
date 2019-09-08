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

package marabillas.loremar.lmvideodownloader.download_feature.lists;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadVideo;

public class DownloadQueues implements Serializable {
    private List<DownloadVideo> downloads;

    public DownloadQueues() {
        downloads = new ArrayList<>();
    }

    public static DownloadQueues load(Context context) {
        File file = new File(context.getFilesDir(), "downloads.dat");
        DownloadQueues queues = new DownloadQueues();
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                queues = (DownloadQueues) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        return queues;
    }

    public void save(Context context) {
        try {
            File file = new File(context.getFilesDir(), "downloads.dat");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertToTop(String size, String type, String link, String name, String page, boolean
            chunked, String website) {
        name = getValidName(name, type);

        DownloadVideo video = new DownloadVideo();
        video.link = link;
        video.name = name;
        video.page = page;
        video.size = size;
        video.type = type;
        video.chunked = chunked;
        video.website = website;
        downloads.add(0, video);
    }

    public void add(String size, String type, String link, String name, String page, boolean
            chunked, String website) {
        name = getValidName(name, type);

        DownloadVideo video = new DownloadVideo();
        video.link = link;
        video.name = name;
        video.page = page;
        video.size = size;
        video.type = type;
        video.chunked = chunked;
        video.website = website;
        downloads.add(video);
    }

    private String getValidName(String name, String type) {
        name = name.replaceAll("[^\\w ()'!\\[\\]\\-]", "");
        name = name.trim();
        if (name.length() > 127) {//allowed filename length is 127
            name = name.substring(0, 127);
        } else if (name.equals("")) {
            name = "video";
        }
        int i = 0;
        File file = new File(DownloadManager.getDownloadFolder(), name + "." + type);
        StringBuilder nameBuilder = new StringBuilder(name);
        while (file.exists()) {
            i++;
            nameBuilder = new StringBuilder(name);
            nameBuilder.append(" ").append(i);
            file = new File(DownloadManager.getDownloadFolder(), nameBuilder + "." + type);
        }
        while (nameAlreadyExists(nameBuilder.toString())) {
            i++;
            nameBuilder = new StringBuilder(name);
            nameBuilder.append(" ").append(i);
        }
        return nameBuilder.toString();
    }

    public List<DownloadVideo> getList() {
        return downloads;
    }

    public DownloadVideo getTopVideo() {
        if (downloads.size() > 0) {
            return downloads.get(0);
        } else {
            return null;
        }
    }

    public void deleteTopVideo(Context context) {
        if (downloads.size() > 0) {
            downloads.remove(0);
            save(context);
        }
    }

    private boolean nameAlreadyExists(String name) {
        for (DownloadVideo video : downloads) {
            if (video.name.equals(name)) return true;
        }
        return false;
    }

    public void renameItem(int index, String newName) {
        if (!downloads.get(index).name.equals(newName)) {
            downloads.get(index).name = getValidName(newName, downloads.get(index).type);
        }
    }
}

