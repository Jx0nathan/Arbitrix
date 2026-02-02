package io.arbitrix.core.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.util.StringUtils;
import io.arbitrix.core.common.enums.OrderLevel;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;

import java.util.Objects;

/**
 * BookTickerEvent event for a symbol. Pushes any update to the best bid or
 * ask's price or quantity in real-time for a specified symbol.
 *
 * <a href="https://binance-docs.github.io/apidocs/spot/cn/#ticker">...</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookTickerEvent {

    /**
     * order book updateId
     */
    @JsonProperty("u")
    private long updateId;

    /**
     * 交易对
     */
    @JsonProperty("s")
    private String symbol;

    /**
     * 买单最优挂单价格
     */
    @JsonProperty("b")
    private String bidPrice;

    /**
     * 买单最优挂单数量
     */
    @JsonProperty("B")
    private String bidQuantity;

    /**
     * 卖单最优挂单价格
     */
    @JsonProperty("a")
    private String askPrice;

    /**
     * 卖单最优挂单数量
     */
    @JsonProperty("A")
    private String askQuantity;

    private String lastPrice;
    private Long arrivalTime;

    private OrderLevel orderLevel;

    public BookTickerEvent(String symbol, String bidPrice, String bidQuantity, String askPrice, String askQuantity) {
        super();
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.bidQuantity = bidQuantity;
        this.askPrice = askPrice;
        this.askQuantity = askQuantity;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE).append("eventType", "BookTicker")
                .append("updateId", updateId).append("symbol", symbol).append("bidPrice", bidPrice)
                .append("bidQuantity", bidQuantity).append("askPrice", askPrice).append("askQuantity", askQuantity)
                .toString();
    }

    public void populateAllPriceIfNeed(BookTickerEvent oldBookTickerEvent) {
        if (Objects.isNull(oldBookTickerEvent)) {
            return;
        }
        if (StringUtils.isEmpty(this.getAskPrice())) {
            this.setAskPrice(oldBookTickerEvent.getAskPrice());
            this.setAskQuantity(oldBookTickerEvent.getAskQuantity());
        }
        if (StringUtils.isEmpty(this.getBidPrice())) {
            this.setBidPrice(oldBookTickerEvent.getBidPrice());
            this.setBidQuantity(oldBookTickerEvent.getBidQuantity());
        }
    }
}