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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.download_feature.fragments.DownloadsInProgress;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class DownloadRearranger implements View.OnTouchListener {
    private Context context;
    private DownloadsInProgress downloadsInProgress;
    private View anchorView;

    private TextView name;
    private TextView ext;
    private TextView status;
    private ProgressBar progress;

    private DownloadVideo item;

    private int position;
    private int height;

    private float y0;
    private float moveY = 0;

    public DownloadRearranger(Context context, DownloadsInProgress downloadsInProgress) {
        this.context = context;
        this.downloadsInProgress = downloadsInProgress;
        LayoutInflater inflater = LayoutInflater.from(context);
        anchorView = inflater.inflate(R.layout.downloads_in_progress_item, (ViewGroup) downloadsInProgress
                .getView(), false);
        if (downloadsInProgress.getView() != null) {
            ((ViewGroup) downloadsInProgress.getView()).addView(anchorView);
        }
        name = anchorView.findViewById(R.id.downloadVideoName);
        ext = anchorView.findViewById(R.id.downloadVideoExt);
        status = anchorView.findViewById(R.id.downloadProgressText);
        progress = anchorView.findViewById(R.id.downloadProgressBar);
        anchorView.findViewById(R.id.deleteDownloadItem).setVisibility(View.INVISIBLE);
        anchorView.findViewById(R.id.renameDownloadVideo).setVisibility(View.INVISIBLE);
        anchorView.findViewById(R.id.moveButton).setVisibility(View.INVISIBLE);

        anchorView.setBackground(context.getResources().getDrawable(R.drawable
                .download_item_dragged_background));
        anchorView.setVisibility(View.GONE);
        anchorView.setOnTouchListener(this);
    }

    public void start(DownloadsInProgress.DownloadItem itemHolder, DownloadVideo item) {
        this.position = itemHolder.getAdapterPosition();
        this.item = item;
        float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context
                .getResources().getDisplayMetrics());
        anchorView.setY(itemHolder.itemView.getY() + margin);
        anchorView.setVisibility(View.VISIBLE);
        height = itemHolder.itemView.getHeight();
        name.setText(item.name);
        String extStr = "." + item.type;
        ext.setText(extStr);
        status.setText(itemHolder.getStatus());
        progress.setProgress(itemHolder.getProgress());
        name.setMaxWidth(itemHolder.getNameMaxWidth());
        downloadsInProgress.getAdapter().setSelectedItemPosition(position);
        itemHolder.itemView.setVisibility(View.INVISIBLE);
        downloadsInProgress.disableDownloadListTouch();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                y0 = event.getRawY();
                moveY = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaY = event.getRawY() - y0;
                y0 = event.getRawY();
                anchorView.setY(anchorView.getY() + deltaY);
                if (anchorView.getY() <= 0 || anchorView.getY() >= (downloadsInProgress
                        .getDownloadListHeight() - anchorView.getHeight())) {
                    anchorView.setY(anchorView.getY() - deltaY);
                    deltaY = 0;
                }
                moveY += deltaY;
                if (moveY >= height) {
                    moveY = moveY - height;
                    if (position + 1 < downloadsInProgress.getDownloads().size()) {
                        downloadsInProgress.getAdapter().setSelectedItemPosition(position + 1);
                        item = downloadsInProgress.getDownloads().get(position);
                        downloadsInProgress.getDownloads().remove(position);
                        downloadsInProgress.getAdapter().notifyItemRemoved(position);
                        downloadsInProgress.getDownloads().add(position + 1, item);
                        downloadsInProgress.getAdapter().notifyItemInserted(position + 1);
                        position++;
                    }
                } else if (moveY <= -height) {
                    moveY = moveY - (-height);
                    if (position - 1 >= 0) {
                        downloadsInProgress.getAdapter().setSelectedItemPosition(position - 1);
                        item = downloadsInProgress.getDownloads().get(position);
                        downloadsInProgress.getDownloads().remove(position);
                        downloadsInProgress.getAdapter().notifyItemRemoved(position);
                        downloadsInProgress.getDownloads().add(position - 1, item);
                        if (anchorView.getY() < height) {
                            //notifyItemInserted doesn't seem to move the top item
                            downloadsInProgress.getAdapter().notifyDataSetChanged();
                        } else {
                            downloadsInProgress.getAdapter().notifyItemInserted(position - 1);
                        }
                        position--;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                moveY = 0;
                anchorView.setVisibility(View.GONE);
                downloadsInProgress.getAdapter().setSelectedItemPosition(-1);
                downloadsInProgress.getAdapter().notifyItemChanged(position);
                downloadsInProgress.enableDownloadListTouch();
                downloadsInProgress.saveQueues();
                if (position == 0 && Utils.isServiceRunning(DownloadManager.class, context.getApplicationContext())) {
                    downloadsInProgress.pauseDownload();
                    downloadsInProgress.startDownload();
                }
                break;
        }
        return true;
    }
}
