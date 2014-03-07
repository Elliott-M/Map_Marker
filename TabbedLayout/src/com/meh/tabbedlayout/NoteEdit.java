/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.meh.tabbedlayout;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NoteEdit extends Activity {

    private EditText mTitleText;
    private EditText mBodyText;
    private EditText mLatText;
    private EditText mLonText;
    private EditText mDateText;
    private EditText mTimeText;
    private String mPhotoUriText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;
    
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
	public Uri fileUri;
	ImageView thePhoto;
	Boolean photoAvailable = false;
	Boolean fromCamera = false;
	Boolean cameraCheck = false;
	String fileName;
	
	public static double currentLat;
	public static double currentLon;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
    	int date = c.get(Calendar.DATE);
    	int year = c.get(Calendar.YEAR);
    	int hour = c.get(Calendar.HOUR);
    	int minute = c.get(Calendar.MINUTE);
    	String zeroMinute = "";

        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mLatText = (EditText) findViewById(R.id.lat);
        mLonText = (EditText) findViewById(R.id.lon);
        mDateText = (EditText) findViewById(R.id.date);
        mTimeText = (EditText) findViewById(R.id.time);
        
        mTitleText.setText("Untitled");
        mLatText.setText("" + currentLat);
        mLonText.setText("" + currentLon);
    	mDateText.setText((month+1) + "/" + date + "/" + year);
    	if(minute < 10){
    		zeroMinute = "0";
    	} else {
    		zeroMinute = "";
    	}
    	if(hour == 0){
    		hour = 12;
    	}
    	mTimeText.setText(hour + ":" + zeroMinute + minute);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        
        thePhoto = new ImageView(this);
        thePhoto = (ImageView) findViewById(R.id.thisPhoto);
        //fileUri = Uri.parse(mPhotoUriText);
        //thePhoto.setImageURI(fileUri);

        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID) : null;
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
            		note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            mLatText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_LAT)));
            mLonText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_LON)));
            mDateText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)));
            mTimeText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME)));
            mPhotoUriText = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_PHOTO));
            
            if(mPhotoUriText != null){
            	photoAvailable = true;
            	Uri uri = Uri.parse(mPhotoUriText);
            	thePhoto.setImageURI(uri);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String lattitude = mLatText.getText().toString();
        String longitude = mLonText.getText().toString();
        String date = mDateText.getText().toString();
        String time = mTimeText.getText().toString();
        String photoUri = mPhotoUriText;

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, lattitude, longitude, date, time, photoUri);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, lattitude, longitude, date, time, photoUri);
        }
    }
    
    public void takePicture(View view){
    	Intent getPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	//fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
    	fromCamera = true;
        startActivityForResult(getPicture, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    
    public void pickPicture(View view){
    	Intent pickPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
    	pickPictureIntent.setType("image/*");
    	startActivityForResult(pickPictureIntent, 1);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK /*&& fromCamera == false*/) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                fileUri = data.getData();
                thePhoto.setImageURI(fileUri);
                mPhotoUriText = "" + fileUri;
                photoAvailable = true;
                cameraCheck = false;
            } else if (resultCode == RESULT_CANCELED) {
            	Toast.makeText(this, "Cancelled!", Toast.LENGTH_LONG).show();
            } else {
            	//Toast.makeText(this, "Error: Photo Uri not saved.", Toast.LENGTH_LONG).show();
            }
            /*if(fromCamera == true){
            	//this sorts the gallery newest to oldest:
                final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
                final String imageOrderBy = MediaStore.Images.Media._ID +" DESC";

                Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
                if(imageCursor.moveToFirst()){
                    
                fileName = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                imageCursor.close();
                
                fileUri = Uri.parse(new File(fileName).toString());
                mPhotoUriText = "" + fileUri;
                thePhoto.setImageURI(fileUri);
                photoAvailable = true;
                cameraCheck = true;
            } else {
                 fileName = "nofile";
            }
            	fromCamera = false;
            }*/
    }
    
    public void testFunction(View view){
    	Toast.makeText(this, "Test:\n" + fileUri + "\n" + mPhotoUriText, Toast.LENGTH_LONG).show();
    }
    
    public void goToPhoto(View v){
    	if(photoAvailable == true /*&& cameraCheck == false*/){
    		Intent i = new Intent();
    		i.setAction(Intent.ACTION_VIEW);
    		i.setDataAndType(Uri.parse(mPhotoUriText), "image/*");
    		startActivity(i);
    	}
    	/*if(photoAvailable == true && cameraCheck == true){
    		Intent i = new Intent();
    		i.setAction(Intent.ACTION_VIEW);
    		i.setDataAndType(Uri.parse(new File(mPhotoUriText).toString()), "image/*");
    		startActivity(i);
    	}*/
    }

}
