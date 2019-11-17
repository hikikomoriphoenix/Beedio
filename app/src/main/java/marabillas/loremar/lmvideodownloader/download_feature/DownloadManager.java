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

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import marabillas.loremar.lmvideodownloader.LMvdApp;
import marabillas.loremar.lmvideodownloader.download_feature.lists.CompletedVideos;
import marabillas.loremar.lmvideodownloader.download_feature.lists.DownloadQueues;
import marabillas.loremar.lmvideodownloader.download_feature.lists.InactiveDownloads;

public class DownloadManager extends IntentService {
    private static File downloadFile = null;
    private static long prevDownloaded = 0;
    private static long downloadSpeed = 0;
    private static long totalSize = 0;

    private static boolean chunked;
    private static ByteArrayOutputStream bytesOfChunk;

    private static DownloadNotifier downloadNotifier;

    private static String downloadFolder;

    private static boolean stop = false;
    private static Thread downloadThread;

    public DownloadManager() {
        super("DownloadManager");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        stop = false;
        downloadThread = Thread.currentThread();
        downloadNotifier = new DownloadNotifier(intent);

        if (intent != null) {
            chunked = intent.getBooleanExtra("chunked", false);

            if (chunked) {
                downloadFile = null;
                prevDownloaded = 0;
                downloadSpeed = 0;
                totalSize = 0;
                handleChunkedDownload(intent);
            } else {
                prevDownloaded = 0;
                URLConnection connection;
                try {
                    totalSize = Long.parseLong(intent.getStringExtra("size"));
                    connection = (new URL(intent.getStringExtra("link"))).openConnection();
                    String filename = intent.getStringExtra("name") + "." + intent.getStringExtra("type");

                    File directory = prepareTargetDirectory();
                    if (!directory.getAbsolutePath().endsWith("/")) {
                        downloadFolder = directory.getAbsolutePath() + "/";
                    } else {
                        downloadFolder = directory.getAbsolutePath();
                    }
                    boolean directotryExists = directory.exists() || directory.mkdir() || directory
                            .createNewFile();
                    if (directotryExists) {
                        downloadNotifier.notifyDownloading();
                        downloadFile = new File(directory, filename);
                        if (connection != null) {
                            FileOutputStream out = null;
                            if (downloadFile.exists()) {
                                prevDownloaded = downloadFile.length();
                                connection.setRequestProperty("Range", "bytes=" + downloadFile.length
                                        () + "-");
                                connection.connect();

                                out = new FileOutputStream(downloadFile.getAbsolutePath(), true);
                            } else {
                                connection.connect();
                                if (downloadFile.createNewFile()) {
                                    out = new FileOutputStream(downloadFile.getAbsolutePath(), true);
                                }
                            }
                            if (out != null && downloadFile.exists()) {
                                InputStream in = connection.getInputStream();
                                ReadableByteChannel readableByteChannel = Channels.newChannel(in);
                                FileChannel fileChannel = out.getChannel();
                                while (downloadFile.length() < totalSize) {
                                    if (stop) return;
                                    fileChannel.transferFrom(readableByteChannel, 0, 1024);
                                /*ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                                int read = readableByteChannel.read(buffer);
                                if (read!=-1) {
                                    buffer.flip();
                                    writableByteChannel.write(buffer);
                                }
                                else break;*/
                                    if (downloadFile == null) return;
                                }
                                readableByteChannel.close();
                                in.close();
                                out.flush();
                                out.close();
                                fileChannel.close();
                                //writableByteChannel.close();
                                downloadFinished(filename);
                            }
                        }
                    } else {
                        Log.e("loremarTest", "Can't create Download directory.");
                        onDownloadFailExceptionListener.onDownloadFailException("Can't create Download directory.");
                    }
                } catch (FileNotFoundException e) {
                    Log.i("loremarTest", "link:" + intent.getStringExtra("link") + " not found");
                    linkNotFound(intent);
                } catch (IOException e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e("loremarTest", sw.toString());
                    onDownloadFailExceptionListener.onDownloadFailException(sw.toString());
                } catch (DownloadFailException e) {
                    Log.e("loremarTest", e.getMessage());
                    onDownloadFailExceptionListener.onDownloadFailException(e.getMessage());
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e("loremarTest", sw.toString());
                    onDownloadFailExceptionListener.onDownloadFailException(sw.toString());
                }
            }
        }
    }

    private void downloadFinished(String filename) {
        downloadNotifier.notifyDownloadFinished();
        if (onDownloadFinishedListener != null) {
            onDownloadFinishedListener.onDownloadFinished();
        } else {
            DownloadQueues queues = DownloadQueues.load(getApplicationContext());
            queues.deleteTopVideo(getApplicationContext());
            CompletedVideos completedVideos = CompletedVideos.load
                    (getApplicationContext());
            completedVideos.addVideo(getApplicationContext(), filename);

            DownloadVideo topVideo = queues.getTopVideo();
            if (topVideo != null) {
                Intent downloadService = LMvdApp.getInstance().getDownloadService();
                downloadService.putExtra("link", topVideo.link);
                downloadService.putExtra("name", topVideo.name);
                downloadService.putExtra("type", topVideo.type);
                downloadService.putExtra("size", topVideo.size);
                downloadService.putExtra("page", topVideo.page);
                downloadService.putExtra("chunked", topVideo.chunked);
                downloadService.putExtra("website", topVideo.website);
                onHandleIntent(downloadService);
            }
        }
    }

    private void linkNotFound(Intent intent) {
        downloadNotifier.cancel();
        if (onLinkNotFoundListener != null) {
            onLinkNotFoundListener.onLinkNotFound();
        } else {
            DownloadQueues queues = DownloadQueues.load(getApplicationContext());
            queues.deleteTopVideo(getApplicationContext());
            DownloadVideo inactiveDownload = new DownloadVideo();
            inactiveDownload.name = intent.getStringExtra("name");
            inactiveDownload.link = intent.getStringExtra("link");
            inactiveDownload.type = intent.getStringExtra("type");
            inactiveDownload.size = intent.getStringExtra("size");
            inactiveDownload.page = intent.getStringExtra("page");
            inactiveDownload.website = intent.getStringExtra("website");
            inactiveDownload.chunked = intent.getBooleanExtra("chunked", false);
            InactiveDownloads inactiveDownloads = InactiveDownloads.load(getApplicationContext());
            inactiveDownloads.add(getApplicationContext(), inactiveDownload);

            DownloadVideo topVideo = queues.getTopVideo();
            if (topVideo != null) {
                Intent downloadService = LMvdApp.getInstance().getDownloadService();
                downloadService.putExtra("link", topVideo.link);
                downloadService.putExtra("name", topVideo.name);
                downloadService.putExtra("type", topVideo.type);
                downloadService.putExtra("size", topVideo.size);
                downloadService.putExtra("page", topVideo.page);
                downloadService.putExtra("chunked", topVideo.chunked);
                downloadService.putExtra("website", topVideo.website);
                onHandleIntent(downloadService);
            }
        }
    }

    private void handleChunkedDownload(Intent intent) {
        try {
            String name = intent.getStringExtra("name");
            String type = intent.getStringExtra("type");

            File directory = prepareTargetDirectory();
            if (directory == null) {
                throw new DownloadFailException("File returned by prepareTargetDirectory() is " +
                        "null... IMPOSSIBLE!");
            }
            if (!directory.getAbsolutePath().endsWith("/")) {
                downloadFolder = directory.getAbsolutePath() + "/";
            } else {
                downloadFolder = directory.getAbsolutePath();
            }
            boolean directotryExists = directory.exists() || directory.mkdir() || directory
                    .createNewFile();
            if (directotryExists) {
                downloadNotifier.notifyDownloading();
                File progressFile = new File(getCacheDir(), name + ".dat");
                File videoFile = new File(directory, name + "." + type);
                long totalChunks = 0;
                if (progressFile.exists()) {
                    FileInputStream in = new FileInputStream(progressFile);
                    DataInputStream data = new DataInputStream(in);
                    totalChunks = data.readLong();
                    data.close();
                    in.close();

                    if (!videoFile.exists()) {
                        if (!videoFile.createNewFile()) {
                            throw new DownloadFailException("Can't create file for download.");
                        }
                    }
                } else if (videoFile.exists()) {
                    downloadFinished(name + "." + type);
                } else {
                    if (!videoFile.createNewFile()) {
                        throw new DownloadFailException("Can't create file for download.");
                    }
                    if (!progressFile.createNewFile()) {
                        throw new DownloadFailException("Can't create progress file.");
                    }
                }

                if (videoFile.exists() && progressFile.exists()) {
                    while (true) {
                        prevDownloaded = 0;
                        String website = intent.getStringExtra("website");
                        String chunkUrl = null;
                        switch (website) {
                            case "dailymotion.com":
                                chunkUrl = getNextChunkWithDailymotionRule(intent, totalChunks);
                                break;
                            case "vimeo.com":
                                chunkUrl = getNextChunkWithVimeoRule(intent, totalChunks);
                                break;
                            case "twitter.com":
                                chunkUrl = getNextChunkWithM3U8Rule(intent, totalChunks);
                                break;
                            case "metacafe.com":
                                chunkUrl = getNextChunkWithM3U8Rule(intent, totalChunks);
                                break;
                            case "myspace.com":
                                chunkUrl = getNextChunkWithM3U8Rule(intent, totalChunks);
                                break;
                        }
                        if (chunkUrl == null) {
                            if (!progressFile.delete()) {
                                Log.i("loremarTest", "can't delete progressFile");
                            }
                            downloadFinished(name + "." + type);
                            break;
                        }
                        bytesOfChunk = new ByteArrayOutputStream();
                        try {
                            URLConnection uCon = new URL(chunkUrl).openConnection();
                            if (uCon != null) {
                                InputStream in = uCon.getInputStream();
                                ReadableByteChannel readableByteChannel = Channels.newChannel(in);
                                WritableByteChannel writableByteChannel = Channels.newChannel(bytesOfChunk);
                                int read;
                                while (true) {
                                    if (stop) return;

                                    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                                    read = readableByteChannel.read(buffer);
                                    if (read != -1) {
                                        buffer.flip();
                                        writableByteChannel.write(buffer);
                                    } else {
                                        FileOutputStream vAddChunk = new FileOutputStream
                                                (videoFile, true);
                                        vAddChunk.write(bytesOfChunk.toByteArray());
                                        FileOutputStream outputStream = new FileOutputStream
                                                (progressFile, false);
                                        DataOutputStream dataOutputStream = new DataOutputStream
                                                (outputStream);
                                        dataOutputStream.writeLong(++totalChunks);
                                        dataOutputStream.close();
                                        outputStream.close();
                                        Log.i("loremarTest", "downloaded chunk " + totalChunks +
                                                ": " + chunkUrl);
                                        break;
                                    }
                                }
                                readableByteChannel.close();
                                in.close();
                                bytesOfChunk.close();
                            }
                        } catch (FileNotFoundException e) {
                            if (!progressFile.delete()) {
                                Log.i("loremarTest", "can't delete progressFile");
                            }
                            downloadFinished(name + "." + type);
                            break;
                        } catch (IOException e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            Log.e("loremarTest", sw.toString());
                            onDownloadFailExceptionListener.onDownloadFailException(sw.toString());
                            break;
                        }
                    }
                }
            } else {
                Log.e("loremarTest", "Can't create Download directory.");
                onDownloadFailExceptionListener.onDownloadFailException("Can't create Download directory.");
            }
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e("loremarTest", sw.toString());
            onDownloadFailExceptionListener.onDownloadFailException(sw.toString());
        } catch (DownloadFailException e) {
            Log.e("loremarTest", e.getMessage());
            onDownloadFailExceptionListener.onDownloadFailException(e.getMessage());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e("loremarTest", sw.toString());
            onDownloadFailExceptionListener.onDownloadFailException(sw.toString());
        }
    }

    private String getNextChunkWithDailymotionRule(Intent intent, long totalChunks) {
        String link = intent.getStringExtra("link");
        return link.replaceAll("FRAGMENT", "frag(" + (totalChunks + 1) + ")");
    }

    private String getNextChunkWithVimeoRule(Intent intent, long totalChunks) {
        String link = intent.getStringExtra("link");
        return link.replaceAll("SEGMENT", "segment-" + (totalChunks + 1));
    }

    private String getNextChunkWithM3U8Rule(Intent intent, long totalChunks) {
        String link = intent.getStringExtra("link");
        String website = intent.getStringExtra("website");
        String line = null;
        try {
            URLConnection m3u8Con = new URL(link).openConnection();
            InputStream in = m3u8Con.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in);
            BufferedReader buffReader = new BufferedReader(inReader);
            while ((line = buffReader.readLine()) != null) {
                if ((website.equals("twitter.com") || website.equals("myspace.com")) && line
                        .endsWith(".ts")) {
                    break;
                } else if (website.equals("metacafe.com") && line.endsWith(".mp4")) {
                    break;
                }
            }
            if (line != null) {
                long l = 1;
                while (l < (totalChunks + 1)) {
                    buffReader.readLine();
                    line = buffReader.readLine();
                    l++;
                }
            }
            buffReader.close();
            inReader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (line != null) {
            String prefix;
            switch (website) {
                case "twitter.com":
                    Log.i("loremarTest", "downloading chunk " + (totalChunks + 1) + ": " +
                            "https://video.twimg.com" + line);
                    return "https://video.twimg.com" + line;
                case "metacafe.com":
                    prefix = link.substring(0, link.lastIndexOf("/") + 1);
                    Log.i("loremarTest", "downloading chunk " + (totalChunks + 1) + ": " + prefix +
                            line);
                    return prefix + line;
                case "myspace.com":
                    prefix = link.substring(0, link.lastIndexOf("/") + 1);
                    Log.i("loremarTest", "downloading chunk " + (totalChunks + 1) + ": " + prefix +
                            line);
                    return prefix + line;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public interface OnDownloadFinishedListener {
        void onDownloadFinished();
    }

    private static OnDownloadFinishedListener onDownloadFinishedListener;

    public static void setOnDownloadFinishedListener(OnDownloadFinishedListener listener) {
        onDownloadFinishedListener = listener;
    }


    public interface OnLinkNotFoundListener {
        void onLinkNotFound();
    }

    private static OnLinkNotFoundListener onLinkNotFoundListener;

    public static void setOnLinkNotFoundListener(OnLinkNotFoundListener listener) {
        onLinkNotFoundListener = listener;
    }

    @Override
    public void onDestroy() {
        downloadFile = null;
        Thread.currentThread().interrupt();
        super.onDestroy();
    }

    public static void stop() {
        if (downloadNotifier != null) {
            downloadNotifier.cancel();
        }

        Intent downloadService = LMvdApp.getInstance().getDownloadService();
        LMvdApp.getInstance().stopService(downloadService);
        forceStopIfNecessary();
    }

    public static void forceStopIfNecessary() {
        if (downloadThread != null && downloadThread.isAlive()) {
            stop = true;
        }
    }

    /**
     * Should be called every second
     *
     * @return download speed in bytes per second
     */
    public static long getDownloadSpeed() {
        if (!chunked) {
            if (downloadFile != null) {
                long downloaded = downloadFile.length();
                downloadSpeed = downloaded - prevDownloaded;
                prevDownloaded = downloaded;
                return downloadSpeed;
            }
            return 0;
        } else {
            if (bytesOfChunk != null) {
                long downloaded = bytesOfChunk.size();
                downloadSpeed = downloaded - prevDownloaded;
                prevDownloaded = downloaded;
                return downloadSpeed;
            }
            return 0;
        }
    }

    /**
     * @return remaining time to download video in milliseconds
     */
    public static long getRemaining() {
        if (!chunked && (downloadFile != null)) {
            long remainingLength = totalSize - prevDownloaded;
            return (1000 * (remainingLength / downloadSpeed));
        } else {
            return 0;
        }
    }

    private static File prepareTargetDirectory() throws DownloadFailException, IOException {
        File downloadFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (
                downloadFolder
                        != null
                        && (downloadFolder.exists() || downloadFolder.mkdir() || downloadFolder.createNewFile())
                        && downloadFolder.canWrite()
        ) {
            return downloadFolder;
        }

        File externalStorage = Environment.getExternalStorageDirectory();
        String externalStorageState = Environment.getExternalStorageState();
        if (
                externalStorage
                        != null
                        && (externalStorage.exists() || externalStorage.mkdir() || externalStorage.createNewFile())
                        && externalStorage.canWrite()
                        && externalStorageState.equals(Environment.MEDIA_MOUNTED)
        ) {
            return new File(externalStorage, "Download");
        }

        File appExternal = LMvdApp.getInstance().getExternalFilesDir(null);
        if (
                appExternal
                        != null
                        && (appExternal.exists() || appExternal.mkdir() || appExternal.createNewFile())
                        && appExternal.canWrite()
        ) {
            return new File(appExternal, "Download");
        }

        String message;
        switch (externalStorageState) {
            case Environment.MEDIA_UNMOUNTABLE:
                message = "External storage is un-mountable.";
                break;
            case Environment.MEDIA_SHARED:
                message = "USB mass storage is turned on. Can not mount external storage.";
                break;
            case Environment.MEDIA_UNMOUNTED:
                message = "External storage is not mounted.";
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                message = "External storage is mounted but has no write access.";
                break;
            case Environment.MEDIA_BAD_REMOVAL:
                message = "External storage was removed without being properly ejected.";
                break;
            case Environment.MEDIA_REMOVED:
                message = "External storage does not exist. Probably removed.";
                break;
            case Environment.MEDIA_NOFS:
                message = "External storage is blank or has unsupported filesystem.";
                break;
            case Environment.MEDIA_CHECKING:
                message = "Still checking for external storage.";
                break;
            case Environment.MEDIA_EJECTING:
                message = "External storage is currently being ejected.";
                break;
            case Environment.MEDIA_UNKNOWN:
                message = "External storage is not available for some unknown reason.";
                break;
            case Environment.MEDIA_MOUNTED:
                message = "External storage is mounted but for some unknown reason is not" +
                        " available.";
                break;
            default:
                message = "External storage is not available. No reason.";
        }
        throw new DownloadFailException(message);
    }

    public static String getDownloadFolder() {
        try {
            File directory = prepareTargetDirectory();
            if (!directory.getAbsolutePath().endsWith("/")) {
                downloadFolder = directory.getAbsolutePath() + "/";
            } else {
                downloadFolder = directory.getAbsolutePath();
            }
            return downloadFolder;
        } catch (DownloadFailException e) {
            Log.e("loremarTest", "No download directory: " + e.getMessage());
            new AlertDialog.Builder(LMvdApp.getInstance())
                    .setMessage("No downnload directory: " + e.getMessage())
                    .setNeutralButton("OK", null)
                    .create()
                    .show();
            return null;
        } catch (IOException e) {
            Log.e("loremarTest", e.getMessage());
            new AlertDialog.Builder(LMvdApp.getInstance())
                    .setMessage(e.getMessage())
                    .setNeutralButton("OK", null)
                    .create()
                    .show();
            return null;
        }
    }

    public interface onDownloadFailExceptionListener {
        void onDownloadFailException(String message);
    }

    private static onDownloadFailExceptionListener onDownloadFailExceptionListener;

    public static void setOnDownloadFailExceptionListener(onDownloadFailExceptionListener listener) {
        onDownloadFailExceptionListener = listener;
    }
}
