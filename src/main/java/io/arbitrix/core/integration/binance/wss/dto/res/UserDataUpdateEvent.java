package io.arbitrix.core.integration.binance.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import io.arbitrix.core.common.event.OrderTradeUpdateEvent;
import io.arbitrix.core.common.monitor.event.MonitorableEvent;
import io.arbitrix.core.integration.binance.constant.BinanceApiConstants;
import io.arbitrix.core.common.exception.UnsupportedEventException;

/**
 * User data update event which can be of four types:
 * <p>
 * 1) outboundAccountInfo, whenever there is a change in the account (e.g. balance of an asset)
 * 2) outboundAccountPosition, the change in account balances caused by an event.
 * 3) executionReport, whenever there is a trade or an order
 * 4) balanceUpdate, the change in account balance (delta).
 * <p>
 * Deserialization could fail with UnsupportedEventException in case of unsupported eventType.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = UserDataUpdateEventDeserializer.class)
@Data
public class UserDataUpdateEvent implements MonitorableEvent {

    private UserDataUpdateEventType eventType;

    private long eventTime;

    private AccountUpdateEvent outboundAccountPositionUpdateEvent;

    private BalanceUpdateEvent balanceUpdateEvent;

    private OrderTradeUpdateEvent orderTradeUpdateEvent;

    @Override
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("eventType", eventType)
                .append("eventTime", eventTime);
        if (eventType == UserDataUpdateEventType.ACCOUNT_POSITION_UPDATE) {
            sb.append("outboundAccountPositionUpdateEvent", outboundAccountPositionUpdateEvent);
        } else if (eventType == UserDataUpdateEventType.BALANCE_UPDATE) {
            sb.append("balanceUpdateEvent", balanceUpdateEvent);
        } else {
            sb.append("orderTradeUpdateEvent", orderTradeUpdateEvent);
        }
        return sb.toString();
    }

    @Override
    public String eventTime() {
        return String.valueOf(eventTime);
    }

    @Override
    public String topic() {
        return eventType.getEventTypeId();
    }

    public enum UserDataUpdateEventType {
        /**
         * Corresponds to "outboundAccountPosition" events.
         */
        ACCOUNT_POSITION_UPDATE("outboundAccountPosition"),
        /**
         * Corresponds to "balanceUpdate" events.
         */
        BALANCE_UPDATE("balanceUpdate"),
        /**
         * Corresponds to "executionReport" events.
         */
        ORDER_TRADE_UPDATE("executionReport"),
        ;

        private final String eventTypeId;

        UserDataUpdateEventType(String eventTypeId) {
            this.eventTypeId = eventTypeId;
        }

        public String getEventTypeId() {
            return eventTypeId;
        }

        public static UserDataUpdateEventType fromEventTypeId(String eventTypeId) {
            if (ORDER_TRADE_UPDATE.eventTypeId.equals(eventTypeId)) {
                return ORDER_TRADE_UPDATE;
            } else if (ACCOUNT_POSITION_UPDATE.eventTypeId.equals(eventTypeId)) {
                return ACCOUNT_POSITION_UPDATE;
            } else if (BALANCE_UPDATE.eventTypeId.equals(eventTypeId)) {
                return BALANCE_UPDATE;
            }
            throw new UnsupportedEventException("Unrecognized user data update event type id: " + eventTypeId);
        }
    }
}
