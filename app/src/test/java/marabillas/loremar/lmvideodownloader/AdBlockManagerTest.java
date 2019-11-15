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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import marabillas.loremar.lmvideodownloader.browsing_feature.adblock.AdBlockManager;

import static com.google.common.truth.Truth.assertThat;

public class AdBlockManagerTest {
    private AdBlockManager adBlock = new AdBlockManager();

    @Before
    public void setupFilters() throws InterruptedException {
        final CountDownLatch cd = new CountDownLatch(1);

        long t = System.currentTimeMillis();
        adBlock.update("", new AdBlockManager.UpdateListener() {
            @Override
            public void onAdBlockUpdateBegins() {
                System.out.println("Updating ad-block filters");
            }

            @Override
            public void onAdBlockUpdateEnds() {
                cd.countDown();
                System.out.println("filters count: " + adBlock.filtersCount());
            }

            @Override
            public void onUpdateFiltersLastUpdated(String today) {

            }

            @Override
            public void onSaveFilters() {

            }

            @Override
            public void onLoadFilters() {

            }
        });

        cd.await();
        System.out.println("update -> " + (System.currentTimeMillis() - t) + " ms");
    }

    @Test
    public void testSimpleFilter() {
        // Test for /getads|
        test("https://www.examplead.com/getads", true);
    }

    @Test
    public void testNonAdUrl() {
        // Test for no matching filter
        test("https://this-is-not-an-ad", false);
    }

    @Test
    public void testException() {
        // Test for @@||ads.tiktok.com^$popup
        test("https://ads.tiktok.com/getads", false);
    }

    @Test
    public void testWildcardCharacters() {
        // Test for /promoredirect?*&campaign*&zone=
        String url = "http://some-domain.com/promoredirect?random-characters&campaign=random-characters" +
                "&zone=random-characters";
        test(url, true);
    }

    @Test
    public void testSeparator() {
        // Test for /eas?*^easformat=
        test("https://www.some-domain.com/eas?random-characters?easformat=random", true);

        // Test for inaharice.pw
        test("http://www.inaharice.pw:8888/random", true);
        test("inaharice.pw/random", true);
    }

    private void test(String url, boolean expected) {
        long t = System.currentTimeMillis();
        boolean isAd = adBlock.checkThroughFilters(url);
        assertThat(isAd).isEqualTo(expected);
        System.out.println("checkThroughFilter -> " + (System.currentTimeMillis() - t) + " ms");
    }
}
