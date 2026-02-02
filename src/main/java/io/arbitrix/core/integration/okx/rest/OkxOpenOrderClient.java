package io.arbitrix.core.integration.okx.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.monitor.annotation.PercentilesMetrics;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiDetail;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiInfo;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrderListResponse;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrderListResponseData;
import io.arbitrix.core.integration.okx.utils.OkxBridgeSystemUtil;
import io.arbitrix.core.common.util.JacksonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author user
 */
@Component
@Log4j2
public class OkxOpenOrderClient extends UriAndHeaderEncoder {
    private static final String PATH = "/api/v5/trade/orders-pending";

    @Value("${okx.api.detail:}")
    private String okxApiDetailStr;

    private final OkxBridgeSystemUtil okxBridgeSystemUtil;

    public OkxOpenOrderClient(OkxProperties okxProperties, OkxBridgeSystemUtil okxBridgeSystemUtil) {
        super(okxProperties);
        this.okxBridgeSystemUtil = okxBridgeSystemUtil;
    }

    /**
     * 限速：60次/2s
     * See Also: https://www.okx.com/docs-v5/zh/#order-book-trading-trade-get-order-details
     */
    @PercentilesMetrics
    public List<Order> getOpenOrders(List<String> symbolPairs) {
        List<Order> allOrderList = new ArrayList<>();
        OkxApiDetail okxApiDetail = JacksonUtil.from(okxApiDetailStr, OkxApiDetail.class);
        List<OkxApiInfo> okxApiInfoList = okxApiDetail.getOkxApiInfoList();
        for (int index = 0; index < okxApiInfoList.size(); index++) {
            try {
                for (String symbolPair : symbolPairs) {
                    Map<String, String> params = new HashMap<>(2);
                    params.put("instType", "SPOT");
                    params.put("instId", symbolPair);
                    String uri = super.buildUrl(PATH, params);
                    HttpRequestBase httpRequest = super.encode("get", uri, "", String.valueOf(index));
                    OkxOrderListResponse okxOrderListResponse = httpClient.execute(httpRequest, this::decode);
                    log.info("OpenOrderClient.getOpenOrders symbol {} okxOrderListResponse is {}", symbolPair, JacksonUtil.toJsonStr(okxOrderListResponse));
                    allOrderList.addAll(this.convertOkxOrderToSystemCommonOrder(okxOrderListResponse));
                }
            } catch (Exception ex) {
                log.error("OpenOrderClient.getOpenOrders.error symbols {} error is {}", JacksonUtil.toJsonStr(symbolPairs), ex.getMessage());
            }
        }
        return allOrderList;
    }

    private OkxOrderListResponse decode(HttpResponse httpResponse) throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        String entityString = EntityUtils.toString(entity);
        return JacksonUtil.from(entityString, OkxOrderListResponse.class);
    }

    private List<Order> convertOkxOrderToSystemCommonOrder(OkxOrderListResponse okxOrderListResponse) {
        List<Order> orderList = new ArrayList<>();
        String code = okxOrderListResponse.getCode();
        if ("0".equals(code)) {
            List<OkxOrderListResponseData> okxOrderListResponseDataList = okxOrderListResponse.getData();
            for (OkxOrderListResponseData item : okxOrderListResponseDataList) {
                Order order = new Order();
                // 这边是有点问题的 不能切换到 BNBUSDT，要保持原状：BNB-USDT
                order.setSymbol(item.getInstId());
                order.setOrderId(item.getOrdId());
                order.setClientOrderId(item.getClOrdId());
                order.setPrice(item.getPx());
                order.setOrigQty(item.getSz());
                order.setStatus(okxBridgeSystemUtil.convertStatus(item.getState()));
                order.setSide(okxBridgeSystemUtil.convertSide(item.getSide()));
                order.setTime(Long.parseLong(item.getCreateTime()));
                orderList.add(order);
            }
        }
        return orderList;
    }
}
