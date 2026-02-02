package io.arbitrix.core.common.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Getter
@Slf4j
public enum ExchangeNameEnum {

    BINANCE("BINANCE"),

    BYBIT("BYBIT"),

    BITGET("BITGET"),

    OKX("OKX");

    private final String value;

    public static ExchangeNameEnum getExchangeName(String exchangeName) {
        for (ExchangeNameEnum item : ExchangeNameEnum.values()) {
            if (item.getValue().equalsIgnoreCase(exchangeName)) {
                return item;
            }
        }
        log.error("ExchangeNameEnum getExchangeName error, exchangeName: {}", exchangeName);
        return null;
    }
}
