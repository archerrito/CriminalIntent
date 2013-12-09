package com.bignerranch.android.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class CrimeListFragment extends ListFragment {
	
	private static final String TAG = "CrimeListFragment";
	// (A3)This handles the rotation for the subtitle, as well as the code in on Create
	private boolean mSubtitleVisible;
	private ArrayList<Crime> mCrimes;
	//adding callbacks for two pane viewing on tablets
	private Callbacks mCallbacks;
	
	//Required interdace for hosting activities
	public interface Callbacks {
		
		void onCrimeSelected(Crime crime);
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
	//now crimesListFragment has a way to call methods on its hosting activity
		//Next in CrimList ACtivity, implement CrimeListFragment.Callbacks
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//fragment manager responsible for calling oncreateoptionsmenu, when receives callback from OS
		//must explicitly tell the Fragment manager that fragment should receive a call.
		setHasOptionsMenu(true);
		
		getActivity().setTitle(R.string.crimes_title);
		mCrimes = CrimeLab.get(getActivity()).getCrimes();
		
		//ArrayAdapter<Crime> adapter =
			//	new ArrayAdapter<Crime>(getActivity(),
				//						android.R.layout.simple_list_item_1,
					//					mCrimes);
		//Replaced with
		CrimeAdapter adapter = new CrimeAdapter(mCrimes);
		setListAdapter(adapter);
		
		//(A4)This helps subtitle handle rotation
		setRetainInstance(true);
		mSubtitleVisible = false;
		
	}
	
	//(A7) need to check to see if subtitle should be shown, set the subtitle if msubtitlevisible is true
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mSubtitleVisible) {
				getActivity().getActionBar().setSubtitle(R.string.subtitle);
			}
		}
		//By default, long-press does not trigger creation of context menu, must register a view by calling 
		//following fragment method
		ListView listView = (ListView)v.findViewById(android.R.id.list);
		//the r.id.list is used to retrieve the ListView managed by listfragment
		//first step is; following code allows for enabling multiple selection in contextual action bar
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			//use floating context menus on Froyo and gingerbread
			registerForContextMenu(listView);
		} else {
			//Use contextual action bar on Honeycomb and higher
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			//Next step is to set a listener on ListView that implements MultiChoiceModeListener in onCreateView
			//Right below
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
					//required but not needed
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) { }
				//required but not needed

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.crime_list_item_context, menu);
					return true;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
						case R.id.menu_item_delete_crime:
							CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
							CrimeLab crimeLab = CrimeLab.get(getActivity());
							for (int i=adapter.getCount() -1; i >=0; i--) {
								if (getListView().isItemChecked(i)) {
									crimeLab.deleteCrime(adapter.getItem(i));
								}
							}
							mode.finish();
							adapter.notifyDataSetChanged();
							crimeLab.saveCrimes();
							return true;
						default:
							return false;
					}

				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) { }
				//Required but not needed
			});
		}
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//Crime c = (Crime)(getListAdapter()).getItem(position);
		//replace with
		Crime c = ((CrimeAdapter)getListAdapter()).getItem(position);		
		//Log.d(TAG, c.getTitle() + "was clicked");
		//Starting an activity from a fragment, p.191.
		
		////Start CrimeActivity
		//Intent i = new Intent(getActivity(), CrimeActivity.class);
		//Start CrimePagerActivity with this crime
		
		//Intent i = new Intent(getActivity(), CrimePagerActivity.class);
		//i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
		//startActivity(i);
		
		//called CrimePAgerActivity, above, so we replace with mCallbacks to get two pane view.
		mCallbacks.onCrimeSelected(c);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}
	//Placed after onResume, creates options menu, and responds to user selection
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_list, menu);
		//(A8) Check the subtitles state in method above to make sure displaying correct menu item title
		MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
		if (mSubtitleVisible && showSubtitle != null) {
			showSubtitle.setTitle(R.string.hide_subtitle);
		}
	}
	
	// when user presses item, fragment receives callback to method below.  This method receives instance of MenuItem that 
	//describes users selection. Create a new crime, add it to crimelab, start instance of CrimePagerActivity to edit new crime.
	//Method returns a boolean value, returns true to indicate no further processing
	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_item_new_crime:
				Crime crime = new Crime();
				CrimeLab.get(getActivity()).addCrime(crime);
				Intent i = new Intent(getActivity(), CrimePagerActivity.class);
				i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
				startActivityForResult(i, 0);
				return true;
			//in response to added item to menu-v11, to show subtitles
			case R.id.menu_item_show_subtitle:
				//(A1)checks for presence of subtitle and when selected, takes action
				if 	(getActivity().getActionBar().getSubtitle() == null) {
					getActivity().getActionBar().setSubtitle(R.string.subtitle);
					//(A5)Response to item selection
					mSubtitleVisible = true;
					//(A2)this 
					item.setTitle(R.string.hide_subtitle);
				} else {
					getActivity().getActionBar().setSubtitle(null);
					//(A6) REsponse to item selection
					mSubtitleVisible = false;
					item.setTitle(R.string.show_subtitle);	
				}
				//Till here and above.  If action 
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	//(B1)Create floating context menu first, inflate menu resource, populate context menu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
	}
	
	//Second step from crimelab delete crime
	@Override
	public boolean onContextItemSelected (MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int position = info.position;
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		Crime crime = adapter.getItem(position);
		
		switch(item.getItemId()) {
		case R.id.menu_item_delete_crime:
			CrimeLab.get(getActivity()).deleteCrime(crime);
			adapter.notifyDataSetChanged();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private class CrimeAdapter extends ArrayAdapter<Crime> {
		
		public CrimeAdapter(ArrayList<Crime> crimes) {
			super(getActivity(), 0, crimes);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//If we weren't given a view, inflate one
			if(convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.list_item_crime,  null);
			}
			
			//Configure the view for this crime
			Crime c = getItem(position);
			
			TextView titleTextView =
					(TextView)convertView.findViewById(R.id.crime_list_item_titleTextView);
			titleTextView.setText(c.getTitle());
			TextView dateTextView =
					(TextView)convertView.findViewById(R.id.crime_list_item_dateTextView);
			dateTextView.setText(c.getDate().toString());
			CheckBox solvedCheckBox = 
					(CheckBox)convertView.findViewById(R.id.crime_list_item_solvedCheckBox);
			solvedCheckBox.setChecked(c.isSolved());
			
			return convertView;
		}
	}
	//Implementing CrimeFragment callbacks
	//method to be called to reload CrimeListFragments list
	public void updateUI() {
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}
}
