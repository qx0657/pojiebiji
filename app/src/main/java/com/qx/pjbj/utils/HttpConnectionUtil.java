package com.qx.pjbj.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Create by QianXiao
 * On 2020/7/24
 * https://blog.csdn.net/DeMonliuhui/article/details/71453656?locationNum=9&fps=1
 */
public class HttpConnectionUtil {
    public static HttpConnectionUtil http = new HttpConnectionUtil();

    public static HttpConnectionUtil getHttp() {
        return http;
    }

    //检测是否代理
    private static boolean a() {
        String proxyAddress = "";
        int proxyPort = 0;
        proxyAddress = System.getProperty("http.proxyHost");
        String proxyPortString = System.getProperty("http.proxyPort");
        proxyPort = Integer.parseInt((proxyPortString != null ? proxyPortString : "-1"));
        if (!TextUtils.isEmpty(proxyAddress) && proxyPort != -1) {
            return true;
        }
        return false;
    }

    public String getRequset(final String url) {
        if(a()){
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL requestUrl = new URL(url);
                    connection = (HttpURLConnection) requestUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    if (connection.getResponseCode() == 200) {
                        InputStream in = connection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        System.out.println(sb);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                    if (connection != null) {
                        connection.disconnect();//断开连接，释放资源
                    }
                }
                return sb.toString();
            }
        });
        new Thread(task).start();
        String s = null;
        try {
            s = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public String postRequset(final String url, final Map<String, String> map) {
        if(a()){
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL requestUrl = new URL(url);
                    connection = (HttpURLConnection) requestUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);//链接超时
                    connection.setReadTimeout(8000);//读取超时
                    //发送post请求必须设置
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    DataOutputStream out = new DataOutputStream(connection
                            .getOutputStream());
                    StringBuilder request = new StringBuilder();
                    for (String key : map.keySet()) {
                        request.append(key + "=" + URLEncoder.encode(map.get(key), "UTF-8") + "&");
                    }
                    out.writeBytes(request.toString());//写入请求参数
                    out.flush();
                    out.close();
                    if (connection.getResponseCode() == 200) {
                        InputStream in = connection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        System.out.println(sb);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        reader.close();//关闭流
                    }
                    if (connection != null) {
                        connection.disconnect();//断开连接，释放资源
                    }
                }
                return sb.toString();
            }
        });
        new Thread(task).start();
        String s = null;
        try {
            s = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

}
