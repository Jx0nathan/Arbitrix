package io.arbitrix.core.integration.bybit.config;

import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.http.HttpHeaderHandler;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for excluding certain HTTP headers from Bybit API requests
 * Some headers like 'host' can cause 403 errors when forwarded
 */
@Component
public class BybitExcludeHttpHeaderHandler implements HttpHeaderHandler {

    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    @Value("#{'${bybit.api.exclude.headers:host}'.split(',')}")
    private List<String> excludeHeaders;

    public BybitExcludeHttpHeaderHandler(ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
    }

    @Override
    public boolean shouldIncludeHeader(String headerKey) {
        if (!exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BYBIT)) {
            return true;
        }
        return !excludeHeaders.contains(headerKey);
    }
}
