package io.arbitrix.core.integration.okx.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiDetail;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiInfo;
import io.arbitrix.core.common.util.JacksonUtil;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author jonathan.ji
 */
@Log4j2
@Component
public class UriAndHeaderEncoder {
    private static final String POST = "post";
    private static final String GET = "get";
    @Value("${okx.mbx.restBaseUrl:}")
    private String urlPath;
    @Value("${okx.testTrading:false}")
    private Boolean testTrading;
    @Value("${okx.api.detail:}")
    private String okxApiDetailStr;
    private final OkxProperties okxProperties;
    protected final HttpClient httpClient;
    private final Timer connectionManagerTimer = new Timer(
            "UriAndHeaderEncoder.connectionManagerTimer", true);
    public UriAndHeaderEncoder(OkxProperties okxProperties) {
        this.okxProperties = okxProperties;
        this.httpClient = this.initHttpClient(okxProperties);
    }
    private HttpClient initHttpClient(OkxProperties okxProperties) {
        ApacheHttpClientConnectionManagerFactory connectionManagerFactory = new DefaultApacheHttpClientConnectionManagerFactory();
        HttpClientConnectionManager connectionManager = connectionManagerFactory
                .newConnectionManager(okxProperties.isDisableSslValidation(),
                        okxProperties.getMaxConnections(),
                        okxProperties.getMaxConnectionsPerRoute(),
                        okxProperties.getTimeToLive(),
                        okxProperties.getTimeToLiveUnit(),
                        null);
        this.connectionManagerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                connectionManager.closeExpiredConnections();
            }
        }, 30000, okxProperties.getConnectionTimerRepeat());
        ApacheHttpClientFactory httpClientFactory = new DefaultApacheHttpClientFactory(HttpClientBuilder.create());
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(okxProperties.getConnectionTimeout())
                .setRedirectsEnabled(okxProperties.isFollowRedirects())
                .build();
        return httpClientFactory.createBuilder()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(defaultRequestConfig).build();
    }


    public Map<String, String> getHeader(String requestType, String uri, Map<String, ?> params, String orderJson, String clientOrderId) {
        Map<String, String> header = new HashMap<>(8);
        String timestamp = String.valueOf(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        String hmacUri = timestamp + requestType.toUpperCase() + uri + orderJson;
        String okAccessSign = encodeAccessSign(hmacUri, clientOrderId);

        OkxApiInfo okxApiInfo = randomOkxApiInfo(clientOrderId);
        header.put("accept", "application/json");
        header.put("Content-type", "application/json");
        header.put("OK-ACCESS-KEY", okxApiInfo.getApiKey());
        header.put("OK-ACCESS-SIGN", okAccessSign);
        header.put("OK-ACCESS-PASSPHRASE", okxApiInfo.getPassprhase());
        header.put("OK-ACCESS-TIMESTAMP", timestamp);
        if (testTrading) {
            header.put("x-simulated-trading", "1");
        }
        return header;
    }

    public String buildUrl(String uri, Map<String, ?> params) {
        String aUri = uri;
        List<NameValuePair> paramPairList = this.buildParams(params);
        if (null != paramPairList) {
            aUri = uri + "?" + URLEncodedUtils.format(paramPairList, "utf-8");
        }
        return aUri;
    }

    private List<NameValuePair> buildParams(Map<String, ?> params) {
        if (params == null) {
            return null;
        } else {
            List<NameValuePair> resultList = new ArrayList();
            Iterator var3 = params.entrySet().iterator();
            while (var3.hasNext()) {
                Map.Entry<String, ?> param = (Map.Entry) var3.next();
                resultList.add(new BasicNameValuePair((String) param.getKey(), String.valueOf(param.getValue())));
            }
            return resultList;
        }
    }

    public HttpRequestBase encode(String requestType, String path, String orderJson, String clientOrderId) throws UnsupportedEncodingException {
        String timestamp = String.valueOf(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        String hmacUri = timestamp + requestType.toUpperCase() + path + orderJson;
        String okAccessSign = encodeAccessSign(hmacUri, clientOrderId);
        StringEntity stringEntity = new StringEntity(orderJson);

        if (POST.equalsIgnoreCase(requestType)) {
            HttpPost request = new HttpPost(urlPath + path);
            request.setEntity(stringEntity);
            addHeaders(request, okAccessSign, timestamp, clientOrderId);
            return request;
        } else if (GET.equalsIgnoreCase(requestType)) {
            HttpGet request = new HttpGet(urlPath + path);
            addHeaders(request, okAccessSign, timestamp, clientOrderId);
            return request;
        } else {
            throw new RuntimeException("Incorrect HTTP request type: " + requestType);
        }
    }

    private void addHeaders(HttpRequestBase request, String okAccessSign, String timestamp, String clientOrderId) {
        OkxApiInfo okxApiInfo = randomOkxApiInfo(clientOrderId);
        request.addHeader("accept", "application/json");
        request.addHeader("Content-type", "application/json");
        request.addHeader("OK-ACCESS-KEY", okxApiInfo.getApiKey());
        request.addHeader("OK-ACCESS-SIGN", okAccessSign);
        request.addHeader("OK-ACCESS-PASSPHRASE", okxApiInfo.getPassprhase());
        request.addHeader("OK-ACCESS-TIMESTAMP", timestamp);
        if (testTrading) {
            request.addHeader("x-simulated-trading", "1");
        }
    }

    private String encodeAccessSign(String hmacUri, String clientOrderId) {
        OkxApiInfo okxApiInfo = randomOkxApiInfo(clientOrderId);
        byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, okxApiInfo.getSecretKey()).hmac(hmacUri);
        return Base64.getEncoder().encodeToString(hmac);
    }

    public OkxApiInfo randomOkxApiInfo(String clientOrderId) {
        OkxApiDetail okxApiDetail = JacksonUtil.from(okxApiDetailStr, OkxApiDetail.class);
        List<OkxApiInfo> okxApiInfoList = okxApiDetail.getOkxApiInfoList();
        return this.randomOkxApiInfo(clientOrderId, okxApiInfoList);
    }

    /**
     * okx官网：
     * 如果请求速率高于我们的限速，可以设置不同的子账户来批量请求限速。我们建议使用此方法来限制或间隔请求，以最大化每个帐户的限速并避免断开连接或拒绝请求
     * <p>
     * 选用clientOrderId的hash值对OKX的配置信息 是为了保证同一个订单的请求得是同一个帐号
     *
     * @param clientOrderId 自定义的用户ID
     * @return OKX的配置信息
     */
    public OkxApiInfo randomOkxApiInfo(String clientOrderId, List<OkxApiInfo> okxApiInfoList) {
        Comparator<OkxApiInfo> comparator = Comparator.comparingInt(OkxApiInfo::getPriority);
        List<OkxApiInfo> sortedList = new ArrayList<>(okxApiInfoList);
        sortedList.sort(comparator);

        int index = Math.floorMod(clientOrderId.hashCode(), sortedList.size());
        log.debug("OkxApiInfo.randomOkxApiInfo clientOrderId is {} api index is {}", clientOrderId, index);
        return sortedList.get(index);
    }
}
