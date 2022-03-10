package com.nabrothers.psl.server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;

@Log4j2
public class HttpUtils {

    private static final String url = "http://127.0.0.1:5700/";

    public static JSONObject doGet(String cmd, JSONObject param) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(cmd + "?");
        for (String key : param.keySet()) {
            try {
                sb.append(key + "=" + URLEncoder.encode(param.get(key).toString(), "UTF-8") + "&");
            } catch (Exception ignore) {}
        }
        String res = doGet(sb.toString());
        if (res == null) {
            log.error("HTTP_GET_ERROR: " + sb.toString());
            return null;
        }
        JSONObject resObj = JSON.parseObject(res);
        if (resObj.getIntValue("retcode") != 0) {
            log.error("HTTP_GET_API_ERROR: " + sb.toString() + ", " + resObj.getString("wording"));
            return null;
        }
        return resObj.getJSONObject("data");
    }

    public static JSONObject doPost(String cmd, JSONObject param) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(cmd);
        String res = doPost(sb.toString(), param.toJSONString());
        if (res == null) {
            log.error("HTTP_POST_ERROR: " + sb.toString());
            return null;
        }
        JSONObject resObj = JSON.parseObject(res);
        if (resObj.getIntValue("retcode") != 0) {
            log.error("HTTP_POST_API_ERROR: " + sb.toString() + ", " + resObj.getString("wording"));
            return null;
        }
        return resObj.getJSONObject("data");
    }


    /**
     * @Description: 发送get请求
     */
    public static String doGet(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("DataEncoding", "UTF-8");
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if(httpResponse.getStatusLine().getStatusCode() != 200){
                return null;
            }
            return EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @Description: 发送http post请求
     */
    public static String doPost(String url, String jsonStr) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("DataEncoding", "UTF-8");
        CloseableHttpResponse httpResponse = null;
        try {
            httpPost.setEntity(new StringEntity(jsonStr));
            httpResponse = httpClient.execute(httpPost);
            if(httpResponse.getStatusLine().getStatusCode() != 200){
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}