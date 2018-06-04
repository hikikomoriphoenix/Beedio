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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;

import marabillas.loremar.lmvideodownloader.LMvdApp;
import marabillas.loremar.lmvideodownloader.R;

public class DownloadNotifier {
    private final int ID = 77777;
    private Intent downloadServiceIntent;
    private Handler handler;
    private NotificationManager notificationManager;
    private DownloadingRunnable downloadingRunnable;

    private class DownloadingRunnable implements Runnable {
        @Override
        public void run() {
            String filename = downloadServiceIntent.getStringExtra("name") + "." +
                    downloadServiceIntent.getStringExtra("type");
            Notification.Builder NB;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NB = new Notification.Builder(LMvdApp.getInstance().getApplicationContext(), "download_01");
            } else {
                NB = new Notification.Builder(LMvdApp.getInstance().getApplicationContext());
            }
            NB.setContentTitle("Downloading " + filename)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setLargeIcon(BitmapFactory.decodeResource(LMvdApp.getInstance()
                            .getApplicationContext().getResources(), R
                            .drawable
                            .ic_launcher_foreground))
                    .setOngoing(true);
            if (downloadServiceIntent.getBooleanExtra("chunked", false)) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DOWNLOADS), filename);
                String downloaded;
                if (file.exists()) {
                    downloaded = android.text.format.Formatter.formatFileSize(LMvdApp.getInstance
                            ().getApplicationContext(), file.length());
                } else {
                    downloaded = "0KB";
                }
                NB.setProgress(100, 0, true)
                        .setContentText(downloaded)
                        .setStyle(new Notification.BigTextStyle());
                notificationManager.notify(ID, NB.build());
                handler.postDelayed(this, 1000);
            } else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DOWNLOADS), filename);
                String sizeString = downloadServiceIntent.getStringExtra("size");
                int progress = (int) Math.ceil(((double) file.length() / (double) Long.parseLong
                        (sizeString)) * 100);
                progress = progress >= 100 ? 100 : progress;
                String downloaded = android.text.format.Formatter.formatFileSize(LMvdApp
                        .getInstance().getApplicationContext(), file.length());
                String total = android.text.format.Formatter.formatFileSize(LMvdApp.getInstance()
                        .getApplicationContext(), Long.parseLong
                        (sizeString));
                NB.setProgress(100, progress, false)
                        .setContentText(downloaded + "/" + total + "   " + progress + "%")
                        .setStyle(new Notification.BigTextStyle());
                notificationManager.notify(ID, NB.build());
                handler.postDelayed(this, 1000);
            }
        }
    }

    DownloadNotifier(Intent downloadServiceIntent) {
        this.downloadServiceIntent = downloadServiceIntent;
        notificationManager = (NotificationManager) LMvdApp.getInstance().getApplicationContext().getSystemService
                (Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("download_01",
                    "Download Notification", NotificationManager.IMPORTANCE_DEFAULT));
            notificationManager.createNotificationChannel(new NotificationChannel("download_02",
                    "Download Notification", NotificationManager.IMPORTANCE_HIGH));
        }
        HandlerThread thread = new HandlerThread("downloadNotificationThread");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    void notifyDownloading() {
        downloadingRunnable = new DownloadingRunnable();
        downloadingRunnable.run();
    }

    void notifyDownloadFinished() {
        handler.removeCallbacks(downloadingRunnable);
        notificationManager.cancel(ID);

        String filename = downloadServiceIntent.getStringExtra("name") + "." +
                downloadServiceIntent.getStringExtra("type");
        Notification.Builder NB;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NB = new Notification.Builder(LMvdApp.getInstance().getApplicationContext(), "download_02")
                    .setTimeoutAfter(1500);
        } else {
            NB = new Notification.Builder(LMvdApp.getInstance().getApplicationContext());
        }
        NB.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(LMvdApp.getInstance().getApplicationContext().getResources(),
                        R.drawable
                                .ic_launcher_foreground))
                .setTicker("Download Finished")
                .setContentTitle("Download Finished")
                .setContentText(filename)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);
        notificationManager.notify(8888, NB.build());
    }

    void cancel() {
        if (downloadingRunnable != null) {
            handler.removeCallbacks(downloadingRunnable);
        }
        notificationManager.cancel(ID);
    }
}
