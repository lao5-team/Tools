package com.dynamsoft.tessocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BR on 2015/5/14.
 */
public final class Utils {
    public static boolean checkReceipt(String code, String number, String password, TessOCR ocr) throws IOException {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://www.bjtax.gov.cn/ptfp/turnyzm.jsp");
        HttpResponse response = httpclient.execute(httpGet);
        Log.d("Utils", "Response Status Code: " + response.getStatusLine().getStatusCode());

        Cookie sessionCookie = httpclient.getCookieStore().getCookies().get(0);

        String validationCode = parseValidationCode(response.getEntity().getContent(), ocr);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("valiNum",validationCode));
        params.add(new BasicNameValuePair("fpdm",code));
        params.add(new BasicNameValuePair("fphm",number));
        params.add(new BasicNameValuePair("sfzh",""));
        params.add(new BasicNameValuePair("fpmm",password));
        params.add(new BasicNameValuePair("ip","127.0.0.1"));
        params.add(new BasicNameValuePair("nsr",""));
        params.add(new BasicNameValuePair("isFrist","1"));
        String query = URLEncodedUtils.format(params, "utf-8");

        try {
            //        // make sure this cookie is added
             new BasicCookieStore().addCookie(sessionCookie);
            URI uri = new URI("http", null, "www.bjtax.gov.cn", -1, "/ptfp/turna.jsp", query, "");
            httpGet = new HttpGet(uri);
            response = httpclient.execute(httpGet);
            //Log.d("Utils", "Response Status Code: " + response.getStatusLine().getStatusCode());
            String content = EntityUtils.toString(response.getEntity(), "utf-8");
            if (content.contains("成功")) {
                return true;
            }else {
                return false;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return false;

    }

    public static String parseValidationCode(InputStream img,TessOCR ocr) {
        Bitmap bmp = BitmapFactory.decodeStream(img);
        String vCode = ocr.getOCRResult(OCRActivity.processCheckcodeBmp(bmp, 2));
        Log.v(OCRActivity.tag, "ValidationCode " + vCode);
        return vCode;
    }
}
