package com.bignerranch.android.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

public class CrimePagerActivity extends FragmentActivity 
//Remember that any activity that hosts CrimeFragment must implement CrimeFragment.Callbacks. Just add empty
//implementation where onCrimeUpdated does nothing.
implements CrimeFragment.Callbacks{
	private ViewPager mViewPager;
	private ArrayList<Crime> mCrimes; //this line retrieves data set from crime lab
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.viewPager);
		setContentView(mViewPager);
		
		mCrimes = CrimeLab.get(this).getCrimes();
		
		FragmentManager fm = getSupportFragmentManager(); // get the activities instance of fragment manager
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm){ //set adapter to be an unnamed instance of FSPA(agent), manages convo w/ ViewPager
			@Override
			public int getCount() { //returns how many items are in the array list
				return mCrimes.size();
			}
			//What is agent doing? ADding fragments you return to your activity and helping ViewPAger identify fragments views to be placed correctly.
			@Override
			public Fragment getItem(int pos) { //where magic happens, fetches the crime instance for the given position in the dataset, creates and returns
				//properly configured CrimeFragment.
				Crime crime = mCrimes.get(pos);
				return CrimeFragment.newInstance(crime.getId());
			}
		});
		
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageScrollStateChanged(int state) { }
			
			public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) { }
			
			public void onPageSelected(int pos) {
				Crime crime = mCrimes.get(pos);
				if (crime.getTitle() != null) {
					setTitle(crime.getTitle());
				}
			}
		});
		
		UUID crimeId = (UUID)getIntent()
				.getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
		for (int i = 0; i < mCrimes.size(); i++) {
			if(mCrimes.get(i).getId().equals(crimeId)) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}
	}

	@Override
	public void onCrimeUpdated(Crime crime) {
		// TODO Auto-generated method stub
		
	}

}
