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

import android.app.Application;
import android.content.Intent;

import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;

public class LMvdApp extends Application {
    private static LMvdApp instance;
    private Intent downloadService;
    private LMvdActivity.OnBackPressedListener onBackPressedListener;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        downloadService = new Intent(getApplicationContext(), DownloadManager.class);
    }

    public Intent getDownloadService() {
        return downloadService;
    }

    public static LMvdApp getInstance() {
        return instance;
    }

    public LMvdActivity.OnBackPressedListener getOnBackPressedListener() {
        return onBackPressedListener;
    }

    public void setOnBackPressedListener(LMvdActivity.OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }
}
