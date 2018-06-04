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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;

public class DownloadNotifier {
    private final int ID = 77777;
    private Notification notification;
    private Intent downloadServiceIntent;
    private Context context;
    private NotificationManager notificationManager;
    private DownloadingRunnable downloadingRunnable;

    private class DownloadingRunnable implements Runnable {
        @Override
        public void run() {
            String filename = downloadServiceIntent.getStringExtra("name") + "." +
                    downloadServiceIntent.getStringExtra("type");
            Notification.Builder NB = new Notification.Builder(context)
                    .setContentTitle("Downloading " + filename)
                    .setOngoing(true);
            if (downloadServiceIntent.getBooleanExtra("chunked", false)) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DOWNLOADS), filename);
                String sizeString = downloadServiceIntent.getStringExtra("size");
                int progress = (int) Math.ceil((file.length() / Long.parseLong(sizeString)) * 100);
                progress = progress >= 100 ? 100 : progress;
                NB.setProgress(100, progress, false);
            } else {
                NB.setProgress(100, 0, true);
            }
            notificationManager.notify(ID, NB.build());
            new Handler(((HandlerThread) Thread.currentThread()).getLooper()).postDelayed(this, 1000);
        }
    }

    DownloadNotifier(Context context, Intent downloadServiceIntent) {
        this.context = context;
        this.downloadServiceIntent = downloadServiceIntent;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    void notifyDownloading() {
        downloadingRunnable = new DownloadingRunnable();
        downloadingRunnable.run();
    }

    void notifyDownloadFinished() {
        new Handler(((HandlerThread) Thread.currentThread()).getLooper()).removeCallbacks(downloadingRunnable);
    }
}
