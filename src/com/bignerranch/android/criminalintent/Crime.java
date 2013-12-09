package com.bignerranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class Crime {
	//(A1)In order to sve the mCrimes array inJSON format, you have 
	//to be able to save individual instances of crime in JSON format.
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";
	private static final String JSON_SOLVED = "solved";
	private static final String JSON_DATE = "date";
	//Update class to hold a Photo object and serialize it to JSON
	private static final String JSON_PHOTO = "photo";
	//Adding suspect constant to model layer
	private static final String JSON_SUSPECT = "suspect";
	
	private UUID mId;
	private String mTitle;
	private Date mDate;
	private boolean mSolved;
	//update calss to hold a photo object and serizlize it to JSON
	private Photo mPhoto;
	//Suspect member variable
	private String mSuspect;
	
	public Crime() {
		//Generate unique identifier
		mId = UUID.randomUUID();
		mDate = new Date();
	}
	//this constructor accepts a JSONObject
	public Crime(JSONObject json) throws JSONException {
		mId = UUID.fromString(json.getString(JSON_ID));
		mTitle = json.getString(JSON_TITLE);
		mSolved = json.getBoolean(JSON_SOLVED);
		mDate = new Date(json.getLong(JSON_DATE));
	//next add a method in CriminalIntentJSONSerializer for loading crimes
		
		//Giving Crime a property photo
		if(json.has(JSON_PHOTO))
			mPhoto = new Photo(json.getJSONObject(JSON_PHOTO));
		//for suspect
		if(json.has(JSON_SUSPECT))
			mSuspect = json.getString(JSON_SUSPECT);
	}
	
	//(A2) - this code uses methods from JSONOnject class to handle the business of converting the data in a Crime
	//into something that can be written to a file as JSON.
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_ID, mId.toString());
		json.put(JSON_TITLE, mTitle);
		json.put(JSON_SOLVED, mSolved);
		json.put(JSON_DATE, mDate.getTime());
		//Giving Crime a property photo
		if (mPhoto != null)
			json.put(JSON_PHOTO, mPhoto.toJSON());
			//for suspect
			json.put(JSON_SUSPECT, mSuspect);
		return json;
	}
	
	@Override
	public String toString() {
		return mTitle;
	}

	public UUID getId() {
		return mId;
	}
	
	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public boolean isSolved() {
		return mSolved;
	}

	public void setSolved(boolean solved) {
		mSolved = solved;
	}
	//Giving crime a photo property, following two methods
	public Photo getPhoto() {
		return mPhoto;
	}
	public void setPhoto(Photo p) {
		mPhoto = p;
	}
	//Giving suspect accessor methods.
	public String getSuspect() {
		return mSuspect;
	}
	
	public void setSuspect(String suspect) {
		mSuspect = suspect;
		//now need to create a template report that can be configured with specific crimes details that can 
		//be replaced at runtime.
	}

}
