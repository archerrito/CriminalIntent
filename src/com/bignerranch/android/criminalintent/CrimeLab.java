package com.bignerranch.android.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

public class CrimeLab {
	//(A1)Saving changes persistently
	private static final String TAG = "Crimelab";
	private static final String FILENAME = "crimes.json";
	
	
	private ArrayList<Crime> mCrimes;
	//(A2)
	private CriminalIntentJSONSerializer mSerializer;
	
	private static CrimeLab sCrimeLab;
	private Context mAppContext;
	// (A2)Although these two above were already in play
	
	private CrimeLab(Context appContext) {
		appContext = mAppContext; //not sure what the issue here is
		mSerializer = new CriminalIntentJSONSerializer(mAppContext, FILENAME);
		//No longer needed mCrimes = new ArrayList<Crime>();
		try {
			mCrimes = mSerializer.LoadCrimes();
		} catch (Exception e) {
			mCrimes = new ArrayList<Crime>();
			Log.e(TAG, "Error loading crimes: ", e);
		}
		
		/*
		for (int i = 0; i < 100; i++) {
			Crime c = new Crime();
			c.setTitle("Crime #" + i);
			c.setSolved(i % 2 == 0); //Every other one
			mCrimes.add(c);
		} //No longer necessary since we are able to add crimes freely!
		//When the user presses, fragment receives a callback to the method onOptionsItem Selected
		 * Receives an instance of MenuItem that describes the users selection
		*/
	}
	public static CrimeLab get(Context c) {
		if (sCrimeLab == null) {
			sCrimeLab = new CrimeLab(c.getApplicationContext());
		}
		return sCrimeLab;
	}
	
	//To respond to a user pressing a new crime, need a way to add a new crime to list of crimes
	//need method that adds a crime to the array list
	public void addCrime(Crime c) {
		mCrimes.add(c);
	}
	
	//Need a method that can delete a crime from the model
	public void deleteCrime(Crime c) {
		mCrimes.remove(c);
		//next step is to respond to menu item sele3ction in onContextmenuItemSElected in CrimeListFragment
	}
	
	//(A3)
	public boolean saveCrimes() {
		try {
			mSerializer.saveCrimes(mCrimes);
			Log.d(TAG, "crimes saved to file");
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Error saving crimes: ", e);
			return false;
		}
	}
	
	public ArrayList<Crime> getCrimes() {
		return mCrimes;
	}
	
	public Crime getCrime(UUID id) {
		for (Crime c : mCrimes) {
			if (c.getId().equals(id))
				return c;
		}
		return null;
	}
}
