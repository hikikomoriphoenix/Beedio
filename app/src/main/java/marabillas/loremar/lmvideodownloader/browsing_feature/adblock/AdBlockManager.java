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

package marabillas.loremar.lmvideodownloader.browsing_feature.adblock;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import marabillas.loremar.lmvideodownloader.R;

public final class AdBlockManager {

    private AdBlockData data = new AdBlockData();
    private AdBlockDatabase filtersDb;

    public void loadFilters(final Context context) {
        if (filtersDb == null) {
            filtersDb = Room.databaseBuilder(context, AdBlockDatabase.class, "adblockfilters")
                    .build();
        }
        AdFilterDao dao = filtersDb.adFilterDao();
        long t = System.currentTimeMillis();
        data.filters = dao.getAll();
        System.out.println("data.filters -> " + (System.currentTimeMillis() - t) + " ms");

        SharedPreferences prefs = context.getSharedPreferences("settings", 0);
        data.easyListLastModified = prefs.getString(context.getString(R.string.easyListLastModified), "");
    }

    public void saveFilters(final Context context) {
        if (filtersDb == null) {
            filtersDb = Room.databaseBuilder(context, AdBlockDatabase.class, "adblockfilters")
                    .build();
        }
        AdFilterDao dao = filtersDb.adFilterDao();
        dao.deleteAll();
        dao.insertAll(data.filters);

        SharedPreferences prefs = context.getSharedPreferences("settings", 0);
        prefs.edit().putString(context.getString(R.string.easyListLastModified), data.easyListLastModified)
                .apply();
    }

    public void update(final String lastUpdated, final UpdateListener updateListener) {
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateListener.onAdBlockUpdateBegins();
                    final String today = new SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(new Date());
                    if (!today.equals(lastUpdated)) {
                        String easyList = "https://easylist.to/easylist/easylist.txt";
                        try {
                            URLConnection conn = new URL(easyList).openConnection();
                            if (conn != null) {
                                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64)" +
                                        " AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                                InputStream in = conn.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.contains("Last modified")) {
                                        if (line.equals(data.easyListLastModified)) {
                                            updateListener.onLoadFilters();
                                            return;
                                        } else {
                                            data.easyListLastModified = line;
                                        }
                                    } else if (
                                            !line.startsWith("!")
                                                    && !line.startsWith("[")
                                                    && !line.contains("#")
                                                    && !line.contains(":-")
                                    ) {
                                        parseFilter(line);
                                    }
                                }
                                updateListener.onSaveFilters();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        updateListener.onUpdateFiltersLastUpdated(today);
                    } else {
                        updateListener.onLoadFilters();
                    }
                    updateListener.onAdBlockUpdateEnds();
                }
            }).start();
        }
    }

    private void parseFilter(String filter) {
        if (filter.isEmpty()) return;

        int optionIndex = filter.indexOf("$");
        if (!filter.startsWith("@@") && optionIndex != -1) return;

        AdFilter mFilter = new AdFilter();
        mFilter.filterStr = filter;
        int startsWithDomainStartIndex = 2;
        int startsWithStartIndex = 1;
        int wildcardIndex = 0;
        if (filter.startsWith("@@")) {
            mFilter.isException = true;
            startsWithDomainStartIndex = 4;
            startsWithStartIndex = 3;
            wildcardIndex = 2;
        }

        boolean hasStartsWithDomain = filter.startsWith("||", startsWithDomainStartIndex - 2);
        boolean hasStartsWith = (!hasStartsWithDomain && filter.startsWith("|", startsWithStartIndex - 1));

        int endsWithIndex;
        if (hasStartsWith) endsWithIndex = filter.indexOf("|", startsWithStartIndex);
        else if (hasStartsWithDomain)
            endsWithIndex = filter.indexOf("|", startsWithDomainStartIndex);
        else endsWithIndex = filter.indexOf("|");
        if (endsWithIndex != -1) mFilter.endsWithLast = true;

        ArrayList<String> contains = new ArrayList<>();
        while (wildcardIndex < filter.length()) {
            int separatorIndex = filter.indexOf("^", wildcardIndex);
            int asteriskIndex = filter.indexOf("*", wildcardIndex);
            int nextWildcardIndex = -1;
            if (separatorIndex != -1 || asteriskIndex != -1) {
                if (separatorIndex == -1) {
                    nextWildcardIndex = asteriskIndex;
                } else if (asteriskIndex == -1) {
                    nextWildcardIndex = separatorIndex;
                } else {
                    nextWildcardIndex = (separatorIndex < asteriskIndex) ? separatorIndex : asteriskIndex;
                }
            }

            if (nextWildcardIndex == wildcardIndex && filter.charAt(nextWildcardIndex) != '^') {
                wildcardIndex++;
                continue;
            }

            if (hasStartsWithDomain && mFilter.startsWithDomain.isEmpty()) {
                if (endsWithIndex != -1) {
                    if (nextWildcardIndex != -1 && nextWildcardIndex < endsWithIndex) {
                        mFilter.startsWithDomain = filter.substring(startsWithDomainStartIndex, nextWildcardIndex);
                    } else {
                        mFilter.startsWithDomain = filter.substring(startsWithDomainStartIndex, endsWithIndex);
                        break;
                    }
                } else if (optionIndex != -1) {
                    if (nextWildcardIndex != -1 && nextWildcardIndex < optionIndex) {
                        mFilter.startsWithDomain = filter.substring(startsWithDomainStartIndex, nextWildcardIndex);
                    } else {
                        mFilter.startsWithDomain = filter.substring(startsWithDomainStartIndex, optionIndex);
                        break;
                    }
                } else if (nextWildcardIndex != -1) {
                    mFilter.startsWithDomain = filter.substring(startsWithDomainStartIndex, nextWildcardIndex);
                } else {
                    mFilter.startsWithDomain = filter.substring(startsWithDomainStartIndex);
                    break;
                }
            } else if (hasStartsWith && mFilter.startsWith.isEmpty()) {
                if (endsWithIndex != -1) {
                    if (nextWildcardIndex != -1 && nextWildcardIndex < endsWithIndex) {
                        mFilter.startsWith = filter.substring(startsWithStartIndex, nextWildcardIndex);
                    } else {
                        mFilter.startsWith = filter.substring(startsWithStartIndex, endsWithIndex);
                        break;
                    }
                } else if (optionIndex != -1) {
                    if (nextWildcardIndex != -1 && nextWildcardIndex < optionIndex) {
                        mFilter.startsWith = filter.substring(startsWithStartIndex, nextWildcardIndex);
                    } else {
                        mFilter.startsWith = filter.substring(startsWithStartIndex, optionIndex);
                        break;
                    }
                } else if (nextWildcardIndex != -1) {
                    mFilter.startsWith = filter.substring(startsWithStartIndex, nextWildcardIndex);
                } else {
                    mFilter.startsWith = filter.substring(startsWithStartIndex);
                    break;
                }
            } else if (nextWildcardIndex != -1) {
                if (endsWithIndex != -1 && nextWildcardIndex > endsWithIndex) {
                    contains.add(filter.substring(wildcardIndex, endsWithIndex));
                    break;
                } else if (optionIndex != -1 && nextWildcardIndex > optionIndex) {
                    contains.add(filter.substring(wildcardIndex, optionIndex));
                    break;
                } else {
                    contains.add(filter.substring(wildcardIndex, nextWildcardIndex));
                }
            } else if (endsWithIndex != -1) {
                if (endsWithIndex > wildcardIndex) {
                    contains.add(filter.substring(wildcardIndex, endsWithIndex));
                }
                break;
            } else if (optionIndex != -1) {
                if (optionIndex > wildcardIndex) {
                    contains.add(filter.substring(wildcardIndex, optionIndex));
                }
                break;
            } else {
                contains.add(filter.substring(wildcardIndex));
                break;
            }

            if (filter.charAt(nextWildcardIndex) == '^') {
                contains.add("^");
            }

            wildcardIndex = nextWildcardIndex + 1;
        }

        if (mFilter.isException && optionIndex != -1) {
            int domainIndex = filter.indexOf("domain=", optionIndex) + 1;
            if (domainIndex != 0) {
                while (domainIndex < filter.length()) {
                    int nextDomainIndex = filter.indexOf("|", domainIndex);
                    if (nextDomainIndex == -1) {
                        if (filter.charAt(domainIndex) == '~') {
                            String domain = filter.substring(domainIndex + 1);
                            if (!domain.isEmpty()) mFilter.excDomains.add(domain);
                        } else {
                            String domain = filter.substring(domainIndex);
                            if (!domain.isEmpty()) mFilter.domains.add(domain);
                        }
                        break;
                    } else {
                        if (filter.charAt(domainIndex) == '~') {
                            String domain = filter.substring(domainIndex + 1, nextDomainIndex);
                            if (!domain.isEmpty()) mFilter.excDomains.add(domain);
                        } else {
                            String domain = filter.substring(domainIndex, nextDomainIndex);
                            if (!domain.isEmpty()) mFilter.domains.add(domain);
                        }
                        domainIndex = nextDomainIndex + 1;
                    }
                }
            }
        }

        if (!contains.isEmpty()) mFilter.contains = contains;

        if (mFilter.isException) data.filters.add(0, mFilter);
        else data.filters.add(mFilter);
    }

    public boolean checkThroughFilters(String url) {
        for (AdFilter filter : data.filters) {
            boolean match = matchUrlWithFilter(url, filter);
            if (match && filter.isException) {
                return false;
            } else if (match) {
                return true;
            }
        }
        return false;
    }

    private boolean matchUrlWithFilter(String url, AdFilter filter) {
        String lastMatch = "";

        if (filter.isException) {
            int start = 0;
            if (url.startsWith("http://")) start += 7;
            else if (url.startsWith("https://")) start += 8;
            if (url.startsWith("www.", start)) start += 4;
            int end = url.indexOf("/", start);
            if (end == -1) end = url.length();
            String mDomain = url.substring(start, end);
            for (String domain : filter.domains) {
                if (!mDomain.contains(domain)) return false;
                for (String excDomain : filter.excDomains) {
                    if (mDomain.contains(excDomain)) return false;
                }
            }
        }

        if (!filter.startsWith.isEmpty()) {
            if (!url.startsWith(filter.startsWith)) return false;
            lastMatch = filter.startsWith;
        }

        if (!filter.startsWithDomain.isEmpty()) {
            if (url.startsWith("http://")) {
                if (url.startsWith("www.", 7)) {
                    if (!url.startsWith(filter.startsWithDomain, 11)) return false;
                    lastMatch = "http://www." + filter.startsWithDomain;
                } else {
                    if (!url.startsWith(filter.startsWithDomain, 7)) return false;
                    lastMatch = "http://" + filter.startsWithDomain;
                }
            } else if (url.startsWith("https://")) {
                if (url.startsWith("www.", 8)) {
                    if (!url.startsWith(filter.startsWithDomain, 12)) return false;
                    lastMatch = "https://www." + filter.startsWithDomain;
                } else {
                    if (!url.startsWith(filter.startsWithDomain, 8)) return false;
                    lastMatch = "https://" + filter.startsWithDomain;
                }
            } else if (!url.startsWith(filter.startsWithDomain)) return false;
            else {
                lastMatch = filter.startsWithDomain;
            }
        }

        int wildcardIndex = lastMatch.length();
        for (String contain : filter.contains) {
            if (contain.equals("^")) {
                char c = url.charAt(wildcardIndex);
                if (c != '/' && c != '?' && c != ':') return false;
                wildcardIndex++;
                continue;
            }

            if (url.indexOf(contain, wildcardIndex) == -1) return false;
            wildcardIndex = wildcardIndex + contain.length();
            lastMatch = contain;
        }

        return !filter.endsWithLast || url.endsWith(lastMatch);
    }

    public int filtersCount() {
        return data.filters.size();
    }

    final class AdBlockData {
        String easyListLastModified = "";
        List<AdFilter> filters = new ArrayList<>();
    }

    public interface UpdateListener {
        void onAdBlockUpdateBegins();
        void onAdBlockUpdateEnds();
        void onUpdateFiltersLastUpdated(String today);

        void onSaveFilters();

        void onLoadFilters();
    }
}