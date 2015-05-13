package com.dynamsoft.tessocr;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;

import android.widget.ImageView;
import com.googlecode.tesseract.android.TessBaseAPI;
import org.apache.http.client.methods.HttpPost;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class TessOCR {
	private TessBaseAPI mTess;
	private ImageView mIvGray;
	private ImageView mIvBin;
	private Activity mActivity;
	public TessOCR(Activity context, ImageView ivGray, ImageView ivBin) {
		// TODO Auto-generated constructor stub
		mTess = new TessBaseAPI();
		String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
		String language = "eng";
		File dir = new File(datapath + "tessdata/");
		if (!dir.exists()) 
			dir.mkdirs();
		mTess.init(datapath, language);
		mIvGray = ivGray;
		mIvBin = ivBin;
		mActivity = context;
	}

	Bitmap grayBitmap;
	Bitmap binBitmap;
	Bitmap srcBitmap;
	int threshhold = 14388608;
	public String getOCRResult(Bitmap bitmap) {
		srcBitmap = bitmap;
		Mat rgbMat = new Mat();
		Mat grayMat = new Mat();
		Utils.bitmapToMat(bitmap, rgbMat);//convert original bitmap to Mat, R G B.
		Mat filterMat = new Mat();

		Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
		//Imgproc.GaussianBlur(grayMat, filterMat, new Size(9,9), 2);
		//Imgproc.f
		 grayBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
		 binBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
		Mat binMat = new Mat();
		Imgproc.threshold(grayMat, binMat, threshhold, 255, Imgproc.THRESH_BINARY);
		Utils.matToBitmap(binMat, binBitmap);
		mTess.setImage(binBitmap);
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mIvBin.setImageBitmap(binBitmap);
				mIvGray.setImageBitmap(grayBitmap);
			}
		});

		String result = mTess.getUTF8Text();

		return result;


    }
	
	public void onDestroy() {
		if (mTess != null)
			mTess.end();
	}

	public void changeParam(int threshhold){
		this.threshhold = threshhold;
		getOCRResult(srcBitmap);
	}
	
}
