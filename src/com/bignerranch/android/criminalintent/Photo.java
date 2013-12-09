package com.bignerranch.android.criminalintent;

import org.json.JSONException;
import org.json.JSONObject;

//notice that photo class has two constructors, first creates a photo from a given filename
//second is a JSON serialization method that Crime will use when saving and loading its property of type photo.
public class Photo {
	private static final String JSON_FILENAME = "filename";
	
	private String mFilename;
	
	/*Create a photo representing an existing fil on disk */
	public Photo (String filename) {
		mFilename = filename;
	}
	public Photo (JSONObject json) throws JSONException {
		mFilename = json.getString(JSON_FILENAME);
	}
	
	public JSONObject toJSON() throws JSONException {
	JSONObject json = new JSONObject();
	json.put(JSON_FILENAME, mFilename);
	return json;
	}	
	
	public String getFilename() {
		return mFilename;
	}
}
