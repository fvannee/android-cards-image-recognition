package com.fnee.carddetector;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fnee.carddetector.common.DdsAdapter;
import com.fnee.carddetector.common.DoubleDummyAnalysis;
import com.fnee.carddetector.common.Game;
import com.fnee.carddetector.common.PbnReadAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void testDds() throws Exception {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        InputStream testInput = testContext.getAssets().open("test.pbn");
        ArrayList<Game> games = PbnReadAdapter.readGames(testInput);

        DoubleDummyAnalysis dds = DdsAdapter.calcTable(games.get(0).getDeal());

        DdsAdapter.releaseInternalMemory();
    }
}
