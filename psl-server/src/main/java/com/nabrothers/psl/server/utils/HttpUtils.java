package com.nabrothers.psl.server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.server.config.GlobalConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

@Log4j2
public class HttpUtils {

    private static final String url = "http://127.0.0.1:5700/";

    private static final String proxy_url = "http://127.0.0.1:5010/";

    @Nullable
    public static JSONObject doGet(String cmd, JSONObject param) {
        JSONObject resObj = getResp(cmd, param);
        return resObj == null ? null : resObj.getJSONObject("data");
    }

    @Nullable
    public static JSONArray doGetArray(String cmd, JSONObject param) {
        JSONObject resObj = getResp(cmd, param);
        return resObj == null ? null : resObj.getJSONArray("data");
    }

    private static JSONObject getResp(String cmd, JSONObject param) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(cmd + "?");
        if (param != null) {
            for (String key : param.keySet()) {
                try {
                    sb.append(key + "=" + URLEncoder.encode(param.get(key).toString(), "UTF-8") + "&");
                } catch (Exception ignore) {
                }
            }
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
        return resObj;
    }

    @Nullable
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

    @Nullable
    public static String doGet(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("DataEncoding", "UTF-8");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
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
            log.error(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.error(e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
        return null;
    }

    @Nullable
    public static String doGetWithProxy(String url) {
        if (!GlobalConfig.ENABLE_PROXY) {
            return doGet(url);
        }
        int tryCount = 0;
        tryLoop:
        while (tryCount < 3) {
            tryCount++;
            Pair<String, Integer> ipPort = getProxy();
            if (ipPort == null) {
                return doGet(url);
            }
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("DataEncoding", "UTF-8");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");
            HttpHost proxy = new HttpHost(ipPort.getKey(), ipPort.getValue());
            RequestConfig requestConfig = RequestConfig.custom()
                    .setProxy(proxy)
                    .setConnectTimeout(3000)
                    .setConnectionRequestTimeout(35000)
                    .setSocketTimeout(60000)
                    .build();
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpGet);
                HttpEntity entity = httpResponse.getEntity();
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    if (httpResponse.getStatusLine().getStatusCode() == 404) {
                        removeProxy(ipPort);
                        continue tryLoop;
                    }
                    return null;
                }
                return EntityUtils.toString(entity);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                log.error(e);
            } catch (ConnectTimeoutException e) {
                log.error(e);
                removeProxy(ipPort);
                continue tryLoop;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error(e);
            } finally {
                if (httpResponse != null) {
                    try {
                        httpResponse.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        log.error(e);
                    }
                }
                if (null != httpClient) {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static String doPost(String url, String jsonStr) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(35000).setSocketTimeout(120000).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("DataEncoding", "UTF-8");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");
        String API_TOKEN = "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"; //temp
        httpPost.setHeader("Authorization", "Bearer "+API_TOKEN); //temp
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
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
        return null;
    }

    private static Pair<String, Integer> getProxy() {
        Pair<String, Integer> proxy = null;
        try {
            String retStr = doGet(proxy_url + "get");
            if (retStr == null) {
                return null;
            }
            String proxyStr = JSONObject.parseObject(retStr).getString("proxy");
            String[] ipPort = proxyStr.split(":");
            proxy = new ImmutablePair<>(ipPort[0], Integer.valueOf(ipPort[1]));
        } catch (Exception e) {
            log.warn("Get proxy error", e);
        }
        return proxy;
    }

    private static boolean removeProxy(Pair<String, Integer> proxy) {
        try {
            String retStr = doGet(proxy_url + "delete?proxy=" + proxy.getKey() + ":" + proxy.getValue());
            if (retStr == null) {
                return false;
            }
            int src = JSONObject.parseObject(retStr).getIntValue("src");
            if (src == 1) {
                log.info("代理 {}:{} 访问失败，已被删除", proxy.getKey(), proxy.getValue());
            }
            return src == 1;
        } catch (Exception e) {
            log.warn("Remove proxy error", e);
        }
        return false;
    }
}