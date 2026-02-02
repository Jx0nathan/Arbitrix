package io.arbitrix.core.integration.okx.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrdersHistory;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrdersHistoryResponse;
import io.arbitrix.core.common.util.JacksonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class OkxTradeClient extends UriAndHeaderEncoder {
    private static final String ORDER_HISTORY_PATH = "/api/v5/trade/orders-history";

    public OkxTradeClient(OkxProperties okxProperties) {
        super(okxProperties);
    }

    /**
     * 获取历史订单记录（近七天）
     * https://www.okx.com/docs-v5/zh/#order-book-trading-trade-get-order-list
     *
     * @param instType  产品类型，SPOT：币币
     * @param instId    产品ID，如BTC-USD-190927
     * @param orderType post_only：只做maker单
     * @param before    请求此ID之后（更新的数据）的分页内容，传的值为对应接口的ordId
     * @param begin     筛选的开始时间戳，Unix 时间戳为毫秒数格式，如 1597026383085
     * @param end       筛选的结束时间戳，Unix 时间戳为毫秒数格式，如 1597027383085
     */
    public List<OkxOrdersHistory> getOrderHistory(String instType, String instId, String state, String orderType, String before, String begin, String end) throws IOException {
        Map<String, String> params = new HashMap<>(2);
        params.put("instType", instType);
        params.put("instId", instId);
        params.put("orderType", orderType);
        params.put("before", before);
        params.put("begin", begin);
        params.put("end", end);
        params.put("state", state);
        String uri = super.buildUrl(ORDER_HISTORY_PATH, params);

        HttpRequestBase httpRequest = super.encode("get", uri, "", "0");

        OkxOrdersHistoryResponse historyResponse = httpClient.execute(httpRequest, this::decode);
        if (historyResponse != null && historyResponse.getCode() == 0) {
            return historyResponse.getData();
        }
        return null;
    }

    private OkxOrdersHistoryResponse decode(HttpResponse httpResponse) throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        String entityString = EntityUtils.toString(entity);
        return JacksonUtil.from(entityString, OkxOrdersHistoryResponse.class);
    }


}
