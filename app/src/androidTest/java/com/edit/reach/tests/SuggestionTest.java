package com.edit.reach.tests;

import android.util.Log;
import com.edit.reach.model.interfaces.SuggestionListener;
import com.edit.reach.model.Suggestion;
import junit.framework.TestCase;

import java.util.List;

public class SuggestionTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSearchForAddresses() throws Exception {
        Log.d("TestClass", "test");
        SuggestionListener suggestionListener = new SuggestionListener() {
            @Override
            public void onGetSuccess(List<String> results) {
                for (String s : results) {
                    Log.d("TestClass", s);
                }
            }
        };

        Suggestion ss = new Suggestion(suggestionListener);
        ss.searchForAddresses("new yo");
        Thread.sleep(5000);
        Log.d("TestClass", "Test done");

    }
}