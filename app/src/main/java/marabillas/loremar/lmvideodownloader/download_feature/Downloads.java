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

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdActivity;
import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.Utils;

public class Downloads extends LMvdFragment implements LMvdActivity.OnBackPressedListener, Tracking {
    private List<DownloadVideo> downloads;
    private TextView downloadSpeed;
    private TextView remaining;
    private Handler mainHandler;
    private Tracking tracking;

    private TabLayout tabs;
    private ViewPager pager;
    private DownloadsInProgress downloadsInProgress;
    private DownloadsCompleted downloadsCompleted;
    private DownloadsInactive downloadsInactive;

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

        downloadSpeed = view.findViewById(R.id.downloadSpeed);
        remaining = view.findViewById(R.id.remaining);

        getLMvdActivity().setOnBackPressedListener(this);

        mainHandler = new Handler(Looper.getMainLooper());
        tracking = new Tracking();

        tabs = view.findViewById(R.id.downloadsTabs);
        pager = view.findViewById(R.id.downloadsPager);

        tabs.addTab(tabs.newTab().setText("In Progress"));
        tabs.addTab(tabs.newTab().setText("Completed"));
        tabs.addTab(tabs.newTab().setText("Inactive"));
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabs.setTabTextColors(Color.BLACK, Color.BLUE);

        pager.setAdapter(new PagerAdapter());
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        downloadsInProgress = new DownloadsInProgress();
        downloadsCompleted = new DownloadsCompleted();
        downloadsInactive = new DownloadsInactive();

        getFragmentManager().beginTransaction().add(pager.getId(), downloadsInProgress).commit();
        getFragmentManager().beginTransaction().add(pager.getId(), downloadsCompleted).commit();
        getFragmentManager().beginTransaction().add(pager.getId(), downloadsInactive).commit();

        downloadsInProgress.setTracking(this);

        return view;
    }

    @Override
    public void onBackpressed() {
        getLMvdActivity().getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
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

            if(getFragmentManager().findFragmentByTag("downloadsInProgress")!=null) {
                downloadsInProgress.updateDownloadItem();
                if (pager.getCurrentItem() == 0 && pager.getAdapter()!=null) {
                    pager.getAdapter().notifyDataSetChanged();
                }
            }
            mainHandler.postDelayed(this, 1000);
        }
    }

    public void startTracking() {
        tracking.run();
    }

    public void stopTracking() {
        mainHandler.removeCallbacks(tracking);
        downloadSpeed.setText(R.string.speed_0);
        remaining.setText(R.string.remaining_undefine);
        if(getFragmentManager().findFragmentByTag("downloadsInProgress")!=null) {
            downloadsInProgress.updateDownloadItem();
            if (pager.getCurrentItem() == 0 && pager.getAdapter()!=null) {
                pager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    class PagerAdapter extends android.support.v4.view.PagerAdapter{
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            switch (position) {
                case 0:
                    return downloadsInProgress;
                case 1:
                    return downloadsCompleted;
                case 2:
                    return downloadsInactive;
                default:
                    return downloadsInProgress;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return ((Fragment)object).getView() == view;
        }
    }
}
