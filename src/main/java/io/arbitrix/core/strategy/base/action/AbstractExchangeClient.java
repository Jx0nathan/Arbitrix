package io.arbitrix.core.strategy.base.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.affinity.AffinityLock;
import io.arbitrix.core.common.domain.ExchangeOrder;
import io.arbitrix.core.common.domain.Order;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.facade.MarketFacade;
import io.arbitrix.core.integration.binance.wss.BinanceWebSocketClient;
import io.arbitrix.core.integration.bitget.rest.BitgetRestClient;
import io.arbitrix.core.integration.bybit.rest.BybitRestClient;
import io.arbitrix.core.integration.okx.rest.OkxCancelOrderClient;
import io.arbitrix.core.integration.okx.rest.OkxPlaceOrderClient;
import io.arbitrix.core.utils.executor.CancelOrderExecutor;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jonathan.ji
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractExchangeClient {
    private final ThreadPoolExecutor CANCEL_ORDER_EXECUTOR = CancelOrderExecutor.getInstance();
    private final MarketFacade marketFacade;
    private final BinanceWebSocketClient binanceWebSocketApiClient;
    private final BitgetRestClient bitgetRestClient;
    private final BybitRestClient bybitRestClient;
    private final OkxPlaceOrderClient okxPlaceOrderClient;
    private final OkxCancelOrderClient okxCancelOrderClient;

    public void newOrderByRestAndWss(String exchangeName, ExchangeOrder sportOrder, String category) {
        if (ExchangeNameEnum.BINANCE.name().equalsIgnoreCase(exchangeName)) {
            // 通过rest和wss下单
            binanceWebSocketApiClient.placeOrder(sportOrder);
            //binanceClient.newOrderBatch(sportOrder);
        }

        // 由于okx的下单接口不是幂等的, 无法像binance一样同时使用wss和rest
        if (ExchangeNameEnum.OKX.name().equalsIgnoreCase(exchangeName)) {
            okxPlaceOrderClient.placeOrder(sportOrder);
        }

        // bitget
        if (ExchangeNameEnum.BITGET.name().equalsIgnoreCase(exchangeName)) {
            bitgetRestClient.placeOrder(sportOrder);
        }

        // bybit
        if (ExchangeNameEnum.BYBIT.name().equalsIgnoreCase(exchangeName)) {
            bybitRestClient.placeOrder(sportOrder, category);
        }
    }

    public void cancelOrder(String exchangeName, String symbol, String clientId, String category) {
        CANCEL_ORDER_EXECUTOR.submit(() -> {
            try (@SuppressWarnings("unused") final AffinityLock al5 = AffinityLock.acquireLock(5)) {
                if (ExchangeNameEnum.BINANCE.name().equalsIgnoreCase(exchangeName)) {
                    binanceWebSocketApiClient.cancelOrder(symbol, clientId);
                }

                if (ExchangeNameEnum.OKX.name().equalsIgnoreCase(exchangeName)) {
                    okxCancelOrderClient.cancel(clientId, symbol);
                }

                if (ExchangeNameEnum.BITGET.name().equalsIgnoreCase(exchangeName)) {
                    bitgetRestClient.cancel(symbol, clientId);
                }
                if (ExchangeNameEnum.BYBIT.name().equalsIgnoreCase(exchangeName)) {
                    if (bybitRestClient.canUseBatchCancel(category)) {
                        bybitRestClient.cancelBatch(category, List.of(Order.builder().symbol(symbol).clientOrderId(clientId).build()));
                    } else {
                        bybitRestClient.cancel(symbol, category, clientId);
                    }
                }
            } catch (Exception ex) {
                log.error("AbstractExchangeClient.cancelOrder error ", ex);
            }
        });
    }

    protected String calculateQuantity(String exchangeName, String symbol, String category, BigDecimal price, String quoteCoinValue) {
        return marketFacade.getSymbolLimitInfo(exchangeName, category, symbol).quoteCoinValue2BaseCoinValueFloor(price, quoteCoinValue);
    }
}
