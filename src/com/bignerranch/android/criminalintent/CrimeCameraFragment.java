package com.bignerranch.android.criminalintent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CrimeCameraFragment extends Fragment {
	private static final String TAG = "CrimeCameraFragment";
	
	//constant added for the extra to pass data for taken photo, setting a result
	public static final String EXTRA_PHOTO_FILENAME =
			"com.bignerdranch.android.criminialintent.photo_filename";
	
	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private View mProgressContainer;
	
	//for taking pictures and handling images
	private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
			//Display the progress indicator
			mProgressContainer.setVisibility(View.VISIBLE);
		}
	};
	
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			//Create a filename, create a unique string to use as a filename
			String filename = UUID.randomUUID().toString() + ".jpg";
			//Save the jpeg data to disk
			FileOutputStream os = null;
			boolean success = true;
			
			try {
				os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
				os.write(data);
			} catch (Exception e) {
				Log.e(TAG, "Error writing to file" + filename, e);
				success = false;
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (Exception e) {
					Log.e(TAG, "Error closing file" + filename, e);
					success = false;
				}
			}
			
			if (success) {
				//Log.i(TAG, "JPEG saved at" + filename);
				//set the photo filename on the result intent
				if (success) {
					Intent i = new Intent() ;
					i.putExtra(EXTRA_PHOTO_FILENAME, filename);
					getActivity().setResult(Activity.RESULT_OK, i);
				} else {
					getActivity().setResult(Activity.RESULT_CANCELED);
				}
			}
			//not exactly how it is in book, p.327 for reference if problems persist.
			getActivity().finish();
		}
	};
	
	@Override
	//SurfaceView provides an implementation of the SurfaceHolder interface
	@SuppressWarnings("deprecation")
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_crime_camera, parent, false);
		
		//insures that user wont interact with anything in photo-taking button twice
		mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
		mProgressContainer.setVisibility(View.INVISIBLE);
		
		Button takePictureButton = (Button)v
				.findViewById(R.id.crime_camera_takePictureButton);
		takePictureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//getActivity().finish();
				//modify listener for take button to call takepicture().
				if (mCamera != null) {
					mCamera.takePicture(mShutterCallback, null, mJpegCallback);
				}
			}
		});
		
		mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
		//SurfaceView
		SurfaceHolder holder = mSurfaceView.getHolder();
		//setType() and SURFACE_TYPE_PUSH_BUFFERS are both deprecated,
		//but are required for camera preview to work on pre 3.0 devices.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		//SurfaceHolder.callback is an interface that listens for events in the lifecycle of
		//surface so you can coordinate the surface with its client.
		holder.addCallback(new SurfaceHolder.Callback() {
			
			//This method is called when the view hierarchy that the surfaceview belongs to is put on
			//screen.  Where you connect the Surface with its client.
			public void surfaceCreated(SurfaceHolder holder) {
				//Tell camera to use this surface as its preview area
				try {
					if (mCamera != null) {
						mCamera.setPreviewDisplay(holder);
					}
				} catch (IOException exception) {
					Log.e(TAG, "Error setting up preview display", exception);
				}
			}
			//When the SurfaceView is removed from screen, the surface is destroyed.  This is where
			//you tell the Surfaces client to stop using surface.
			
			public void surfaceDestroyed(SurfaceHolder holder) {
				//We can no longer display on this surface, so stop the preview.
				if (mCamera != null) {
					mCamera.stopPreview();
				}
			}
			//When the surface is being displayed, this method is called.  informs you of pixel format.
			
			public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
				if(mCamera == null) return;
				
				//The surface has changed size; update the camera preview size
				Camera.Parameters parameters = mCamera.getParameters();
				//Size s = null; replaced with line below linked to algorithm at bottom
				Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), w, h);
				parameters.setPreviewSize(s.width, s.height);
				//Setting the picture size
				s = getBestSupportedSize(parameters.getSupportedPictureSizes(), w, h);
				parameters.setPictureSize(s.width, s.height);
				mCamera.setParameters(parameters);
				try {
					mCamera.startPreview();
				} catch (Exception e) {
					Log.e(TAG, "Could not start preview", e);
					mCamera.release();
					mCamera = null;
				}
			}
		});
		
		return v;
	}
	@TargetApi(9)
	@Override
	public void onResume() {
		super.onResume();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mCamera = Camera.open(0);
		} else {
			mCamera = Camera.open();
		}
			
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}
	/** A simple algorithm to get the largest size available. For a more 
	 * robust version, see CameraPreview.java in the ApiDemos sample app from Android*/
	private Size getBestSupportedSize(List<Size> sizes, int width, int height) {
		Size bestSize = sizes.get(0);
		int largestArea = bestSize.width * bestSize.height;
		for (Size s : sizes) {
			int area = s.width * s.height;
			if (area > largestArea) {
				bestSize = s;
				largestArea = area;
			}
		}
		return bestSize;
	}
}
