package com.dynamsoft.tessocr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class OCRActivity extends Activity implements OnClickListener {
	private TessOCR mTessOCR;
	private TextView mResult;
	private ProgressDialog mProgressDialog;
	private ImageView mImage;
	private Button mButtonGallery, mButtonCamera;
	private String mCurrentPhotoPath;
	private static final int REQUEST_TAKE_PHOTO = 1;
	private static final int REQUEST_PICK_PHOTO = 2;
	private EditText mEtxThreshold;
	private Button mBtnInvalidate;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					//Log.i(TAG, "OpenCV loaded successfully");
					//System.loadLibrary("native_activity");
//					Intent intent = new Intent(OCRActivity.this, android.app.NativeActivity.class);
//					OCRActivity.this.startActivity(intent);
//					OCRActivity.this.finish();
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mResult = (TextView) findViewById(R.id.tv_result);
		mImage = (ImageView) findViewById(R.id.image);
		mButtonGallery = (Button) findViewById(R.id.bt_gallery);
		mButtonGallery.setOnClickListener(this);
		mButtonCamera = (Button) findViewById(R.id.bt_camera);
		mButtonCamera.setOnClickListener(this);
		mTessOCR = new TessOCR(this ,(ImageView) findViewById(R.id.imageView_gray), (ImageView) findViewById(R.id.imageView_bin));
		mEtxThreshold = (EditText)findViewById(R.id.editText_threshold);
		mBtnInvalidate = (Button)findViewById(R.id.button_invalidate);
		mBtnInvalidate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int value = Integer.valueOf(mEtxThreshold.getEditableText().toString());
				mTessOCR.changeParam(value);
			}
		});
	}

	private void uriOCR(Uri uri) {
		if (uri != null) {
			InputStream is = null;
			try {
				is = getContentResolver().openInputStream(uri);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				mImage.setImageBitmap(bitmap);
				doOCR(bitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		Intent intent = getIntent();
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			Uri uri = (Uri) intent
					.getParcelableExtra(Intent.EXTRA_STREAM);
			uriOCR(uri);
		}

		if (!OpenCVLoader.initDebug()) {
			//Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		} else {
			//Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		mTessOCR.onDestroy();
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File

			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	/**
	 * http://developer.android.com/training/camera/photobasics.html
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		String storageDir = Environment.getExternalStorageDirectory()
				+ "/TessOCR";
		File dir = new File(storageDir);
		if (!dir.exists())
			dir.mkdir();

		File image = new File(storageDir + "/" + imageFileName + ".jpg");

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_TAKE_PHOTO
				&& resultCode == Activity.RESULT_OK) {
			setPic();
		}
		else if (requestCode == REQUEST_PICK_PHOTO
				&& resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			if (uri != null) {
				uriOCR(uri);
			}
		}
	}

	private void setPic() {
		// Get the dimensions of the View
		int targetW = mImage.getWidth();
		int targetH = mImage.getHeight();

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor << 1;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		mImage.setImageBitmap(bitmap);
		doOCR(bitmap);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.bt_gallery:
			pickPhoto();
			break;
		case R.id.bt_camera:
			takePhoto();
			break;
		}
	}
	
	private void pickPhoto() {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_PICK_PHOTO);
	}

	private void takePhoto() {
		dispatchTakePictureIntent();
	}

	private void doOCR(final Bitmap bitmap) {
		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialog.show(this, "Processing",
					"Doing OCR...", true);
		}
		else {
			mProgressDialog.show();
		}
		
		new Thread(new Runnable() {
			public void run() {

				final String result = mTessOCR.getOCRResult(bitmap);

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (result != null && !result.equals("")) {
							mResult.setText(result);
						}

						mProgressDialog.dismiss();
					}

				});

			};
		}).start();
	}
}
