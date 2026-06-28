package io.arbitrix.core.strategy.funding_carry.gateway;

import io.arbitrix.core.strategy.funding_carry.model.CarryPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Executes the two legs of a cash-and-carry atomically. The implementation MUST guarantee that either both
 * legs (spot buy + perp short) succeed or any filled leg is unwound — never leave a naked directional leg.
 * <p>
 * Build a real adapter on top of {@code AbstractExchangeClient} (spot) and the future order path
 * ({@code FutureOpenExecutor}/{@code FutureCloseExecutor}, {@code BybitPositionClient}).
 *
 * @author jonathan.ji
 */
public interface CarryTradeGateway {

    /** Open spot-long + perp-short for the given notional. */
    OpenResult openCarry(String exchange, String symbol, BigDecimal notionalUsdt,
                         BigDecimal spotPrice, BigDecimal perpPrice);

    /** Unwind both legs of an open position. Returns true only if fully flat afterwards. */
    boolean closeCarry(CarryPosition position);

    @Getter
    @AllArgsConstructor
    class OpenResult {
        private final boolean success;
        /** The resulting position when {@link #success}; otherwise null. */
        private final CarryPosition position;
        /** Error description when not {@link #success}; otherwise null. */
        private final String error;

        public static OpenResult ok(CarryPosition position) {
            return new OpenResult(true, position, null);
        }

        public static OpenResult fail(String error) {
            return new OpenResult(false, null, error);
        }
    }
}
