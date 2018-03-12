package com.vism.gethlibrary;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Administrator on 2018-3-12.
 */

public class HttpUtils {
    private static final int TIMEOUT_IN_MILLIONS = 5000;

    private HttpUtilsCallBack myCallBack;
    public interface HttpUtilsCallBack {
        void onRequestComplete(String result);
    }
    public void setMyCallBack(HttpUtilsCallBack httpUtilsCallBack){
        myCallBack = httpUtilsCallBack;
    }

    /**
     *   异步的Get请求
     *   @param urlStr
     *   @param callBack
     */
    public  void doGetAsyn(final String urlStr, final HttpUtilsCallBack callBack) {
        new Thread() {
            public void run() {
                try {
                    String result = doGet(urlStr);
                    if (callBack != null) {
                        callBack.onRequestComplete(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 异步的Post请求
     *
     * @param urlStr
     * @param params
     * @param callBack
     * @throws Exception
     */
    public static void doPostAsyn(final String urlStr, final String params,final HttpUtilsCallBack callBack) throws Exception {
        new Thread() {
            public void run() {
                try {
                    String result = doPost(urlStr, params);
                    if (callBack != null) {
                        callBack.onRequestComplete(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     *  Get请求，获取网络数据
     * @param urlStr
     * @return
     */
    public  String doGet(String urlStr) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            url = new URL(urlStr);
            System.out.println("链接地址:"+url);
            conn = (HttpURLConnection) url.openConnection();
            //如果是https
            if (conn instanceof HttpsURLConnection)
            {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(new TrustAnyHostnameVerifier());
            }else {
                conn.setRequestMethod("GET");
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
            }
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);

            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];
                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                String json = baos.toString();
                System.out.println("返回json数据:"+json);
                if (json!=null && json.length()!=0) {
                    //返回的json字符串不为空，执行回调函数
                    myCallBack.onRequestComplete(baos.toString());
                }
                return baos.toString();
            } else {
                throw new RuntimeException(" responseCode is  not 200 ... ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {

            }
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {

            }
            conn.disconnect();
        }
        return "{\"status\":\"0\",\"message\":\"NO\",\"result\":\"Request again!\"}";
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式
     * @return 代表远程资源的响应结果
     * @throws Exception
     */
    public static String doPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url); // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            //如果是https
            if (conn instanceof HttpsURLConnection)
            {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(new TrustAnyHostnameVerifier());
            }else {
                // 设置通用的请求属性
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  // 设置内容类型
                conn.setRequestProperty("charset", "utf-8");   // 设置字符编码
                conn.setUseCaches(false); // 发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);     // 设置是否从httpUrlConnection读入，默认情况下是true;
            }
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);        // 将读超时设置为指定的超时，以毫秒为单位。
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);	// 设置一个指定的超时值（以毫秒为单位）
            if (param != null && !param.trim().equals("")) { // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream()); // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {    //使用finally块来关闭输出流和输入流
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 证书信任管理器（用于https请求） .
     */
    private static class TrustAnyTrustManager implements X509TrustManager
    {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {
        }

        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier
    {
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    }
}
