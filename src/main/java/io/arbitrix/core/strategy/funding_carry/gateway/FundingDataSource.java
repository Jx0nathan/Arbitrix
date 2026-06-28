package io.arbitrix.core.strategy.funding_carry.gateway;

import io.arbitrix.core.strategy.funding_carry.model.FundingSnapshot;

/**
 * Supplies funding rate + spot/perp prices for a (exchange, symbol). Implement an adapter per exchange
 * on top of the existing REST clients (OKX funding-rate, Binance premiumIndex, Bybit tickers.fundingRate)
 * and {@code MarketFacade} for prices.
 *
 * @author jonathan.ji
 */
public interface FundingDataSource {

    /** Returns the latest snapshot, or null if unavailable this tick. */
    FundingSnapshot fetch(String exchange, String symbol);

    /** Whether market/account connectivity for the exchange is currently healthy. */
    boolean isConnected(String exchange);
}
