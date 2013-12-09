package com.bignerranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;



public class CrimeFragment extends Fragment {
	//Retrieving filename
	private static final String TAG = "CrimeFragment";
	
	public static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime.id";
	
	public static final String DIALOG_DATE = "date";
	private static final int REQUEST_DATE = 0;
	//Starting CrimeCameraACtivity for a result
	private static final int REQUEST_PHOTO = 1;
	//Field for suspect button from contacts
	private static final int REQUEST_CONTACT = 2;
	//Show image Fragment
	private static final String DIALOG_IMAGE = "image";
	
	private Crime mCrime;
	private EditText mTitleField;
	private Button mDateButton;
	private CheckBox mSolvedCheckBox;
	private ImageButton mPhotoButton;
	private ImageView mPhotoView;
	//suspect field
	private Button mSuspectButton;
	//For CrimeFragmentCallbacks
	private Callbacks mCallbacks;
	
	//Required interface for hosting activities
	public interface Callbacks {
		void onCrimeUpdated(Crime crime);
	}
	
	@Override 
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallbacks = (Callbacks)activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
		//then implement CrimeFragment.callbacks in CrimeListACtivity to reload this list in onCrimeUpdated(crime)
	}
	
	public static CrimeFragment newInstance(UUID crimeId) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_CRIME_ID, crimeId);
		
		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//mCrime = new Crime();
		//UUID crimeId = (UUID)getActivity().getIntent().getSerializableExtra(EXTRA_CRIME_ID);
		UUID crimeId = (UUID)getArguments().getSerializable(EXTRA_CRIME_ID);
		
		mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
		
		//The first thing to do is tell the FragmentManager that this fragment will be implementing callbacks 
		//on behalf of the activity
		setHasOptionsMenu(true);
	}
	
	//Code that sets button's text is identical to code in on create view, avoid setting in two places
	//on createview
	//onactivityresult
	public void updateDate() {
		mDateButton.setText(mCrime.getDate().toString());
	}
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
		
		//Method is from API level 11 so we need to wrap it to keep it safe
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//If there is not named parent, then dont want to display up caret
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		
		mTitleField = (EditText)v.findViewById(R.id.crime_title);
		mTitleField.setText(mCrime.getTitle());
		mTitleField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence c, int start, int before, int count) {
				mCrime.setTitle(c.toString());
				//call to update crimes title or solved status
				mCallbacks.onCrimeUpdated(mCrime);
				getActivity().setTitle(mCrime.getTitle());
			}
			public void beforeTextChanged(
					CharSequence c, int start, int count, int after) {
				//This space intentionally left blank
			}
			public void afterTextChanged(Editable c) {
				//This one too
			}
		});
		
		mDateButton = (Button)v.findViewById(R.id.crime_date);
		//mDateButton.setText(mCrime.getDate().toString()); 
		//Encapsulated in private method above oncreateview, updateDate inputted 
		updateDate();
		//mDateButton.setEnabled(false);
		mDateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				FragmentManager fm = getActivity()
						.getSupportFragmentManager();
				//DatePickerFragment dialog = new DatePickerFragment();
				//Removed call to DatePickerFragment constructor and replaced 
				//with a call to datepickerfragment.newinstance(date)
				DatePickerFragment dialog = DatePickerFragment
						.newInstance(mCrime.getDate());
				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				dialog.show(fm, DIALOG_DATE);
			}
		});
		
		
		mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
		mSolvedCheckBox.setChecked(mCrime.isSolved());
		mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//Set the crime's solved property
				mCrime.setSolved(isChecked);
				//Call to set solved status in two pane
				mCallbacks.onCrimeUpdated(mCrime);
			}
		});
		
		//Starting CrimeCameraActivityfrom button
		mPhotoButton = (ImageButton)v.findViewById(R.id.crime_imageButton);
		mPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), CrimeCameraActivity.class);
				//startActivity(i);
				//Starting Crime cameraACtivity for Result
				//CrimeCameraFragment will put the filename in an intent extra and pass it into 
				//CrimeCameraActivity.setResult.
				startActivityForResult(i, REQUEST_PHOTO);
			}
		});
		
		//Configuring the imageButton
		mPhotoView = (ImageView) v.findViewById(R.id.crime_imageView);
		//Second step from ImageFragment class, onCreateView/OnDestroyView methods
		mPhotoView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Photo p = mCrime.getPhoto();
				if (p == null)
					return;
				
				FragmentManager fm = getActivity()
						.getSupportFragmentManager();
				String path = getActivity()
						.getFileStreamPath(p.getFilename()).getAbsolutePath();
				ImageFragment.newInstance(path)
				.show(fm, DIALOG_IMAGE);
			}
		});
		
		//If camera is not available, disable camera functionality
		PackageManager pm = getActivity().getPackageManager();
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) &&
				!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
			mPhotoButton.setEnabled(false);
		}
		
		//implicit intent to send a crime report
		Button reportButton = (Button)v.findViewById(R.id.crime_reportButton);
		reportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
				i.putExtra(Intent.EXTRA_SUBJECT, 
						getString(R.string.crime_report_subject));
				//using a chooser, essentially a title at the top of the send menu
				i = Intent.createChooser(i, getString(R.string.send_report));
				startActivity(i);
			}
			//use an intent constructor that accepts a string that is a constant defining action
		});
		//Listener with implicit intent
		mSuspectButton = (Button)v.findViewById(R.id.crime_suspectButton);
		mSuspectButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, 
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(i, REQUEST_CONTACT);
			}
		});
		
		if (mCrime.getSuspect() != null) {
			mSuspectButton.setText(mCrime.getSuspect());
		//Finally get the data from the contacts list
		}
		
		return v;
	}
	//private method that sets a scaled image on imageview
	private void showPhoto() {
		//(Re) set the image buttons image based on our photo
		Photo p = mCrime.getPhoto();
		BitmapDrawable b = null;
		if (p != null) {
			String path = getActivity()
					.getFileStreamPath(p.getFilename()).getAbsolutePath();
			b = PictureUtils.getScaledDrawable(getActivity(), path);
		}
		mPhotoView.setImageDrawable(b);
		//next step: add an implementation of onStart that calls showPhoto method to have
		//the photo ready as soon as CrimeFragments view becomes visible to the user
	}
	//Onstart implementation
	@Override
	public void onStart() {
		super.onStart();
		showPhoto();
		//net step, in CrimeFragment.onActivityResult, call showphoto to ensure image will be visible
		//when the user returns from CrimeCameraActivity.
	}
	
	//To be sure there are no finalizer bugs for large images, complete cleanup from pictureUtils class, 
	//CleanImageView method
	@Override
	public void onStop() {
		super.onStop();
		PictureUtils.cleanImageView(mPhotoView);
		//Loading images in onstart(above) and unloading them in onStop is good practice.
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) return;
		if (requestCode == REQUEST_DATE) {
			Date date = (Date)data
					.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			mCrime.setDate(date);
			//mDateButton.setText(mCrime.getDate().toString());
			//encapsulated in private method above oncreateview, updateDate added below
			//Being neighborly and letting onActivity know that Date has been updated
			mCallbacks.onCrimeUpdated(mCrime);
			updateDate();
			//Retrieving filename
		} else if (requestCode == REQUEST_PHOTO) {
			//Create a new photo object and attach it to the crime
			String filename = data
					.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
			if (filename != null) {
				//Log.i(TAG, "filename:" + filename);
				//Now that it has a filename, much to be done with it
				Photo p = new Photo (filename);
				mCrime.setPhoto(p);
				//Log.i(TAG, "Crime: " + mCrime.getTitle() + " has a photo");
				//Calling showPhoto to ensure that image will be visible
				//again neighborly
				mCallbacks.onCrimeUpdated(mCrime);
				showPhoto();
			}
		} else if (requestCode == REQUEST_CONTACT) {
			Uri contactUri = data.getData();
			
			//Specify which fields you want your query to return values for
			String[] queryFields = new String[] {
					ContactsContract.Contacts.DISPLAY_NAME
			};
			
			//Perform your query - contactUri is like a "where"
			Cursor c = getActivity().getContentResolver()
					.query(contactUri, queryFields, null, null, null);
			
			//Double-check that you actually got results
			if(c.getCount() == 0) {
				c.close();
				return;
			}
			
			//Pull out the first column of the first row of data - that is your suspects name
			c.moveToFirst();
			String suspect = c.getString(0);
			mCrime.setSuspect(suspect);
			//last time neighborly
			mCallbacks.onCrimeUpdated(mCrime);
			mSuspectButton.setText(suspect);
			c.close();
		}
		
		
		//started activity with action_pick, receive an intent via onActivityResult, includes data URI that points
		//at a single contact that a user picks
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				//Checks metadata tag in manifest, then navigates to parent activity
				if (NavUtils.getParentActivityName(getActivity()) != null) {
					NavUtils.navigateUpFromSameTask(getActivity());
				}
				return true;
				default:
					return super.onOptionsItemSelected(item);
		}
	}
	//Where should you call save crimes, onPause is safest bet.
	@Override
	public void onPause() {
		super.onPause();
		CrimeLab.get(getActivity()).saveCrimes();
	}
	//Add a method that creates four strings and then pieces them together and returns a complete report.
	private String getCrimeReport() {
		String solvedString = null;
		if (mCrime.isSolved()) {
			solvedString = getString(R.string.crime_report_solved);
		} else {
			solvedString = getString(R.string.crime_report_unsolved);
		}
		
		String dateFormat = "EEE, MMM dd";
		String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
		
		String suspect = mCrime.getSuspect();
		if (suspect == null) {
			suspect = getString(R.string.crime_report_no_suspect);
		} else {
			suspect = getString(R.string.crime_report_suspect);
		}
		
		String report = getString(R.string.crime_report,
				mCrime.getTitle(), dateString, solvedString, suspect);
		return report;
	}
}
