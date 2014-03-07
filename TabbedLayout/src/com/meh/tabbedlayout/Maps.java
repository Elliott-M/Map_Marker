package com.meh.tabbedlayout;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Maps extends MapActivity {
	/** Called when the activity is first created. */
	
	private static final int INSERT_ID = Menu.FIRST;
	private static final int ACTIVITY_CREATE=0;
	
	private NotesDbAdapter mDbHelper;
	private Cursor notesCursor;
	MapView mapView;
	
	private boolean satellite = false;
	
	double lat = 0;
	double lon = 0;
	
	@Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(true);

        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
        Helloitemizedoverlay itemizedoverlay = new Helloitemizedoverlay(drawable, this);
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        notesCursor = mDbHelper.fetchAllNotes();
        //startManagingCursor(notesCursor);
        notesCursor.moveToFirst();
        while(notesCursor.isAfterLast() != true){
        	String title = notesCursor.getString(notesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
        	String body = notesCursor.getString(notesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
        	String noteLat = notesCursor.getString(notesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_LAT));
            String noteLon = notesCursor.getString(notesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_LON));
            
            int latSub = noteLat.length();
            int lonSub = noteLon.length();
            if(latSub > 9){
            	latSub = 9;
            	lonSub = 9;
            }
            if(noteLat.charAt(0) == '-'){
            	latSub = 10;
            }
            if(noteLon.charAt(0) == '-'){
            	lonSub = 10;
            }
            noteLat = noteLat.substring(0,latSub);
            noteLon = noteLon.substring(0,lonSub);
            noteLat = noteLat.replace(".", "");
            noteLon = noteLon.replace(".", "");
            int theLat = Integer.parseInt(noteLat);
            int theLon = Integer.parseInt(noteLon);
            
        	GeoPoint point = new GeoPoint(theLat,theLon);
        	OverlayItem overlayitem = new OverlayItem(point, title, body);
        	itemizedoverlay.addOverlay(overlayitem);
        	mapOverlays.add(itemizedoverlay);
        	
        	//mDbHelper.close();
        	notesCursor.moveToNext();
    	}
        //notesCursor.close();
        mDbHelper.close();
        
        LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
    }
    
    public class MyLocationListener implements LocationListener{
    	
    	public void onLocationChanged(Location loc){
    	loc.getLatitude();
    	loc.getLongitude();
    	lat = loc.getLatitude();
    	lon = loc.getLongitude();
    	}
    
    	public void onProviderDisabled(String provider){
    		Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_LONG ).show();
    	}
    
    	public void onProviderEnabled(String provider){
    		Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_LONG).show();
    	}
    
    	public void onStatusChanged(String provider, int status, Bundle extras){
    	}
    }/* End of Class MyLocationListener */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.mark);
        menu.add(0, INSERT_ID+1, 0, R.string.toggle);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
            case INSERT_ID+1:
                satelliteSwitch();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
    
    private void satelliteSwitch(){
    	if(satellite == true){
    		mapView.setSatellite(true);
    		satellite = false;
    	} else {
    		mapView.setSatellite(false);
    		satellite = true;
    	}
    }
    
    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
 
        NoteEdit.currentLat = lat;
        NoteEdit.currentLon = lon;
        
 ///       Application myApp = getApplicationObject();
 ///       myApp.lathold = 

        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
}
