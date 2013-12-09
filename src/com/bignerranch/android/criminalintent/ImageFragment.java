package com.bignerranch.android.criminalintent;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

//Showing a larger image in a DialogFragment
public class ImageFragment extends DialogFragment {
	public static final String EXTRA_IMAGE_PATH = 
			"com.bignerdranch.android.criminalintent.image.path";
	
	public static ImageFragment newInstance(String imagePath) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_IMAGE_PATH, imagePath);
		
		ImageFragment fragment = new ImageFragment();
		fragment.setArguments(args);
		fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		
		return fragment;
	}
	//not need title or buttons, cleaner.  Create imageview from scratch, retrieve the path from its arguments
	//then get a scaled version of the image and set it on imageview.  Override onDestroy to free up memory.
	
	private ImageView mImageView;
	
	@Override 
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup parent, Bundle savedInstanceState) {
		mImageView = new ImageView(getActivity());
		String path = (String)getArguments().getSerializable(EXTRA_IMAGE_PATH);
		BitmapDrawable image = PictureUtils.getScaledDrawable(getActivity(), path);
		
		mImageView.setImageDrawable(image);
		
		return mImageView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		PictureUtils.cleanImageView(mImageView);
		//finally need to show this dialog from CrimeFragment, add a listener to mPhotoView, within listener
		//create an instance of ImageFragment and add it to CrimePagerActivity's FragmentManager by calling show
		//on imageFragment
	}

}
