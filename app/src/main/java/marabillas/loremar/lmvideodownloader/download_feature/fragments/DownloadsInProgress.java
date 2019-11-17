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

package marabillas.loremar.lmvideodownloader.download_feature.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.PermissionRequestCodes;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadPermissionHandler;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadRearranger;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadVideo;
import marabillas.loremar.lmvideodownloader.download_feature.OnDownloadWithNewLinkListener;
import marabillas.loremar.lmvideodownloader.download_feature.Tracking;
import marabillas.loremar.lmvideodownloader.download_feature.lists.DownloadQueues;
import marabillas.loremar.lmvideodownloader.utils.RenameDialog;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class DownloadsInProgress extends LMvdFragment
        implements DownloadManager.OnDownloadFinishedListener,
        DownloadManager.OnLinkNotFoundListener,
        OnDownloadWithNewLinkListener,
        DownloadManager.onDownloadFailExceptionListener {
    private View view;
    private List<DownloadVideo> downloads;
    private RecyclerView downloadsList;
    private DownloadQueues queues;
    private TextView downloadsStartPauseButton;

    private Tracking tracking;

    private DownloadRearranger downloadRearranger;
    private RecyclerView.OnItemTouchListener downloadsListItemTouchDisabler;

    private OnAddDownloadedVideoToCompletedListener onAddDownloadedVideoToCompletedListener;
    private OnAddDownloadItemToInactiveListener onAddDownloadItemToInactiveListener;
    private OnNumDownloadsInProgressChangeListener onNumDownloadsInProgressChangeListener;

    private RenameDialog activeRenameDialog;

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
        setRetainInstance(true);

        downloads = new ArrayList<>();
        queues = DownloadQueues.load(getActivity());
        downloads = queues.getList();

        if (view == null) {
            view = inflater.inflate(R.layout.downloads_in_progress, container, false);

            downloadsList = view.findViewById(R.id.downloadsList);
            downloadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
            downloadsList.setAdapter(new DownloadListAdapter());
            downloadsList.setHasFixedSize(true);

            downloadsStartPauseButton = view.findViewById(R.id.downloadsStartPauseButton);

            downloadsStartPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isServiceRunning(DownloadManager.class, getActivity().getApplicationContext())) {
                        pauseDownload();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            new DownloadPermissionHandler(getActivity()) {
                                @Override
                                public void onPermissionGranted() {
                                    startDownload();
                                }
                            }.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    PermissionRequestCodes.DOWNLOADS);
                        } else startDownload();
                    }
                }
            });

            DownloadManager.setOnDownloadFinishedListener(this);
            DownloadManager.setOnLinkNotFoundListener(this);
            DownloadManager.setOnDownloadFailExceptionListener(this);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        DownloadManager.setOnDownloadFinishedListener(null);
        DownloadManager.setOnLinkNotFoundListener(null);
        super.onDestroyView();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (Utils.isServiceRunning(DownloadManager.class, getActivity().getApplicationContext())) {
            downloadsStartPauseButton.setText(R.string.pause);
            getAdapter().unpause();
            tracking.startTracking();
        } else {
            downloadsStartPauseButton.setText(R.string.start);
            getAdapter().pause();
            tracking.stopTracking();
        }

        downloadRearranger = new DownloadRearranger(getActivity(), this);
        downloadsListItemTouchDisabler = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return true;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadsList.getAdapter().notifyDataSetChanged();
        onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
    }

    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
    }

    public void startDownload() {
        Intent downloadService = getLMvdApp().getDownloadService();
        if (downloads.size() > 0) {
            DownloadVideo topVideo = downloads.get(0);
            downloadService.putExtra("link", topVideo.link);
            downloadService.putExtra("name", topVideo.name);
            downloadService.putExtra("type", topVideo.type);
            downloadService.putExtra("size", topVideo.size);
            downloadService.putExtra("page", topVideo.page);
            downloadService.putExtra("chunked", topVideo.chunked);
            downloadService.putExtra("website", topVideo.website);
            getLMvdApp().startService(downloadService);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadsStartPauseButton.setText(R.string.pause);
                    getAdapter().unpause();
                }
            });
            tracking.startTracking();
        }
    }

    public void pauseDownload() {
        DownloadManager.stop();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadsStartPauseButton.setText(R.string.start);
                tracking.stopTracking();
                getAdapter().pause();
            }
        });
    }

    @Override
    public void onDownloadFinished() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activeRenameDialog != null && activeRenameDialog.isActive()) {
                    activeRenameDialog.dismiss();
                }

                downloadsStartPauseButton.setText(R.string.start);
                tracking.stopTracking();
                if (downloads.size() > 0) {
                    String name = downloads.get(0).name;
                    String type = downloads.get(0).type;
                    downloads.remove(0);
                    saveQueues();
                    onAddDownloadedVideoToCompletedListener.onAddDownloadedVideoToCompleted(name, type);
                    getAdapter().notifyItemRemoved(0);
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
                    saveQueues();
                    onAddDownloadItemToInactiveListener.onAddDownloadItemToInactive(inactiveDownload);
                    getAdapter().notifyItemRemoved(0);
                    onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                }
                startDownload();
            }
        });
    }

    @Override
    public void onDownloadFailException(final String message) {
        pauseDownload();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setMessage("Can't download! " + message)
                        .setNeutralButton("OK", null)
                        .create()
                        .show();
            }
        });
    }

    public void updateDownloadItem() {
        getAdapter().notifyItemChanged(0);
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
        if (Utils.isServiceRunning(DownloadManager.class, getActivity().getApplicationContext())) {
            pauseDownload();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloads.add(0, download);
                saveQueues();
                getAdapter().notifyItemInserted(0);
                onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                startDownload();
            }
        });
    }

    public DownloadListAdapter getAdapter() {
        return (DownloadListAdapter) downloadsList.getAdapter();
    }

    public List<DownloadVideo> getDownloads() {
        return downloads;
    }

    public float getDownloadListHeight() {
        return downloadsList.getHeight();
    }

    public void disableDownloadListTouch() {
        downloadsList.addOnItemTouchListener(downloadsListItemTouchDisabler);
    }

    public void enableDownloadListTouch() {
        downloadsList.removeOnItemTouchListener(downloadsListItemTouchDisabler);
    }

    public void saveQueues() {
        queues.save(getActivity());
    }

    public class DownloadListAdapter extends RecyclerView.Adapter<DownloadItem> {
        private int selectedItemPosition = -1;
        private boolean paused;

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

        public int getSelectedItemPosition() {
            return selectedItemPosition;
        }

        public void setSelectedItemPosition(int position) {
            selectedItemPosition = position;
        }

        public void pause() {
            paused = true;
        }

        public void unpause() {
            paused = false;
        }

        public boolean isPaused() {
            return paused;
        }
    }

    public class DownloadItem extends RecyclerView.ViewHolder implements ViewTreeObserver
            .OnGlobalLayoutListener {
        private TextView name;
        private TextView ext;
        private ImageView rename;
        private ImageView delete;
        private ProgressBar progress;
        private TextView status;
        private TextView move;

        private boolean adjustedlayout;
        private int nameMaxWidth;

        DownloadItem(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.downloadVideoName);
            ext = itemView.findViewById(R.id.downloadVideoExt);
            rename = itemView.findViewById(R.id.renameDownloadVideo);
            delete = itemView.findViewById(R.id.deleteDownloadItem);
            progress = itemView.findViewById(R.id.downloadProgressBar);
            status = itemView.findViewById(R.id.downloadProgressText);
            move = itemView.findViewById(R.id.moveButton);
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
                                        saveQueues();
                                        getAdapter().notifyItemRemoved(position);
                                    } else {
                                        downloads.remove(position);
                                        saveQueues();
                                        getAdapter().notifyItemRemoved(position);
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
                    final String downloadFolder = DownloadManager.getDownloadFolder();
                    if (downloadFolder != null) {
                        final int itemToRenamePosition = getAdapterPosition();
                        if (itemToRenamePosition == -1)
                            return;

                        activeRenameDialog = new RenameDialog(
                                getActivity(),
                                name.getText().toString()) {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                activeRenameDialog = null;
                            }

                            @Override
                            public void onOK(String newName) {
                                queues.renameItem(itemToRenamePosition, newName);
                                File renamedFile = new File(downloadFolder, downloads.get
                                        (itemToRenamePosition).name + ext.getText().toString());
                                File file = new File(downloadFolder, name.getText().toString()
                                        + ext.getText().toString());
                                if (file.exists()) {
                                    if (file.renameTo(renamedFile)) {
                                        saveQueues();
                                        getAdapter().notifyItemChanged(itemToRenamePosition);
                                    } else {
                                        downloads.get(itemToRenamePosition).name = name.getText()
                                                .toString();
                                        Toast.makeText(getActivity(), "Failed: Cannot rename file",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    saveQueues();
                                    getAdapter().notifyItemChanged(itemToRenamePosition);
                                }
                                activeRenameDialog = null;
                            }
                        };
                    }
                }
            });

            move.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadRearranger.start(DownloadItem.this, downloads.get(getAdapterPosition
                            ()));
                }
            });
        }

        void bind(DownloadVideo video) {
            final String downloadFolder = DownloadManager.getDownloadFolder();
            if (downloadFolder != null) {
                name.setText(video.name);
                String extString = "." + video.type;
                ext.setText(extString);
                String downloaded;
                File file = new File(downloadFolder, video.name + extString);
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
                        if (!getAdapter().isPaused()) {
                            if (!progress.isIndeterminate()) {
                                progress.setIndeterminate(true);
                            }
                        } else {
                            progress.setIndeterminate(false);
                        }
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

                if (getAdapter().getSelectedItemPosition() == getAdapterPosition()) {
                    itemView.setVisibility(View.INVISIBLE);
                } else {
                    itemView.setVisibility(View.VISIBLE);
                }
            }
        }

        public String getStatus() {
            return status.getText().toString();
        }

        public int getProgress() {
            return progress.getProgress();
        }

        public int getNameMaxWidth() {
            return nameMaxWidth;
        }

        @Override
        public void onGlobalLayout() {
            if (!adjustedlayout) {
                if (itemView.getWidth() != 0 && ext.getWidth() != 0 && rename.getWidth() != 0 && delete
                        .getWidth() != 0) {
                    int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                    .COMPLEX_UNIT_DIP, 35,
                            getActivity().getResources().getDisplayMetrics());
                    nameMaxWidth = itemView.getMeasuredWidth() - totalMargin - ext
                            .getMeasuredWidth() - rename.getMeasuredWidth() - delete
                            .getMeasuredWidth();
                    name.setMaxWidth(nameMaxWidth);
                    adjustedlayout = true;
                }
            }
        }
    }
}
