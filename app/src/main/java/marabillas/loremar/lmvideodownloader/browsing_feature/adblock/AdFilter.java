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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
final public class AdFilter {
    @PrimaryKey(autoGenerate = true)
    int uid;
    @ColumnInfo(name = "filter_str")
    String filterStr = null;
    @ColumnInfo(name = "starts_with")
    String startsWith = "";
    @ColumnInfo(name = "start_with_domain")
    String startsWithDomain = "";
    @ColumnInfo(name = "contains")
    List<String> contains = new ArrayList<>();
    @ColumnInfo(name = "ends_with_last")
    boolean endsWithLast = false;
    @ColumnInfo(name = "is_exception")
    boolean isException = false;
    @ColumnInfo(name = "domains")
    List<String> domains = new ArrayList<>();
    @ColumnInfo(name = "excluded_domains")
    List<String> excDomains = new ArrayList<>();

    @Override
    public String toString() {
        return "{ fiterStr: " + filterStr + "\nstartsWith: " + startsWith + "\nstartsWithDomain: "
                + startsWithDomain + "\ncontains: " + contains + "\nendsWidthLast: " + endsWithLast
                + "\nisException: " + isException + "\ndomains: " + domains + "\nexcDomains" +
                excDomains + " }";
    }
}