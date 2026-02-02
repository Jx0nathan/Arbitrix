package io.arbitrix.core.integration.binance.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TickerEvent {

    /**
     * 事件类型
     */
    @JsonProperty("e")
    private String eventType;

    /**
     * 事件时间
     */
    @JsonProperty("E")
    private long eventTime;

    /**
     * 交易对
     */
    @JsonProperty("s")
    private String symbol;

    /**
     * 24小时价格变化
     */
    @JsonProperty("p")
    private String priceChange;

    /**
     * 24小时价格变化(百分比)
     */
    @JsonProperty("P")
    private String priceChangePercent;

    /**
     * 平均价格
     */
    @JsonProperty("w")
    private String weightedAveragePrice;

    /**
     * 整整24小时之前，向前数的最后一次成交价格
     */
    @JsonProperty("x")
    private String previousDaysClosePrice;

    /**
     * 最新成交价格
     */
    @JsonProperty("c")
    private String currentDaysClosePrice;

    /**
     * 最新成交交易的成交量
     */
    @JsonProperty("Q")
    private String closeTradesQuantity;

    /**
     * 目前最高买单价
     */
    @JsonProperty("b")
    private String bestBidPrice;

    /**
     * 目前最高买单价的挂单量
     */
    @JsonProperty("B")
    private String bestBidQuantity;

    /**
     * 目前最低卖单价
     */
    @JsonProperty("a")
    private String bestAskPrice;

    /**
     * 目前最低卖单价的挂单量
     */
    @JsonProperty("A")
    private String bestAskQuantity;

    /**
     * 整整24小时前，向后数的第一次成交价格
     */
    @JsonProperty("o")
    private String openPrice;

    /**
     * 24小时内最高成交价
     */
    @JsonProperty("h")
    private String highPrice;

    /**
     * 24小时内最低成交价
     */
    @JsonProperty("l")
    private String lowPrice;

    /**
     * 24小时内成交量
     */
    @JsonProperty("v")
    private String totalTradedBaseAssetVolume;

    /**
     * 24小时内成交额
     */
    @JsonProperty("q")
    private String totalTradedQuoteAssetVolume;

    /**
     * 统计开始时间
     */
    @JsonProperty("O")
    private long statisticsOpenTime;

    /**
     * 统计结束时间
     */
    @JsonProperty("C")
    private long statisticsCloseTime;

    /**
     * 24小时内第一笔成交交易ID
     */
    @JsonProperty("F")
    private long firstTradeId;

    /**
     * 24小时内最后一笔成交交易ID
     */
    @JsonProperty("L")
    private long lastTradeId;

    /**
     * 24小时内成交数
     */
    @JsonProperty("n")
    private long totalNumberOfTrades;

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("eventType", eventType)
                .append("eventTime", eventTime)
                .append("symbol", symbol)
                .append("priceChange", priceChange)
                .append("priceChangePercent", priceChangePercent)
                .append("weightedAveragePrice", weightedAveragePrice)
                .append("previousDaysClosePrice", previousDaysClosePrice)
                .append("currentDaysClosePrice", currentDaysClosePrice)
                .append("closeTradesQuantity", closeTradesQuantity)
                .append("bestBidPrice", bestBidPrice)
                .append("bestBidQuantity", bestBidQuantity)
                .append("bestAskPrice", bestAskPrice)
                .append("bestAskQuantity", bestAskQuantity)
                .append("openPrice", openPrice)
                .append("highPrice", highPrice)
                .append("lowPrice", lowPrice)
                .append("totalTradedBaseAssetVolume", totalTradedBaseAssetVolume)
                .append("totalTradedQuoteAssetVolume", totalTradedQuoteAssetVolume)
                .append("statisticsOpenTime", statisticsOpenTime)
                .append("statisticsCloseTime", statisticsCloseTime)
                .append("firstTradeId", firstTradeId)
                .append("lastTradeId", lastTradeId)
                .append("totalNumberOfTrades", totalNumberOfTrades)
                .toString();
    }
}
