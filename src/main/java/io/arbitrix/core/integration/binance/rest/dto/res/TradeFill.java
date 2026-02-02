package io.arbitrix.core.integration.binance.rest.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;

/**
 * Represents an executed trade.
 *
 * @author jonathan.ji
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TradeFill {

    /**
     * Trade id.
     */
    private Long id;

    /**
     * Price.
     */
    private String price;

    /**
     * Quantity.
     */
    private String qty;


    /**
     * Quote quantity for the trade (price * qty).
     */
    private String quoteQty;

    /**
     * Commission.
     */
    private String commission;

    /**
     * Asset on which commission is taken
     */
    private String commissionAsset;

    /**
     * Trade execution time.
     */
    private long time;

    /**
     * The symbol of the trade.
     */
    private String symbol;

    @JsonProperty("isBuyer")
    private boolean buyer;

    @JsonProperty("isMaker")
    private boolean maker;

    @JsonProperty("isBestMatch")
    private boolean bestMatch;

    private String orderId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("id", id)
                .append("symbol", symbol)
                .append("price", price)
                .append("qty", qty)
                .append("quoteQty", quoteQty)
                .append("commission", commission)
                .append("commissionAsset", commissionAsset)
                .append("time", time)
                .append("buyer", buyer)
                .append("maker", maker)
                .append("bestMatch", bestMatch)
                .append("orderId", orderId)
                .toString();
    }
}
