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

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.PermissionRequestCodes;
import marabillas.loremar.lmvideodownloader.PermissionsManager;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.RenameDialog;
import marabillas.loremar.lmvideodownloader.Utils;

public class DownloadsInProgress extends LMvdFragment implements DownloadManager.OnDownloadFinishedListener, DownloadManager.OnLinkNotFoundListener, OnDownloadWithNewLinkListener {
    private List<DownloadVideo> downloads;
    private RecyclerView downloadsList;
    private DownloadQueues queues;
    private TextView downloadsStartPauseButton;

    private Tracking tracking;

    private OnAddDownloadedVideoToCompletedListener onAddDownloadedVideoToCompletedListener;
    private OnAddDownloadItemToInactiveListener onAddDownloadItemToInactiveListener;
    private OnNumDownloadsInProgressChangeListener onNumDownloadsInProgressChangeListener;

    public interface OnAddDownloadedVideoToCompletedListener {
        void onAddDownloadedVideoToCompleted(String name, String type);
    }

    public interface OnAddDownloadItemToInactiveListener {
        void onAddDownloadItemToInactive(DownloadVideo inactiveDownload);
    }

    public interface OnNumDownloadsInProgressChangeListener {
        void onNumDownloadsInProgressChange();
    }

    public int getNumDownloadsInProgress() {
        return downloads.size();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.downloads_in_progress, container, false);

        downloadsList = view.findViewById(R.id.downloadsList);
        downloadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        downloadsList.setAdapter(new DownloadListAdapter());
        downloadsList.setHasFixedSize(true);
        downloads = new ArrayList<>();
        File file = new File(getActivity().getFilesDir(), "downloads.dat");
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                queues = (DownloadQueues) objectInputStream.readObject();
                downloads = queues.getList();
                objectInputStream.close();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView
                    .State state) {
                int verticalSpacing = (int) Math.ceil(TypedValue.applyDimension(TypedValue
                        .COMPLEX_UNIT_SP, 4, getActivity().getResources()
                        .getDisplayMetrics()));
                outRect.top = verticalSpacing;
                outRect.bottom = verticalSpacing;
            }
        };
        divider.setDrawable(getActivity().getResources().getDrawable(R.drawable.greydivider));
        downloadsList.addItemDecoration(divider);

        downloadsStartPauseButton = view.findViewById(R.id.downloadsStartPauseButton);
        if (Utils.isServiceRunning(DownloadManager.class, getActivity())) {
            downloadsStartPauseButton.setText(R.string.pause);
        } else downloadsStartPauseButton.setText(R.string.start);
        downloadsStartPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent downloadService = getLMvdActivity().getDownloadService();
                if (Utils.isServiceRunning(DownloadManager.class, getActivity())) {
                    getActivity().stopService(downloadService);
                    DownloadManager.stopThread();
                    downloadsStartPauseButton.setText(R.string.start);
                    tracking.stopTracking();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        final PermissionsManager downloadPermMngr = new PermissionsManager(getActivity()) {
                            @Override
                            public void showRequestPermissionRationale() {
                                showPermissionSummaryDialog(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions();
                                    }
                                });
                            }

                            @Override
                            public void requestDisallowedAction() {
                                SharedPreferences prefs = getActivity().getSharedPreferences
                                        ("settings", 0);
                                boolean requestDisallowed = prefs.getBoolean("requestDisallowed",
                                        false);
                                if (requestDisallowed) {
                                    showPermissionSummaryDialog(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setMessage("Go to Settings?")
                                                    .setPositiveButton("Yes", new
                                                            DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    startActivityForResult(new Intent(android
                                                                            .provider.Settings
                                                                            .ACTION_APPLICATION_DETAILS_SETTINGS,
                                                                            Uri.fromParts("package", getActivity()
                                                                                            .getPackageName(),
                                                                                    null)), 1337);
                                                                }
                                                            })
                                                    .setNegativeButton(
                                                            "No", new
                                                                    DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            Toast.makeText(getActivity(), "Can't download; Necessary PERMISSIONS denied." +
                                                                                    " Try again", Toast.LENGTH_LONG).show();
                                                                        }
                                                                    })
                                                    .create()
                                                    .show();

                                        }
                                    });
                                } else {
                                    prefs.edit().putBoolean("requestDisallowed", true).apply();
                                    onPermissionsDenied();
                                }
                            }

                            @Override
                            public void onPermissionsGranted() {
                                startDownload();
                            }

                            @Override
                            public void onPermissionsDenied() {
                                Toast.makeText(getActivity(), "Can't download; Necessary PERMISSIONS denied." +
                                        " Try again", Toast.LENGTH_LONG).show();
                            }

                            private void showPermissionSummaryDialog(DialogInterface.OnClickListener
                                                                             okListener) {
                                new AlertDialog.Builder(getActivity())
                                        .setPositiveButton("OK", okListener)
                                        .setMessage("This feature requires WRITE_EXTERNAL_STORAGE " +
                                                "permission to save downloaded videos into the Download " +
                                                "folder. Make sure to grant this permission. Otherwise, " +
                                                "downloading videos is not possible.")
                                        .create()
                                        .show();
                            }
                        };
                        downloadPermMngr.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                PermissionRequestCodes.DOWNLOADS);
                    } else startDownload();
                }
            }
        });

        DownloadManager.setOnDownloadFinishedListener(this);
        DownloadManager.setOnLinkNotFoundListener(this);

        onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337) {
            PermissionsManager downloadsPermMgr = new PermissionsManager(getActivity()) {
                @Override
                public void showRequestPermissionRationale() {

                }

                @Override
                public void requestDisallowedAction() {
                    onPermissionsDenied();
                }

                @Override
                public void onPermissionsGranted() {
                    startDownload();
                }

                @Override
                public void onPermissionsDenied() {
                    Toast.makeText(getActivity(), "Can't download; Necessary PERMISSIONS denied." +
                            " Try again", Toast.LENGTH_LONG).show();
                }
            };
            downloadsPermMgr.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    PermissionRequestCodes.DOWNLOADS);
        }
    }

    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
    }

    private void startDownload() {
        Intent downloadService = getLMvdActivity().getDownloadService();
        if (downloads.size() > 0) {
            DownloadVideo topVideo = downloads.get(0);
            downloadService.putExtra("link", topVideo.link);
            downloadService.putExtra("name", topVideo.name);
            downloadService.putExtra("type", topVideo.type);
            downloadService.putExtra("size", topVideo.size);
            getActivity().startService(downloadService);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadsStartPauseButton.setText(R.string.pause);
                }
            });
            tracking.startTracking();
        }
    }

    @Override
    public void onDownloadFinished() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadsStartPauseButton.setText(R.string.start);
                tracking.stopTracking();
                if (downloads.size() > 0) {
                    String name = downloads.get(0).name;
                    String type = downloads.get(0).type;
                    downloads.remove(0);
                    queues.saveQueues(getActivity());
                    onAddDownloadedVideoToCompletedListener.onAddDownloadedVideoToCompleted(name, type);
                    downloadsList.getAdapter().notifyItemRemoved(0);
                    onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                }
                startDownload();
            }
        });
    }

    @Override
    public void onLinkNotFound() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadsStartPauseButton.setText(R.string.start);
                tracking.stopTracking();
                if (downloads.size() > 0) {
                    DownloadVideo video = downloads.get(0);
                    DownloadVideo inactiveDownload = new DownloadVideo();
                    inactiveDownload.name = video.name;
                    inactiveDownload.link = video.link;
                    inactiveDownload.page = video.page;
                    inactiveDownload.size = video.size;
                    inactiveDownload.type = video.type;
                    downloads.remove(0);
                    queues.saveQueues(getActivity());
                    onAddDownloadItemToInactiveListener.onAddDownloadItemToInactive(inactiveDownload);
                    downloadsList.getAdapter().notifyItemRemoved(0);
                    onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                }
                startDownload();
            }
        });
    }

    public void updateDownloadItem() {
        downloadsList.getAdapter().notifyItemChanged(0);
    }

    public void setOnAddDownloadedVideoToCompletedListener
            (OnAddDownloadedVideoToCompletedListener onAddDownloadedVideoToCompletedListener) {
        this.onAddDownloadedVideoToCompletedListener = onAddDownloadedVideoToCompletedListener;
    }

    public void setOnAddDownloadItemToInactiveListener(OnAddDownloadItemToInactiveListener
                                                               onAddDownloadItemToInactiveListener) {
        this.onAddDownloadItemToInactiveListener = onAddDownloadItemToInactiveListener;
    }

    public void setOnNumDownloadsInProgressChangeListener(OnNumDownloadsInProgressChangeListener
                                                                  onNumDownloadsInProgressChangeListener) {
        this.onNumDownloadsInProgressChangeListener = onNumDownloadsInProgressChangeListener;
    }

    @Override
    public void onDownloadWithNewLink(final DownloadVideo download) {
        Log.i("loremarTest", "download with new link");
        Intent downloadService = getLMvdActivity().getDownloadService();
        if (Utils.isServiceRunning(DownloadManager.class, getActivity())) {
            getActivity().stopService(downloadService);
            DownloadManager.stopThread();
            downloadsStartPauseButton.setText(R.string.start);
            tracking.stopTracking();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloads.add(0, download);
                queues.saveQueues(getActivity());
                downloadsList.getAdapter().notifyItemInserted(0);
                onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                startDownload();
            }
        });
    }


    class DownloadListAdapter extends RecyclerView.Adapter<DownloadItem> {

        @NonNull
        @Override
        public DownloadItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            return new DownloadItem(inflater.inflate(R.layout.downloads_in_progress_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DownloadItem holder, int position) {
            holder.bind(downloads.get(position));
        }

        @Override
        public int getItemCount() {
            return downloads.size();
        }
    }

    class DownloadItem extends RecyclerView.ViewHolder implements ViewTreeObserver.OnGlobalLayoutListener {
        private TextView name;
        private TextView ext;
        private ImageView rename;
        private ImageView delete;
        private ProgressBar progress;
        private TextView status;

        boolean adjustedlayout;

        DownloadItem(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.downloadVideoName);
            ext = itemView.findViewById(R.id.downloadVideoExt);
            rename = itemView.findViewById(R.id.renameDownloadVideo);
            delete = itemView.findViewById(R.id.deleteDownloadItem);
            progress = itemView.findViewById(R.id.downloadProgressBar);
            status = itemView.findViewById(R.id.downloadProgressText);
            itemView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            ext.getViewTreeObserver().addOnGlobalLayoutListener(this);
            rename.getViewTreeObserver().addOnGlobalLayoutListener(this);
            delete.getViewTreeObserver().addOnGlobalLayoutListener(this);
            adjustedlayout = false;
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Remove this item?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int position = getAdapterPosition();
                                    if (position != 0) {
                                        downloads.remove(position);
                                        queues.saveQueues(getActivity());
                                        downloadsList.getAdapter().notifyItemRemoved(position);
                                    } else {
                                        downloads.remove(position);
                                        queues.saveQueues(getActivity());
                                        downloadsList.getAdapter().notifyItemRemoved(position);
                                        startDownload();
                                    }
                                    onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();
                }
            });
            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new RenameDialog(getActivity(), name.getText().toString()) {
                        @Override
                        public void onOK(String newName) {
                            queues.renameItem(getAdapterPosition(), newName);
                            File renamedFile = new File(Environment
                                    .getExternalStoragePublicDirectory
                                            (Environment.DIRECTORY_DOWNLOADS), downloads.get
                                    (getAdapterPosition()).name + ext.getText().toString());
                            File file = new File(Environment.getExternalStoragePublicDirectory
                                    (Environment.DIRECTORY_DOWNLOADS), name.getText().toString()
                                    + ext.getText().toString());
                            if (file.exists()) {
                                if (file.renameTo(renamedFile)) {
                                    queues.saveQueues(getActivity());
                                    downloadsList.getAdapter().notifyItemChanged(getAdapterPosition());
                                } else {
                                    downloads.get(getAdapterPosition()).name = name.getText()
                                            .toString();
                                    Toast.makeText(getActivity(), "Failed: Cannot rename file",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                queues.saveQueues(getActivity());
                                downloadsList.getAdapter().notifyItemChanged(getAdapterPosition());
                            }
                        }
                    };
                }
            });
        }

        void bind(DownloadVideo video) {
            name.setText(video.name);
            String extString = "." + video.type;
            ext.setText(extString);
            String downloaded;
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment
                    .DIRECTORY_DOWNLOADS), video.name + extString);
            if (file.exists()) {
                if (video.size != null) {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatFileSize(getActivity(), downloadedSize);
                    double percent = 100d * downloadedSize / Long.parseLong(video.size);
                    if (percent > 100d) {
                        percent = 100d;
                    }
                    DecimalFormat percentFormat = new DecimalFormat("00.00");
                    String percentFormatted = percentFormat.format(percent);
                    progress.setProgress((int) percent);
                    String formattedSize = Formatter.formatFileSize(getActivity(), Long
                            .parseLong(video.size));
                    String statusString = downloaded + " / " + formattedSize + " " + percentFormatted +
                            "%";
                    status.setText(statusString);
                } else {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatShortFileSize(getActivity(), downloadedSize);
                    status.setText(downloaded);
                    progress.setProgress(0);
                }
            } else {
                if (video.size != null) {
                    String formattedSize = Formatter.formatShortFileSize(getActivity(), Long
                            .parseLong(video.size));
                    String statusString = "0KB / " + formattedSize + " 0%";
                    status.setText(statusString);
                    progress.setProgress(0);
                } else {
                    String statusString = "0kB";
                    status.setText(statusString);
                    progress.setProgress(0);
                }
            }
        }

        @Override
        public void onGlobalLayout() {
            if (!adjustedlayout) {
                if (itemView.getWidth() != 0 && ext.getWidth() != 0 && rename.getWidth() != 0 && delete
                        .getWidth() != 0) {
                    int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                    .COMPLEX_UNIT_DIP, 35,
                            getActivity().getResources().getDisplayMetrics());
                    int nameMaxWidth = itemView.getMeasuredWidth() - totalMargin - ext
                            .getMeasuredWidth() - rename.getMeasuredWidth() - delete
                            .getMeasuredWidth();
                    name.setMaxWidth(nameMaxWidth);
                    adjustedlayout = true;
                }
            }
        }
    }
}
