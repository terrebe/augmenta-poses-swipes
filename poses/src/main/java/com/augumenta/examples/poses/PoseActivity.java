/*
 * Copyright (c) 2012-2016 Augumenta Ltd. All rights reserved.
 *
 * This source code file is furnished under a limited license and may be used or
 * copied only in accordance with the terms of the license. Except as permitted
 * by the license, no part of this source code file may be  reproduced, stored in
 * a retrieval system, or transmitted, in any form or by  any means, electronic,
 * mechanical, recording, or otherwise, without the prior written permission of
 * Augumenta.
 *
 * This source code file contains proprietary information that is protected by
 * copyright. Certain parts of proprietary information is patent protected. The
 * content herein is furnished for informational use only, is subject to change
 * without notice, and should not be construed as a commitment by Augumenta.
 * Augumenta assumes no responsibility or liability for any errors or
 * inaccuracies that may appear in the informational content contained herein.
 * This source code file has not been thoroughly tested under all conditions.
 * Augumenta, therefore, does not guarantee or imply its reliability,
 * serviceability, or function.
 *
 */

package com.augumenta.examples.poses;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.augumenta.agapi.CameraFrameProvider;
import com.augumenta.agapi.AugumentaManager;
import com.augumenta.agapi.Poses;
import com.augumenta.agapi.HandPose;
import com.augumenta.agapi.HandPose.HandSide;
import com.augumenta.agapi.HandPoseEvent;
import com.augumenta.agapi.HandPoseListener;
import com.augumenta.agapi.HandTransitionEvent;
import com.augumenta.agapi.HandTransitionListener;

/**
 * PoseActivity demonstrates the basic usage of detecting hand poses and hand transitions
 * from devices default camera.
 */
public class PoseActivity extends Activity {
	private static final String TAG = PoseActivity.class.getSimpleName();

	// On Android 6.0 and later permissions are requested at runtime.
	// See more information about requesting permission at runtime at:
	// https://developer.android.com/training/permissions/requesting.html

	// Permission request code for CAMERA permission
	private static final int PERMISSION_REQUEST_CAMERA = 0;

	// SurfaceView for showing camera preview
	private SurfaceView mCameraPreview;
	// FrameLayout for showing pose images
	private FrameLayout mPoseFrame;
	// AugumentaManager instance
	private AugumentaManager mAugumentaManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pose);

		// Get preview SurfaceView from activity layout
		mCameraPreview = (SurfaceView) findViewById(R.id.preview);
		mPoseFrame = (FrameLayout) findViewById(R.id.poses);

		// There are two ways to use default CameraFrameProvider and use SurfaceView
		// as a camera preview.

		// 1) Create CameraFrameProvider before getting AugumentaManager instance and
		//    pass the provider as an argument when getting the AugumentaManager instance.

		// Create CameraFrameProvider and set our preview to it
		CameraFrameProvider cameraFrameProvider = new CameraFrameProvider();
		cameraFrameProvider.setCameraPreview(mCameraPreview);

		// In fast mode the camera provider will select the lowest
		// available preview resolution, this will increase the
		// performance of the application. However, in some devices
		// this has been a source of problems in low resolutions.
		//
		// cameraFrameProvider.setFastMode(true);

		// Create AugumentaManager and initialize it with our CameraFrameProvider
		try {
			mAugumentaManager = AugumentaManager.getInstance(this, cameraFrameProvider);
		} catch (IllegalStateException e) {
			// Something went wrong while authenticating license
			Toast.makeText(this, "License error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(TAG, "License error: " + e.getMessage());

			// Close the app before AugumentaManager is used, otherwise it will cause
			// NullPointerException when trying to use it.
			finish();
		}

		// 2) Get AugumentaManager instance without specifying frame provider and
		//    get the default CameraFrameProvider instance from it.
		/*
		// If no frame provider is specified, then default CameraFrameProvider is used.
		mAugumentaManager = AugumentaManager.getInstance();

		// Get the CameraFrameProvider instance from AugumentaManager
		CameraFrameProvider cameraFrameProvider = (CameraFrameProvider) mAugumentaManager.getFrameProvider();

		// Set SurfaceView to the CameraFrameProvider to be used as camera preview
		cameraFrameProvider.setCameraPreview(mCameraPreview);
		*/

		// To remove preview from the CameraFrameProvider, just set the camera preview to null.
		/*
		cameraFrameProvider.setCameraPreview(null);
		*/

		// You can register listeners at onCreate method in which case you don't need to unregister
		// listeners at onPause. In that case the registered poses are valid for the whole
		// life-cycle of the application.

		// It is possible to register same pose to multiple listeners or
		// multiple poses to same listener. It all depends on the use case.
	}

	/**
	 * Set screen brightness
	 * See http://developer.android.com/reference/android/view/WindowManager.LayoutParams.html#screenBrightness
	 * @param brightness screen brightness value
	 */
	private void setScreenBrightness(float brightness) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightness;
		getWindow().setAttributes(lp);
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

		// Set screen brightness to minimum to prevent over heating
		// the Google Glass
		// Note: On some devices setting the brightness to zero will
		// turn the screen off.
		// setScreenBrightness(0.1f);

		// There are few ways to register poses to listeners
		// Register Pose with both hands to the specified listener
		// (register P008 (thumb up) for both hands)
		mAugumentaManager.registerListener(mPoseListener, Poses.P008);

		/*
		// Register Pose with both hands simultaneously detected to the specified listener (register
		// P032 (fist) for both hands)
		mAugumentaManager.registerListener(mPoseListener, Poses.P032);
		mAugumentaManager.setDetectBothHands(Poses.P032, true);
		// Extend the search for smaller or more distant poses
		mAugumentaManager.setSearchSizeFactor(0.9);
		// Register Pose to be detected with a wider angle range to the
		// specified listener (register P001, (open palm))
		mAugumentaManager.registerListener(mPoseListener, Poses.P001, HandSide.ALL, 0, 90);
		// Enable global filtering of detected coordinates for poses
		mAugumentaManager.setFiltering(true);
		// Set the filtering factor, bigger the value slower the change of the coordinate
		mAugumentaManager.setFilterFactor(2.0f);
		*/
		// or as a short hand, you can just pass the Pose type as a second argument
		// to registerListener method
		// (register P001 (open palm) for both hands)
		mAugumentaManager.registerListener(mPoseListener, Poses.P001);

		// Register only the right hand to the specified listener
		// (register P016 (thumb down) only for right hand)
		mAugumentaManager.registerListener(mPoseListener, Poses.P016, HandSide.RIGHT);

		// Register only the left hand to the specified listener
		// (register P201 (rotated fist) only for left hand)
		mAugumentaManager.registerListener(mPoseListener, Poses.P201, HandSide.LEFT);

		// Register transition from P016 (thumb down) to P008 (thumb up) with wide range of
		// detection angles.
		mAugumentaManager.registerListener(mTransitionListener, Poses.P016,	Poses.P008);

		// Check if the Camera permission is already available
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
		    != PackageManager.PERMISSION_GRANTED) {
			// Camera permission has not been granted
			requestCameraPermission();
		} else {
			// Camera permission is already available
			// Start detection when activity is resumed
			startAugumentaManager();
		}
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		// Unregister all pose listeners
		mAugumentaManager.unregisterAllListeners();

		// It's also possible to unregister specific poses or listeners
		/*
		// To unregister only a specific pose from a specific listener, use:
		mAugumentaManager.unregisterListener(mPoseListener, new HandPose(Poses.P001));

		*/

		/*
		// To unregister all poses that are linked to a specific listener, use:
		mAugumentaManager.unregisterListener(mPoseListener);

		*/

		// Remember to stop the detection when the activity is paused.
		// Otherwise the camera is not released and other applications are unable to
		// open camera.
		mAugumentaManager.stop();

		// Reset the screen brightness
		setScreenBrightness(-1);
	}

	private void startAugumentaManager() {
		// Start detection when the activity is resumed
		if (!mAugumentaManager.start()) {
			// Failed to start detection, probably failed to open camera
			Toast.makeText(this, "Failed to open camera!", Toast.LENGTH_LONG).show();
			// Close activity
			finish();
		}
	}

	private void requestCameraPermission() {
		// Request CAMERA permission from user
		Log.d(TAG, "Requesting CAMERA permission");
		ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, PERMISSION_REQUEST_CAMERA);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CAMERA) {
			Log.d(TAG, "Received response for CAMERA permission");

			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "CAMERA permission granted, starting AugumentaManager");
				startAugumentaManager();
			} else {
				Log.d(TAG, "CAMERA permission not granted, exiting..");
				Toast.makeText(this, "Camera permission was not granted.", Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	/// Example_HandTransitionListener
	private HandTransitionListener mTransitionListener = new HandTransitionListener() {
		@Override
		public void onTransition(HandTransitionEvent handTransitionEvent) {
			Log.d(TAG, "onTransition: " + handTransitionEvent);
			// Close activity when "Thumb down" -> "Thumb up" transition is detected.
			PoseActivity.this.finish();
		}
	};

	// cache created ImageViews to SparceArray using pose event id as a key
	private SparseArray<ImageView> mPoseImageArray = new SparseArray<ImageView>();

	/**
	 * HandPoseListener that shows a pose image while pose is detected
	 */
	private HandPoseListener mPoseListener = new HandPoseListener() {
		@Override
		public void onDetected(final HandPoseEvent handPoseEvent, final boolean newdetect) {
			Log.d(TAG, "onDetected: " + handPoseEvent + " + " + newdetect);
			// If multiple poses are registered to a single listener,
			// you can check which pose triggered the event by examining
			// the handPoseEvent object

			// All interactions to the UI must be done in the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(newdetect) {
						// create new image view for detected pose
						ImageView image = new ImageView(PoseActivity.this);
						image.setImageResource(getImageResourceByPose(handPoseEvent.handpose.pose()));
						if (handPoseEvent.handpose.handside() == HandSide.RIGHT) {
							// mirror the pose image if right hand pose
							image.setRotationY(180);
						}
						// get the size of the pose
						// pose size and position values are relative to the frame size
						// (0, 0) is top-left corner and (1, 1) is bottom-right corner
						int w = (int) (handPoseEvent.rect.width() * mPoseFrame.getWidth());
						int h = (int) (handPoseEvent.rect.height() * mPoseFrame.getHeight());

						// set image size and position based upon the detected pose
						LayoutParams lp = new LayoutParams(w, h);
						lp.leftMargin = (int) (handPoseEvent.rect.centerX() * mPoseFrame.getWidth() - (w / 2));
						lp.topMargin = (int) (handPoseEvent.rect.centerY() * mPoseFrame.getHeight() - (h / 2));
						image.setLayoutParams(lp);

						// cache image with poses id, so it can be referenced later
						mPoseImageArray.put(handPoseEvent.id, image);

						// add image to the layout
						mPoseFrame.addView(image);
					} else { // Motion here
						ImageView image = mPoseImageArray.get(handPoseEvent.id);
						if (image != null) {
							// update image position and size
							int w = (int) (handPoseEvent.rect.width() * mPoseFrame.getWidth());
							int h = (int) (handPoseEvent.rect.height() * mPoseFrame.getHeight());

							LayoutParams lp = new LayoutParams(w, h);
							lp.leftMargin = (int) (handPoseEvent.rect.centerX() * mPoseFrame.getWidth() - (w / 2));
							lp.topMargin = (int) (handPoseEvent.rect.centerY() * mPoseFrame.getHeight() - (h / 2));
							image.setLayoutParams(lp);
						}
					}
				}
			});
		}

		@Override
		public void onLost(final HandPoseEvent handPoseEvent) {
			Log.d(TAG, "onLost: " + handPoseEvent);
			// All interactions to the UI must be done in the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// pose is not detected anymore
					// get the image with pose id and remove it from the layout
					ImageView image = mPoseImageArray.get(handPoseEvent.id);
					if (image != null) {
						mPoseFrame.removeView(image);
					}
				}
			});
		}

		@Override
		public void onMotion(final HandPoseEvent handPoseEvent) {
		}
	};

	/**
	 * Get pose image resource with Pose value
	 *
	 * @param pose pose
	 * @return Image resource of the pose
	 */
	private int getImageResourceByPose(int pose) {
		switch (pose) {
			case Poses.P001:
				return R.drawable.p001;
			case Poses.P008:
				return R.drawable.p008;
			case Poses.P016:
				return R.drawable.p016;
			case Poses.P201:
				return R.drawable.p201;
			default:
				return -1;
		}
	}
}
