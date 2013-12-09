package com.bignerranch.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class CrimeListActivity extends SingleFragmentActivity 
	//for implementing callbacks
	implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks { //p.370, context missing

	@Override
	protected Fragment createFragment() {
		return new CrimeListFragment();
	}
	//Able to test activity_twopane.xml here
	@Override
	protected int getLayoutResId() {
		//return R.layout.activity_twopane;
		//changed because of Alias id
		return R.layout.activity_masterdetail;
	}
	
	public void onCrimeSelected (Crime crime) {
		//onCrimeSelected(Crime) handles the selection of a crime in either interface.
		if (findViewById(R.id.detailFragmentContainer) == null) {
			//start an instance of CrimePagerActivity
			Intent i = new Intent(this, CrimePagerActivity.class);
			i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
			startActivity(i);
		} else {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			
			Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
			Fragment newDetail = CrimeFragment.newInstance(crime.getId());
			
			if (oldDetail != null) {
				ft.remove(oldDetail);
			}
			
			ft.add(R.id.detailFragmentContainer, newDetail);
			ft.commit();
		//finally, call onCrimeSelected(Crime) in the places where you currently start a new CrimePagerActivity.
		}
	}
	
	//implement CrimeFragment.Callbacks here to reload the list
	@Override
	public void onCrimeUpdated(Crime crime) {
		FragmentManager fm = getSupportFragmentManager();
		CrimeListFragment listFragment = (CrimeListFragment)
				fm.findFragmentById(R.id.fragmentContainer);
		listFragment.updateUI();
		//in CrimeFragment add calls to onCrimeUpdated(crime) when a crimes title or solved status changes.
	}
}
