package io.arbitrix.okx;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import io.arbitrix.core.Application;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderStatus;
import io.arbitrix.core.integration.okx.rest.enums.OkxOrderType;
import io.arbitrix.core.integration.okx.rest.enums.OkxSide;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrderListResponse;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrderListResponseData;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.integration.okx.rest.OkxCancelOrderClient;
import io.arbitrix.core.integration.okx.rest.OkxOpenOrderClient;
import io.arbitrix.core.integration.okx.rest.OkxPlaceOrderClient;
import io.arbitrix.core.integration.okx.wss.OkxWebSocketClient;
import io.arbitrix.core.common.util.JacksonUtil;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OkxClient {

    @Autowired
    private OkxOpenOrderClient okxOpenOrderClient;

    @Autowired
    private OkxCancelOrderClient okxCancelOrderClient;

    @Autowired
    private OkxPlaceOrderClient okxPlaceOrderClient;

    @Autowired
    private OkxWebSocketClient okxWebSocketClient;
    private List<String> symbolPairs = List.of("BTC-USDT");

    /**
     * 这个测试的目的是为了同一个clientId下，可以下多个订单。并不是互斥的
     */
    @Test
    @Ignore
    public void testApi() {
        List<Order> orderList = okxOpenOrderClient.getOpenOrders(symbolPairs);
        for (Order order : orderList) {
            okxCancelOrderClient.cancel(order.getClientOrderId(), order.getSymbol());
        }

        ExchangeOrder sportOrder = ExchangeOrder.limitMarketBuy("BTC-USDT", "0.1", "26000", UUID.randomUUID().toString());
        okxWebSocketClient.placeOrder(sportOrder);
        okxPlaceOrderClient.placeOrder(sportOrder);
        List<Order> orderList2 = okxOpenOrderClient.getOpenOrders(symbolPairs);
        Assertions.assertEquals(orderList2.size(), 2);
    }

    @Test
    @Ignore
    public void testCancelApi(){
        List<Order> orderList = okxOpenOrderClient.getOpenOrders(symbolPairs);
        String clientOrderId = orderList.get(0).getClientOrderId();
        okxCancelOrderClient.cancel(clientOrderId, "BTC-USDT");
    }

    @Test
    public void testOpenOrderApi(){
        okxOpenOrderClient.getOpenOrders(symbolPairs);
    }

    /**
     * cTime序列化的问题 -> getCTime导致序列化失败
     */
    @Test
    public void okxOrderListResponseTest() {
        String json = "{\"code\":\"0\",\"data\":[{\"accFillSz\":\"0\",\"algoClOrdId\":\"\",\"algoId\":\"\",\"attachAlgoClOrdId\":\"\",\"avgPx\":\"\",\"cTime\":\"1694098181027\",\"cancelSource\":\"\",\"cancelSourceReason\":\"\",\"category\":\"normal\",\"ccy\":\"\",\"clOrdId\":\"2e4edcf8caa84afbbace6dd704358255\",\"fee\":\"0\",\"feeCcy\":\"BTC\",\"fillPx\":\"\",\"fillSz\":\"0\",\"fillTime\":\"\",\"instId\":\"BTC-USDT\",\"instType\":\"SPOT\",\"lever\":\"\",\"ordId\":\"620027942387060773\",\"ordType\":\"post_only\",\"pnl\":\"0\",\"posSide\":\"net\",\"px\":\"21000\",\"pxType\":\"\",\"pxUsd\":\"\",\"pxVol\":\"\",\"quickMgnType\":\"\",\"rebate\":\"0\",\"rebateCcy\":\"USDT\",\"reduceOnly\":\"false\",\"side\":\"buy\",\"slOrdPx\":\"\",\"slTriggerPx\":\"\",\"slTriggerPxType\":\"\",\"source\":\"\",\"state\":\"live\",\"stpId\":\"\",\"stpMode\":\"\",\"sz\":\"0.001\",\"tag\":\"\",\"tdMode\":\"cash\",\"tgtCcy\":\"\",\"tpOrdPx\":\"\",\"tpTriggerPx\":\"\",\"tpTriggerPxType\":\"\",\"tradeId\":\"\",\"uTime\":\"1694098181027\"}],\"msg\":\"\"}";
        OkxOrderListResponse response = JacksonUtil.from(json, OkxOrderListResponse.class);
        OkxOrderListResponseData item = response.getData().get(0);

        Assertions.assertEquals(item.getCreateTime(), "1694098181027");
        Assertions.assertEquals(item.getOrdId(), "620027942387060773");
        Assertions.assertEquals(item.getClOrdId(), "2e4edcf8caa84afbbace6dd704358255");
        Assertions.assertEquals(item.getState(), OkxOrderStatus.Live);
        Assertions.assertEquals(item.getInstId(), "BTC-USDT");
        Assertions.assertEquals(item.getSide(), OkxSide.BUY);
        Assertions.assertEquals(item.getOrdType(), OkxOrderType.POST_ONLY);
        Assertions.assertEquals(item.getPx(), "21000");
        Assertions.assertEquals(item.getSz(), "0.001");
    }
}
