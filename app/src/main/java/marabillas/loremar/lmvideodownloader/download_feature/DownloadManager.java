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

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class DownloadManager extends IntentService {
    private static File downloadFile = null;
    private static long prevDownloaded = 0;
    private static long downloadSpeed = 0;
    private static long totalSize = 0;

    public DownloadManager() {
        super("DownloadManager");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        prevDownloaded = 0;
        URLConnection connection;
        try {
            if (intent != null) {
                totalSize = Long.parseLong(intent.getStringExtra("size"));
                connection = (new URL(intent.getStringExtra("link"))).openConnection();
                String filename = intent.getStringExtra("name") + "." + intent.getStringExtra("type");
                File directory = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath(), "Download");

                boolean directotryExists;
                directotryExists = directory.exists() || directory.mkdir() || directory
                        .createNewFile();
                if (directotryExists) {
                    downloadFile = new File(Environment.getExternalStoragePublicDirectory(Environment
                            .DIRECTORY_DOWNLOADS), filename);
                    if (connection != null) {
                        FileOutputStream out = null;
                        if (downloadFile.exists()) {
                            prevDownloaded = downloadFile.length();
                            connection.setRequestProperty("Range", "bytes=" + downloadFile.length
                                    () + "-");
                            connection.connect();
                            out = new FileOutputStream(downloadFile.getAbsolutePath(),true);
                        } else {
                            connection.connect();
                            if (downloadFile.createNewFile()) {
                                out = new FileOutputStream(downloadFile.getAbsolutePath(),true);
                            }
                        }
                        if (out != null && downloadFile.exists()) {
                            InputStream in = connection.getInputStream();
                            ReadableByteChannel readableByteChannel = Channels.newChannel(in);
                            FileChannel fileChannel = out.getChannel();
                            while(downloadFile.length() < totalSize) {
                                if (Thread.currentThread().isInterrupted()) return;
                                fileChannel.transferFrom(readableByteChannel, 0, 1024);
                                /*ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                                int read = readableByteChannel.read(buffer);
                                if (read!=-1) {
                                    buffer.flip();
                                    writableByteChannel.write(buffer);
                                }
                                else break;*/
                                if (downloadFile==null) return;
                            }
                            readableByteChannel.close();
                            in.close();
                            out.flush();
                            out.close();
                            fileChannel.close();
                            //writableByteChannel.close();
                            onDownloadFinishedListener.onDownloadFinished();
                        }
                    }
                }
                else {
                    Log.e("loremarTest", "directory doesn't exist");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface OnDownloadFinishedListener {
        void onDownloadFinished();
    }

    private static OnDownloadFinishedListener onDownloadFinishedListener;

    static void setOnDownloadFinishedListener(OnDownloadFinishedListener listener) {
        onDownloadFinishedListener = listener;
    }

    @Override
    public void onDestroy() {
        downloadFile = null;
        Thread.currentThread().interrupt();
        super.onDestroy();
    }

    static void stopThread() {
        Thread.currentThread().interrupt();
    }

    /**
     * Should be called every second
     * @return download speed in bytes per second
     */
    static long getDownloadSpeed() {
        if (downloadFile!=null) {
            long downloaded = downloadFile.length();
            downloadSpeed = downloaded - prevDownloaded;
            prevDownloaded = downloaded;
            return downloadSpeed;
        }
        return 0;
    }

    /**
     *
     * @return remaining time to download video in milliseconds
     */
    static long getRemaining() {
        long remainingLength = totalSize - prevDownloaded;
        return (1000 * (remainingLength / downloadSpeed));
    }
}
