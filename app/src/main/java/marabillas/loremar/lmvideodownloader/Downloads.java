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

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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

public class Downloads extends Fragment implements LMvd.OnBackPressedListener, DownloadManager.OnDownloadFinishedListener {
    private List<DownloadVideo> downloads;
    private RecyclerView downloadsList;
    private DownloadQueues queues;
    private TextView downloadSpeed;
    private TextView remaining;
    private TextView downloadsStartPauseButton;
    private Handler mainHandler;
    private Tracking tracking;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.downloads, container, false);

        final DrawerLayout layout = getActivity().findViewById(R.id.drawer);
        ImageView menu = view.findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(Gravity.START);
            }
        });

        downloadsList = view.findViewById(R.id.downloadsList);
        downloadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        downloadsList.setAdapter(new DownloadListAdapter());
        downloadsList.setHasFixedSize(true);
        downloads = new ArrayList<>();
        File file = new File(getActivity().getFilesDir(), "downloads.dat");
        if(file.exists()) {
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

        downloadSpeed = view.findViewById(R.id.downloadSpeed);
        remaining = view.findViewById(R.id.remaining);

        ((LMvd)getActivity()).setOnBackPressedListener(this);

        downloadsStartPauseButton = view.findViewById(R.id.downloadsStartPauseButton);
        if (Utils.isServiceRunning(DownloadManager.class, getActivity())) {
            downloadsStartPauseButton.setText(R.string.pause);
        }
        else downloadsStartPauseButton.setText(R.string.start);
        downloadsStartPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final PermissionsManager downloadPermMngr = new PermissionsManager(getActivity()) {
                        @Override
                        void showRequestPermissionRationale() {
                            showPermissionSummaryDialog(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions();
                                }
                            });
                        }

                        @Override
                        void requestDisallowedAction() {
                            showPermissionSummaryDialog(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog goToSettingsDialog = new AlertDialog.Builder
                                            (getActivity()).create();
                                    goToSettingsDialog.setMessage("Go to Settings?");
                                    goToSettingsDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                                            "Yes", new
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
                                                    });
                                    goToSettingsDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                                            "No", new
                                                    DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Toast.makeText(getActivity(), "Can't download; Necessary PERMISSIONS denied." +
                                                                    " Try again", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                    goToSettingsDialog.show();
                                }
                            });
                        }

                        @Override
                        void onPermissionsGranted() {
                            startDownload();
                        }

                        @Override
                        void onPermissionsDenied() {
                            Toast.makeText(getActivity(), "Can't download; Necessary PERMISSIONS denied." +
                                    " Try again", Toast.LENGTH_LONG).show();
                        }

                        private void showPermissionSummaryDialog(DialogInterface.OnClickListener
                                                                         okListener) {
                            AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", okListener);
                            dialog.setMessage("This feature requires WRITE_EXTERNAL_STORAGE " +
                                    "permission to save downloaded videos into the Download " +
                                    "folder. Make sure to grant this permission. Otherwise, " +
                                    "downloading videos is not possible.");
                            dialog.show();
                        }
                    };
                    downloadPermMngr.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            PermissionRequestCodes.DOWNLOADS);
                }
                else startDownload();
            }
        });

        mainHandler = new Handler(Looper.getMainLooper());
        tracking = new Tracking();

        DownloadManager.setOnDownloadFinishedListener(this);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1337) {
            PermissionsManager downloadsPermMgr = new PermissionsManager(getActivity()) {
                @Override
                void showRequestPermissionRationale() {

                }

                @Override
                void requestDisallowedAction() {
                    onPermissionsDenied();
                }

                @Override
                void onPermissionsGranted() {
                    startDownload();
                }

                @Override
                void onPermissionsDenied() {
                    Toast.makeText(getActivity(), "Can't download; Necessary PERMISSIONS denied." +
                            " Try again", Toast.LENGTH_LONG).show();
                }
            };
            downloadsPermMgr.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    PermissionRequestCodes.DOWNLOADS);
        }
    }

    private void startDownload() {
        Intent downloadService = ((LMvd)getActivity()).getDownloadService();
        if (Utils.isServiceRunning(DownloadManager.class, getActivity())) {
            Log.i("loremarTest", "service is running");
            getActivity().stopService(downloadService);
            DownloadManager.stopThread();
            downloadsStartPauseButton.setText(R.string.start);
            stopTracking();
        }
        else {
            Log.i("loremarTest", "service is not running");
            downloadService = ((LMvd)getActivity()).getDownloadService();
            if (downloads.size()>0) {
                DownloadVideo topVideo = downloads.get(0);
                downloadService.putExtra("link", topVideo.link);
                downloadService.putExtra("name", topVideo.name);
                downloadService.putExtra("type", topVideo.type);
                downloadService.putExtra("size", topVideo.size);
                getActivity().startService(downloadService);
                downloadsStartPauseButton.setText(R.string.pause);
                startTracking();
            }
        }
    }

    @Override
    public void onBackpressed() {
        ((LMvd)getActivity()).getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onDownloadFinished() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                downloadsStartPauseButton.setText(R.string.start);
                stopTracking();
            }
        });
        //todo download next item(start new download service), move top item to completed tab
    }

    class Tracking implements Runnable {

        @Override
        public void run() {
            long downloadSpeedValue = DownloadManager.getDownloadSpeed();
            String downloadSpeedText = "Speed:" + Formatter.formatShortFileSize(getActivity(),
                    downloadSpeedValue) + "/s";

            downloadSpeed.setText(downloadSpeedText);

            if (downloadSpeedValue>0) {
                long remainingMills = DownloadManager.getRemaining();
                String remainingText = "Remaining:" + Utils.getHrsMinsSecs(remainingMills);
                remaining.setText(remainingText);
            }
            else {
                remaining.setText(R.string.remaining_undefine);
            }

            downloadsList.getAdapter().notifyItemChanged(0);
            mainHandler.postDelayed(this, 1000);
        }
    }

    private void startTracking() {
        tracking.run();
    }

    private void stopTracking() {
        mainHandler.removeCallbacks(tracking);
        downloadSpeed.setText(R.string.speed_0);
        remaining.setText(R.string.remaining_undefine);
        downloadsList.getAdapter().notifyItemChanged(0);
    }

    class DownloadListAdapter extends RecyclerView.Adapter<DownloadItem > {

        @NonNull
        @Override
        public DownloadItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            return new DownloadItem(inflater.inflate(R.layout.download_item, parent, false));
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
        TextView name;
        TextView ext;
        ImageView rename;
        ImageView delete;
        ProgressBar progress;
        TextView status;

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
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage("Remove this item?");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloads.remove(getAdapterPosition());
                            queues.saveQueues(getActivity());
                            downloadsList.getAdapter().notifyDataSetChanged();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
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
                if (video.size!=null) {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatFileSize(getActivity(), downloadedSize);
                    double percent = 100d * downloadedSize/Long.parseLong(video.size);
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
                }
                else {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatShortFileSize(getActivity(), downloadedSize);
                    status.setText(downloaded);
                    progress.setProgress(0);
                }
            }
            else {
                if (video.size!=null) {
                    String formattedSize = Formatter.formatShortFileSize(getActivity(), Long
                            .parseLong(video.size));
                    String statusString = "0KB / " + formattedSize + " 0%";
                    status.setText(statusString);
                    progress.setProgress(0);
                }
                else {
                    String statusString = "0kB";
                    status.setText(statusString);
                    progress.setProgress(0);
                }
            }
        }

        @Override
        public void onGlobalLayout() {
            if (!adjustedlayout) {
                if (itemView.getWidth()!=0 && ext.getWidth()!=0 && rename.getWidth()!=0 && delete
                        .getWidth()!=0) {
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
