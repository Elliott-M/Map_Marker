package com.meh.tabbedlayout;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabbedLayoutActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, Maps.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        intent = new Intent().setClass(this, Maps.class);
        spec = tabHost.newTabSpec("Maps").setIndicator("Map",
                          res.getDrawable(R.drawable.mapicon))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, Notepadv3.class);
        spec = tabHost.newTabSpec("Marks").setIndicator("Marks",
                          res.getDrawable(R.drawable.ic_tab_artists))
                      .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }
}