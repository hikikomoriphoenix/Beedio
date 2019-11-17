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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import marabillas.loremar.lmvideodownloader.LMvdActivity;
import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.download_feature.DownloadManager;
import marabillas.loremar.lmvideodownloader.download_feature.Tracking;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class Downloads extends LMvdFragment implements LMvdActivity.OnBackPressedListener, Tracking, DownloadsInProgress.OnNumDownloadsInProgressChangeListener, DownloadsCompleted.OnNumDownloadsCompletedChangeListener, DownloadsInactive.OnNumDownloadsInactiveChangeListener {
    private View view;

    private TextView downloadSpeed;
    private TextView remaining;
    private Handler mainHandler;
    private Tracking tracking;

    //private TabLayout tabs;
    private TextView inProgressTab;
    private TextView completedTab;
    private TextView inactiveTab;
    private TextView pageSelected;
    private ViewPager pager;
    private DownloadsInProgress downloadsInProgress;
    private DownloadsCompleted downloadsCompleted;
    private DownloadsInactive downloadsInactive;

    @Override
    public void onDestroy() {
        Fragment fragment;
        if ((fragment = getFragmentManager().findFragmentByTag("downloadsInProgress")) != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if ((fragment = getFragmentManager().findFragmentByTag("downloadsCompleted")) != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if ((fragment = getFragmentManager().findFragmentByTag("downloadsInactive")) != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        if (view == null) {
            view = inflater.inflate(R.layout.downloads, container, false);

            final DrawerLayout layout = getActivity().findViewById(R.id.drawer);
            ImageView menu = view.findViewById(R.id.menuButton);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layout.openDrawer(GravityCompat.START);
                }
            });

            downloadSpeed = view.findViewById(R.id.downloadSpeed);
            remaining = view.findViewById(R.id.remaining);

            getLMvdActivity().setOnBackPressedListener(this);

            mainHandler = new Handler(Looper.getMainLooper());
            tracking = new Tracking();

            pager = view.findViewById(R.id.downloadsPager);
            pager.setAdapter(new PagerAdapter());

            /*if (Build.VERSION.SDK_INT >= 22) {
                tabs = view.findViewById(R.id.downloadsTabs);
                tabs.addTab(tabs.newTab());
                tabs.addTab(tabs.newTab());
                tabs.addTab(tabs.newTab());

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
            } else {*/
            LinearLayout tabs0 = view.findViewById(R.id.downloadsTabs);
            inProgressTab = tabs0.findViewById(R.id.inProgressTab);
            completedTab = tabs0.findViewById(R.id.completedTab);
            inactiveTab = tabs0.findViewById(R.id.inactiveTab);

            pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    switch (position) {
                        case 0:
                            unboxPreviousSelectedPageTab();
                            boxNewSelectedPageTab(inProgressTab);
                            break;
                        case 1:
                            unboxPreviousSelectedPageTab();
                            boxNewSelectedPageTab(completedTab);
                            break;
                        case 2:
                            unboxPreviousSelectedPageTab();
                            boxNewSelectedPageTab(inactiveTab);
                    }
                }
            });

            inProgressTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unboxPreviousSelectedPageTab();
                    boxNewSelectedPageTab(inProgressTab);
                    pager.setCurrentItem(0);
                }
            });

            completedTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unboxPreviousSelectedPageTab();
                    boxNewSelectedPageTab(completedTab);
                    pager.setCurrentItem(1);
                }
            });

            inactiveTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unboxPreviousSelectedPageTab();
                    boxNewSelectedPageTab(inactiveTab);
                    pager.setCurrentItem(2);
                }
            });

            pager.setOffscreenPageLimit(2);//default is 1 which would make Inactive tab not diplay

            downloadsInProgress = new DownloadsInProgress();
            downloadsCompleted = new DownloadsCompleted();
            downloadsInactive = new DownloadsInactive();

            downloadsInProgress.setOnNumDownloadsInProgressChangeListener(this);
            downloadsCompleted.setOnNumDownloadsCompletedChangeListener(this);
            downloadsInactive.setOnNumDownloadsInactiveChangeListener(this);

            getFragmentManager().beginTransaction().add(pager.getId(), downloadsInProgress,
                    "downloadsInProgress").commit();
            getFragmentManager().beginTransaction().add(pager.getId(), downloadsCompleted,
                    "downloadsCompleted").commit();
            getFragmentManager().beginTransaction().add(pager.getId(), downloadsInactive,
                    "downloadsInactive").commit();

            downloadsInProgress.setTracking(this);

            downloadsInProgress.setOnAddDownloadedVideoToCompletedListener(downloadsCompleted);
            downloadsInProgress.setOnAddDownloadItemToInactiveListener(downloadsInactive);
            downloadsInactive.setOnDownloadWithNewLinkListener(downloadsInProgress);


            pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if (position == 1) {
                        final SharedPreferences prefs = getActivity().getSharedPreferences("settings", 0);
                        if (prefs.getBoolean(getString(R.string.showDownloadNotice), true)) {
                            View view = inflater.inflate(R.layout.download_notice_checkbox,
                                    container, false);
                            final CheckBox showNoticeCheckbox = view.findViewById(R.id.showNoticeCheckbox);
                            showNoticeCheckbox.setChecked(false);
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("Downloaded videos are either saved in the " +
                                            "storage's Download folder or in the app's own " +
                                            "Download folder which is located in " +
                                            "Android/data/marabillas.loremar.lmvideodownloader/.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (showNoticeCheckbox.isChecked()) {
                                                prefs.edit().putBoolean(getString(R.string.showDownloadNotice), false)
                                                        .apply();
                                            }
                                        }
                                    })
                                    .setView(view)
                                    .create()
                                    .show();
                        }
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pager.setCurrentItem(0);
        boxNewSelectedPageTab(inProgressTab);
    }

    private void unboxPreviousSelectedPageTab() {
        if (pageSelected != null) {
            pageSelected.setBackground(null);
            pageSelected = null;
        }
    }

    private void boxNewSelectedPageTab(TextView selected) {
        pageSelected = selected;
        pageSelected.setBackground(getResources().getDrawable(R.drawable.tab_text_bg));
    }

    @Override
    public void onBackpressed() {
        getLMvdActivity().getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onNumDownloadsInProgressChange() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpannableStringBuilder tabText = createStyledTabText(12, downloadsInProgress
                        .getNumDownloadsInProgress(), "In Progress " + downloadsInProgress
                        .getNumDownloadsInProgress());
                /*if (Build.VERSION.SDK_INT >= 22) {
                    TabLayout.Tab tab = tabs.getTabAt(0);
                    if (tab != null) {
                        tab.setText(tabText);
                    }
                } else {*/
                    inProgressTab.setText(tabText);
            }
        });
    }

    @Override
    public void onNumDownloadsCompletedChange() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpannableStringBuilder tabText = createStyledTabText(10, downloadsCompleted
                        .getNumDownloadsCompleted(), "Completed " + downloadsCompleted
                        .getNumDownloadsCompleted());
                /*if (Build.VERSION.SDK_INT >= 22) {
                    TabLayout.Tab tab = tabs.getTabAt(1);
                    if (tab != null) {
                        tab.setText(tabText);
                    }
                } else {*/
                    completedTab.setText(tabText);
            }
        });
    }

    @Override
    public void onNumDownloadsInactiveChange() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpannableStringBuilder tabText = createStyledTabText(9, downloadsInactive
                        .getNumDownloadsInactive(), "Inactive " + downloadsInactive
                        .getNumDownloadsInactive());
                /*if (Build.VERSION.SDK_INT >= 22) {
                    TabLayout.Tab tab = tabs.getTabAt(2);
                    if (tab != null) {
                        tab.setText(tabText);
                    }
                } else {*/
                    inactiveTab.setText(tabText);
            }
        });
    }

    private SpannableStringBuilder createStyledTabText(int start, int num, String text) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        ForegroundColorSpan fcs;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            fcs = new ForegroundColorSpan(getResources().getColor(R.color.darkColor));
        } else {
            fcs = new ForegroundColorSpan(getResources().getColor(R.color.darkColor, null));
        }
        sb.setSpan(fcs, start, start + String.valueOf(num).length(), Spanned
                .SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    class Tracking implements Runnable {

        @Override
        public void run() {
            long downloadSpeedValue = DownloadManager.getDownloadSpeed();
            String downloadSpeedText = "Speed:" + Formatter.formatShortFileSize(getActivity(),
                    downloadSpeedValue) + "/s";

            downloadSpeed.setText(downloadSpeedText);

            if (downloadSpeedValue > 0) {
                long remainingMills = DownloadManager.getRemaining();
                String remainingText = "Remaining:" + Utils.getHrsMinsSecs(remainingMills);
                remaining.setText(remainingText);
            } else {
                remaining.setText(R.string.remaining_undefine);
            }

            if (getFragmentManager() != null && getFragmentManager().findFragmentByTag
                    ("downloadsInProgress") != null) {
                downloadsInProgress.updateDownloadItem();
            }
            mainHandler.postDelayed(this, 1000);
        }
    }

    public void startTracking() {
        getActivity().runOnUiThread(tracking);
    }

    public void stopTracking() {
        mainHandler.removeCallbacks(tracking);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadSpeed.setText(R.string.speed_0);
                remaining.setText(R.string.remaining_undefine);
                if (getFragmentManager().findFragmentByTag("downloadsInProgress") != null) {
                    downloadsInProgress.updateDownloadItem();
                }
            }
        });
    }

    class PagerAdapter extends androidx.viewpager.widget.PagerAdapter {
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
            return ((Fragment) object).getView() == view;
        }
    }
}
