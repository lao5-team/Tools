package com.dynamsoft.tessocr;

import android.graphics.Bitmap;
import android.graphics.Rect;
import org.opencv.core.Mat;

/**
 * Created by yihao on 15/5/13.
 */
public class ReceiptProcess {

    /**
     * 设置取景框矩形
     * @param rect
     */
    public void setCameraCapture(Rect rect){

    }

    /**
     * 扫描图片
     * @param type 0 发票代码和发票号码,1 发票密码, 2 网站验证码
     */
    public void scan(int type){

    }

    /**
     * 获取到图片
     * @param bmp
     */
    public void onImageReceived(Bitmap bmp){

    }

    public void filterImage(Bitmap bmp, Mat mat){

    }

    public void grayImage(Bitmap bmp, Mat mat){

    }

    public void binaryImage(Bitmap bmp, Mat mat){

    }

    public void onGetReceiptString(String receiptCode, String receiptNumber){

    }

    public void onGetReceiptPassword(String receiptPassword){

    }

    public void onGetCheckCode(String checkCode){

    }


}
