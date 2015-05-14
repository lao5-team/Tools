package com.dynamsoft.tessocr;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookiePolicy;
import java.net.URI;

/**
 * Created by BR on 2015/5/14.
 */
public final class Utils {
    public static boolean checkReceipt(String code, String number, String password) throws IOException {

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://www.bjtax.gov.cn/ptfp/turnyzm.jsp");
        HttpResponse response = httpclient.execute(httpGet);
        Log.d("Utils", "Response Status Code: " + response.getStatusLine().getStatusCode());

        Cookie sessionCookie = httpclient.getCookieStore().getCookies().get(0);

        String validationCode = parseValidationCode(response.getEntity().getContent());

        URI uri = new URIBuilder().setScheme("http")
                .setHost("www.bjtax.gov.cn").setPath("/ptfp/turna.jsp")
                .setParameter("valiNum", validationCode)
                .setParameter("fpdm", code)
                .setParameter("fphm", number).setParameter("sfzh", "")
                .setParameter("fpmm", password)
                .setParameter("ip", "127.0.0.1").setParameter("nsr", "")
                .setParameter("isFrist", "1").build();
        System.out.println("Req URI: " + uri.toString());

        // make sure this cookie is added
        httpCookieStore.addCookie(sessionCookie);

        httpGet = new HttpGet(uri);
        response = httpclient.execute(httpGet);
        Log.d("Utils", "Response Status Code: " + response.getStatusLine().getStatusCode());
        if (IOUtils.toString(response.getEntity().getContent()).contains("ец")) {
            return true;
        }else {
            return false;
        }
    }

    public static String parseValidationCode(InputStream img) {
        return "";
    }
}
