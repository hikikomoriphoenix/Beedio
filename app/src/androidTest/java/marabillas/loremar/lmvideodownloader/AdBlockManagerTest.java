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

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import marabillas.loremar.lmvideodownloader.browsing_feature.adblock.AdBlockManager;

import static com.google.common.truth.Truth.assertThat;

public final class AdBlockManagerTest {

    @Test
    public void test() throws InterruptedException {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final CountDownLatch cd = new CountDownLatch(1);
        final AdBlockManager adblock = new AdBlockManager();
        adblock.update("", new AdBlockManager.UpdateListener() {
            @Override
            public void onAdBlockUpdateBegins() {
                System.out.println("Updating filters");
            }

            @Override
            public void onAdBlockUpdateEnds() {
                System.out.println("Total filters: " + adblock.filtersCount());
                cd.countDown();
            }

            @Override
            public void onUpdateFiltersLastUpdated(String today) {
                System.out.println("Filters last updated today: " + today);
            }

            @Override
            public void onSaveFilters() {
                adblock.saveFilters(context);
            }

            @Override
            public void onLoadFilters() {

            }
        });
        cd.await();

        AdBlockManager mAdblock = new AdBlockManager();
        mAdblock.loadFilters(context);
        boolean result = mAdblock.checkThroughFilters("https://www.examplead.com/getads");
        assertThat(result).isTrue();
    }
}
