package com.dynamsoft.tessocr;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.*;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import org.opencv.android.*;

public class OCRActivity extends Activity implements OnClickListener, SurfaceHolder.Callback, Camera.PreviewCallback {
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

	public static final String tag="tessocr";
	private boolean isPreview = false;
	private SurfaceView mPreviewSV = null; //预览SurfaceView
	private DrawImageView mDrawIV = null;
	private SurfaceHolder mySurfaceHolder = null;
	private ImageButton mPhotoImgBtn = null;
	private Camera myCamera = null;
	private Bitmap mBitmap = null;
	private Camera.AutoFocusCallback myAutoFocusCallback = null;
	private EditText mEtxCode = null;
	private EditText mEtxNumber = null;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//Bitmap checkcodeBmp = BitmapFactory.decodeStream(getResources().getAssets().open("2.jpeg"));
						//doOCR(processCheckcodeBmp(checkcodeBmp, 2));
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Utils.checkReceipt("111001481005", "64274411", "asdf", mTessOCR);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
						t.start();
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
		mEtxCode = (EditText)findViewById(R.id.editText_code);
		mEtxNumber = (EditText)findViewById(R.id.editText_number);
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


		mPreviewSV = (SurfaceView)findViewById(R.id.surfaceView_capture);
		mPreviewSV.setZOrderOnTop(false);
		mySurfaceHolder = mPreviewSV.getHolder();
		mySurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明

		mySurfaceHolder.addCallback(this);
		//为了实现照片预览功能，需要将SurfaceHolder的类型设置为PUSH
		//这样，画图缓存就由Camera类来管理，画图缓存是独立于Surface的
		mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		//自动聚焦变量回调
		myAutoFocusCallback = new Camera.AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {
				// TODO Auto-generated method stub
				if(success)//success表示对焦成功
				{
					Log.i(tag, "myAutoFocusCallback: success...");
					//myCamera.setOneShotPreviewCallback(null);
					myCamera.cancelAutoFocus();
				}
				else
				{
					//未对焦成功
					Log.i(tag, "myAutoFocusCallback: failed...");

				}


			}
		};

		//绘制矩形的ImageView
		mDrawIV = (DrawImageView)findViewById(R.id.drawIV);
		mDrawIV.draw(new Canvas());



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

	boolean mIsCapture = false;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.bt_gallery:
			pickPhoto();
			break;
		case R.id.bt_camera:
			int value = Integer.valueOf(mEtxThreshold.getEditableText().toString());
			mTessOCR.changeParam(value);
			mIsCapture = true;
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
							mEtxCode.setText(result);
//							String [] temps = result.split("\n");
//							mEtxCode.setText(temps[0]);
//							mEtxNumber.setText(temps[1]);
						}

						mProgressDialog.dismiss();
					}

				});

			};
		}).start();
	}

	/**
	 * This is called immediately after the surface is first created.
	 * Implementations of this should start up whatever rendering code
	 * they desire.  Note that only one thread can ever draw into
	 * a {@link Surface}, so you should not draw into the Surface here
	 * if your normal rendering will be in another thread.
	 *
	 * @param holder The SurfaceHolder whose surface is being created.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		myCamera = Camera.open();
		try {
			myCamera.setPreviewDisplay(mySurfaceHolder);
			Log.i(tag, "SurfaceHolder.Callback: surfaceCreated!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(null != myCamera){
				myCamera.release();
				myCamera = null;
			}
			e.printStackTrace();
		}
	}

	/**
	 * This is called immediately after any structural changes (format or
	 * size) have been made to the surface.  You should at this point update
	 * the imagery in the surface.  This method is always called at least
	 * once, after {@link #surfaceCreated}.
	 *
	 * @param holder The SurfaceHolder whose surface has changed.
	 * @param format The new PixelFormat of the surface.
	 * @param width  The new width of the surface.
	 * @param height The new height of the surface.
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		initCamera();
	}

	/**
	 * This is called immediately before a surface is being destroyed. After
	 * returning from this call, you should no longer try to access this
	 * surface.  If you have a rendering thread that directly accesses
	 * the surface, you must ensure that thread is no longer touching the
	 * Surface before returning from this function.
	 *
	 * @param holder The SurfaceHolder whose surface is being destroyed.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(tag, "SurfaceHolder.Callback：Surface Destroyed");
		if(null != myCamera)
		{
			myCamera.setPreviewCallback(null); /*在启动PreviewCallback时这个必须在前不然退出出错。
			这里实际上注释掉也没关系*/

			myCamera.stopPreview();
			isPreview = false;
			myCamera.release();
			myCamera = null;
		}
	}

	public void initCamera(){
		if(isPreview){
			myCamera.stopPreview();
		}
		if(null != myCamera){
			Camera.Parameters myParam = myCamera.getParameters();
			//			//查询屏幕的宽和高
			//			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			//			Display display = wm.getDefaultDisplay();
			//			Log.i(tag, "屏幕宽度："+display.getWidth()+" 屏幕高度:"+display.getHeight());

			myParam.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式

			//查询camera支持的picturesize和previewsize
			List<Camera.Size> pictureSizes = myParam.getSupportedPictureSizes();
			List<Camera.Size> previewSizes = myParam.getSupportedPreviewSizes();
			for (int i = 0; i < pictureSizes.size(); i++) {
				Camera.Size size = pictureSizes.get(i);
				Log.i(tag, "initCamera:摄像头支持的pictureSizes: width = " + size.width + "height = " + size.height);
			}
			for (int i = 0; i < previewSizes.size(); i++) {
				Camera.Size size = previewSizes.get(i);
				Log.i(tag, "initCamera:摄像头支持的previewSizes: width = " + size.width + "height = " + size.height);

			}


			//设置大小和方向等参数
			int width = mPreviewSV.getWidth();
			int height = mPreviewSV.getHeight();
			Log.v("tessocr", "width " + width + " height " + height);
			myParam.setPictureSize(640, 480);
			myParam.setPreviewSize(640, 480);
			myParam.set("rotation", 90);
			myCamera.setDisplayOrientation(90);
			myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			myCamera.setParameters(myParam);
			myCamera.setPreviewCallback(this);
			myCamera.startPreview();
			myCamera.autoFocus(myAutoFocusCallback);
			myCamera.cancelAutoFocus();
			isPreview = true;
		}
	}

	/*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
	Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback()
			//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
	{

		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(tag, "myShutterCallback:onShutter...");

		}
	};
	Camera.PictureCallback myRawCallback = new Camera.PictureCallback()
			// 拍摄的未压缩原数据的回调,可以为null
	{

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(tag, "myRawCallback:onPictureTaken...");

		}
	};
	Camera.PictureCallback myJpegCallback = new Camera.PictureCallback()
			//对jpeg图像数据的回调,最重要的一个回调
	{

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(tag, "myJpegCallback:onPictureTaken...");
			if(null != data){
				mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
				myCamera.stopPreview();
				isPreview = false;
			}
			//设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。图片竟然不能旋转了，故这里要旋转下
			Matrix matrix = new Matrix();
			matrix.postRotate((float)90.0);
			Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);

			//旋转后rotaBitmap是960×1280.预览surfaview的大小是540×800
			//将960×1280缩放到540×800
			Bitmap sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, 540, 800, true);
			Bitmap rectBitmap = Bitmap.createBitmap(sizeBitmap, 100, 200, 300, 300);//截取


			//保存图片到sdcard
			if(null != rectBitmap)
			{
				saveJpeg(rectBitmap);
			}

			//再次进入预览
			myCamera.startPreview();
			isPreview = true;
		}
	};

	/**
	 * Called as preview frames are displayed.  This callback is invoked
	 * on the event thread {@link #open(int)} was called from.
	 * <p/>
	 * <p>If using the {@link android.graphics.ImageFormat#YV12} format,
	 * refer to the equations in {@link android.hardware.Camera.Parameters#setPreviewFormat}
	 * for the arrangement of the pixel data in the preview callback
	 * buffers.
	 *
	 * @param data   the contents of the preview frame in the format defined
	 *               by {@link android.graphics.ImageFormat}, which can be queried
	 *               with {@link android.hardware.Camera.Parameters#getPreviewFormat()}.
	 *               If {@link android.hardware.Camera.Parameters#setPreviewFormat(int)}
	 *               is never called, the default will be the YCbCr_420_SP
	 *               (NV21) format.
	 * @param camera the Camera service object.
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.v(tag, "onPreviewFrame");
		if(mIsCapture == true){
		Camera.Size size = camera.getParameters().getPreviewSize();
		try{
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
			if(image!=null){
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);
				Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

				Matrix m = new Matrix();
				m.setRotate(90, (float) bmp.getWidth() / 2, (float) bmp.getHeight() / 2);

				try {
					Bitmap bmp1 = Bitmap.createBitmap(bmp, 300, 170, 40, 140, m, true);
					mImage.setImageBitmap(bmp1);
					doOCR(bmp1);
				} catch (OutOfMemoryError ex) {
				}

				stream.close();
			}
		}catch(Exception ex){
			Log.e("Sys","Error:"+ex.getMessage());
		}
			mIsCapture = false;
		}

	}

	//拍照按键的监听
	public class PhotoOnClickListener implements OnClickListener{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isPreview && myCamera!=null){
				myCamera.takePicture(myShutterCallback, null, myJpegCallback);
			}

		}

	}
	/*给定一个Bitmap，进行保存*/
	public void saveJpeg(Bitmap bm){
		String savePath = "/mnt/sdcard/rectPhoto/";
		File folder = new File(savePath);
		if(!folder.exists()) //如果文件夹不存在则创建
		{
			folder.mkdir();
		}
		long dataTake = System.currentTimeMillis();
		String jpegName = savePath + dataTake +".jpg";
		Log.i(tag, "saveJpeg:jpegName--" + jpegName);
		//File jpegFile = new File(jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);

			//			//如果需要改变大小(默认的是宽960×高1280),如改成宽600×高800
			//			Bitmap newBM = bm.createScaledBitmap(bm, 600, 800, false);

			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			Log.i(tag, "saveJpeg：存储完毕！");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i(tag, "saveJpeg:存储失败！");
			e.printStackTrace();
		}
	}

	/*为了使图片按钮按下和弹起状态不同，采用过滤颜色的方法.按下的时候让图片颜色变淡*/
	public class MyOnTouchListener implements View.OnTouchListener {

		public final  float[] BT_SELECTED=new float[]
				{ 2, 0, 0, 0, 2,
						0, 2, 0, 0, 2,
						0, 0, 2, 0, 2,
						0, 0, 0, 1, 0 };

		public final float[] BT_NOT_SELECTED=new float[]
				{ 1, 0, 0, 0, 0,
						0, 1, 0, 0, 0,
						0, 0, 1, 0, 0,
						0, 0, 0, 1, 0 };
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());

			}
			return false;
		}

	}

	@Override
	public void onBackPressed()
	//无意中按返回键时要释放内存
	{
		// TODO Auto-generated method stub
		super.onBackPressed();
		this.finish();
	}

	private Bitmap getCaptureBitmap()
	{
		myCamera.stopPreview();
		myCamera.release();
		Canvas canvas = mySurfaceHolder.lockCanvas(null);
		Bitmap bmpResult = Bitmap.createBitmap(240, 80, Bitmap.Config.ARGB_8888);
		canvas.drawBitmap(bmpResult, new Rect(120, 280, 360, 360), new Rect(0, 0, 240, 80), null);
		mySurfaceHolder.unlockCanvasAndPost(canvas);
		return bmpResult;
	}

	public static Bitmap processCheckcodeBmp(Bitmap bmp, int width){
		return Bitmap.createBitmap(bmp, width, width, bmp.getWidth()-width*2, bmp.getHeight() - width*2);
	}
}
