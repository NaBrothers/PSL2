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
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Log4j2
public class HttpUtils {

    private static final String url = "http://127.0.0.1:5700/";

    private static final String proxy_url = "http://127.0.0.1:5010/";

    private static final List<String> UA_LIST = Arrays.asList(
            "Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_2 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5",
            "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5",
            "MQQBrowser/25 (Linux; U; 2.3.3; zh-cn; HTC Desire S Build/GRI40;480*800)",
            "Mozilla/5.0 (Linux; U; Android 2.3.3; zh-cn; HTC_DesireS_S510e Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
            "Mozilla/5.0 (SymbianOS/9.3; U; Series60/3.2 NokiaE75-1 /110.48.125 Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413",
            "Mozilla/5.0 (iPad; U; CPU OS 4_3_3 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like Gecko) Mobile/8J2",
            "Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/534.51.22 (KHTML, like Gecko) Version/5.1.1 Safari/534.51.22",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A5313e Safari/7534.48.3",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A5313e Safari/7534.48.3",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A5313e Safari/7534.48.3",
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; SAMSUNG; OMNIA7)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; XBLWP7; ZuneWP7)",
            "Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30",
            "Mozilla/5.0 (Windows NT 5.1; rv:5.0) Gecko/20100101 Firefox/5.0",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET4.0E; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET4.0E; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C)",
            "Mozilla/4.0 (compatible; MSIE 60; Windows NT 5.1; SV1; .NET CLR 2.0.50727)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)",
            "Opera/9.80 (Windows NT 5.1; U; zh-cn) Presto/2.9.168 Version/11.50",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET4.0E; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C)",
            "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1",
            "Mozilla/5.0 (Windows; U; Windows NT 5.1; ) AppleWebKit/534.12 (KHTML, like Gecko) Maxthon/3.0 Safari/534.12",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; TheWorld)"
    );

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
        String res = doPost(sb.toString(), param, null);
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
            httpGet.setHeader("User-Agent", UA_LIST.get(new Random().nextInt(UA_LIST.size())));
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
    public static String doPost(String url, JSONObject body, JSONObject header) {
        String jsonStr = body.toJSONString();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3000)
                .setConnectionRequestTimeout(35000)
                .setSocketTimeout(120000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("DataEncoding", "UTF-8");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");
        if (header != null) {
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        CloseableHttpResponse httpResponse = null;
        try {
            httpPost.setEntity(new StringEntity(jsonStr, ContentType.APPLICATION_JSON));
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

    @Nullable
    public static String doPostWithProxy(String url, JSONObject body, JSONObject header) throws IOException {
        String jsonStr = body.toJSONString();

        CloseableHttpClient httpClient = createSSLClientDefault();
        HttpPost httpPost = new HttpPost(url);
        HttpHost proxy = new HttpHost("172.245.226.43", 8899);

        RequestConfig requestConfig = RequestConfig.custom()
                .setProxy(proxy)
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(35000)
                .setSocketTimeout(120000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("DataEncoding", "UTF-8");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");
        if (header != null) {
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        CloseableHttpResponse httpResponse = null;
        try {
            httpPost.setEntity(new StringEntity(jsonStr, ContentType.APPLICATION_JSON));
            httpResponse = httpClient.execute(httpPost);
            if(httpResponse.getStatusLine().getStatusCode() != 200){
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        } catch (ClientProtocolException e) {
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

    public static CloseableHttpClient createSSLClientDefault() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext ctx = SSLContext.getInstance("TLSv1.2");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            sslsf = new SSLConnectionSocketFactory(ctx, new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }
}
