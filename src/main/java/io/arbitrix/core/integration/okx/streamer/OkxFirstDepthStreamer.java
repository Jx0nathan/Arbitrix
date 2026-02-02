package io.arbitrix.core.integration.okx.streamer;

import io.arbitrix.core.common.util.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketMessage;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.OrderSide;
import io.arbitrix.core.common.enums.WSStreamType;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.okx.wss.constant.DepthConstant;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeArg;
import io.arbitrix.core.integration.okx.wss.dto.req.SubscribeRequest;
import io.arbitrix.core.integration.okx.wss.dto.res.DepthInfo;
import io.arbitrix.core.integration.okx.wsshandler.OkxPublicWebsocketAbstractHandler;
import io.arbitrix.core.strategy.base.action.BookTickerEventListener;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;
import io.arbitrix.core.strategy.base.condition.ExecuteStrategyConditional;
import io.arbitrix.core.strategy.pure_market_making.PureMarketMakingSpotWorker;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.common.util.JacksonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订阅深度信息
 *
 * @author jonathan.ji
 */
@Log4j2
@Component
@ExchangeConditional(exchangeName = "OKX")
@ExecuteStrategyConditional(executeStrategyName = "pure_market_making")
public class OkxFirstDepthStreamer extends OkxPublicWebsocketAbstractHandler {
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;
    private final List<BookTickerEventListener> quotesListenerList;

    public OkxFirstDepthStreamer(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil, PureMarketMakingSpotWorker marketMakerWorker) {
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;

        List<BookTickerEventListener> quotesListenerList = new ArrayList<>();
        quotesListenerList.add(0, marketMakerWorker);
        this.quotesListenerList = quotesListenerList;
    }

    @Override
    public SubscribeRequest getSubscribeRequest() {
        List<String> instIdArray = exchangeMarketOpenUtil.getSymbolPairs(ExchangeNameEnum.OKX);
        List<SubscribeArg> subscribeArgList = new ArrayList<>();
        for (String str : instIdArray) {
            SubscribeArg subscribeArg = SubscribeArg.builder().instId(str).channel(DepthConstant.BBO_TBT).build();
            subscribeArgList.add(subscribeArg);
        }
        return new SubscribeRequest("subscribe", subscribeArgList);
    }

    @Override
    public void handleMessageForChannel(WebSocketMessage<?> message) {
        long receiveTime = SystemClock.now();
        if (exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.OKX)) {
            log.info("OkxFirstDepthStreamer.handleMessageForChannel.message is {}", message.getPayload());
            WSSMonitor.receiveDepth1Event(ExchangeNameEnum.OKX.name());
            DepthInfo depthInfo;
            try {
                depthInfo = JacksonUtil.from((String) message.getPayload(), DepthInfo.class);
            } catch (Exception ex) {
                log.error("OkxFirstDepthStreamer.handleMessage.message.error is {}", message.getPayload(), ex);
                return;
            }

            WSSMonitor.recordDelay(ExchangeNameEnum.OKX.name(), WSStreamType.PUBLIC, depthInfo, receiveTime);

            List<BookTickerEvent> bookTickerEventList = convertDepthInfo(depthInfo);
            log.info("OkxFirstDepthStreamer.handleMessage.message is {}", JacksonUtil.toJsonStr(bookTickerEventList));
            try {
                bookTickerEventList.forEach(bookTickerEvent -> quotesListenerList.forEach(quotesListener -> quotesListener.onBookTicker(ExchangeNameEnum.OKX.name(), bookTickerEvent)));
            } finally {
                TrackingUtils.clearTrace();
            }
        }
    }

    public List<BookTickerEvent> convertDepthInfo(DepthInfo depthInfo) {
        List<BookTickerEvent> bookTickerEventList = new ArrayList<>();

        String instId = depthInfo.getArg().getInstId();
        List<DepthInfo.DataItem> dataItemList = depthInfo.getData();
        if (dataItemList == null || dataItemList.isEmpty()) {
            log.warn("OkxBookTickerStreamer.convertMarketTickerInfoToSelf.convertDepthInfo is empty");
            return bookTickerEventList;
        }

        if (dataItemList.size() > 1) {
            log.warn("OkxBookTickerStreamer.convertMarketTickerInfoToSelf.convertDepthInfo.size.more.than.one {}", dataItemList.size());
        }

        DepthInfo.DataItem dataItem = dataItemList.get(0);

        dataItemList.forEach(item -> {
            BookTickerEvent bookTickerEvent = new BookTickerEvent();
            bookTickerEvent.setSymbol(instId);

            Pair<String, String> bidPair = this.getBestPriceAndQuantity(OrderSide.BUY, instId, dataItem);
            bookTickerEvent.setBidPrice(bidPair.getLeft());
            bookTickerEvent.setBidQuantity(bidPair.getRight());

            Pair<String, String> askPair = this.getBestPriceAndQuantity(OrderSide.SELL, instId, dataItem);
            bookTickerEvent.setAskPrice(askPair.getLeft());
            bookTickerEvent.setAskQuantity(askPair.getRight());
            bookTickerEventList.add(bookTickerEvent);
        });
        return bookTickerEventList;
    }

    /**
     * asks和bids值数组举例说明： ["411.8", "10", "0", "4"]
     * - 411.8为深度价格
     * - 10为此价格的数量 （合约交易为合约，现货/币币杠杆为交易币的数量
     * - 0该字段已弃用(始终为0)
     * - 4为此价格的订单数量
     */
    private Pair<String, String> getBestPriceAndQuantity(OrderSide orderSide, String instId, DepthInfo.DataItem dataItem) {
        List<List<String>> targetList;
        if (orderSide == OrderSide.BUY) {
            targetList = dataItem.getBids();
        } else {
            targetList = dataItem.getAsks();
        }

        if (CollectionUtils.isEmpty(targetList)) {
            log.warn("OkxBookTickerStreamer.getBestPriceAndQuantity.targetList is empty instId is {}", instId);
            return Pair.of(BigDecimal.valueOf(0).toString(), BigDecimal.valueOf(0).toString());
        }

        if (targetList.size() > 1) {
            log.warn("OkxBookTickerStreamer.getBestPriceAndQuantity.targetList.size.more.than.one instId is {}", instId);
        }
        List<String> target = targetList.get(0);

        try {
            return Pair.of(target.get(0), target.get(1));
        } catch (Exception e) {
            log.error("OkxBookTickerStreamer.getBestPriceAndQuantity.targetList.get error instId is {} bid is {}", instId, target, e);
            return Pair.of(BigDecimal.valueOf(0).toString(), BigDecimal.valueOf(0).toString());
        }
    }
}
