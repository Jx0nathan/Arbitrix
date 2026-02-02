package io.arbitrix.core.integration.bybit.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.bybit.util.SignatureUtils;
import io.arbitrix.core.integration.bybit.config.BybitProperties;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;

import java.net.URI;

/**
 * @author mcx
 * @date 2023/9/26
 * @description
 */
@Component
public class BybitSignatureInterceptor implements RequestInterceptor {
    private final static String RECV_WINDOW = "5000";
    private final BybitProperties bybitProperties;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    public BybitSignatureInterceptor(BybitProperties bybitProperties, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.bybitProperties = bybitProperties;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BYBIT)) {
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        final URI requestURI = URI.create(template.url());
        try {
            boolean requestBodyIsEmpty = template.requestBody() == null || template.requestBody().length() <= 0;
            String sign = SignatureUtils.generate(timestamp,
                    bybitProperties.getApiKey(),
                    bybitProperties.getSecretKey(),
                    RECV_WINDOW,
                    requestURI.getQuery(),
                    requestBodyIsEmpty ? "" : template.requestBody().asString());


            template.header("X-BAPI-API-KEY", bybitProperties.getApiKey());
            template.header("X-BAPI-SIGN", sign);
            template.header("X-BAPI-TIMESTAMP", timestamp);
            template.header("X-BAPI-RECV-WINDOW", RECV_WINDOW);
            template.header("Content-Type", "application/json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
