package io.arbitrix.core.integration.bitget.rest.api;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.integration.bitget.rest.enums.HttpHeader;
import io.arbitrix.core.integration.bitget.rest.enums.SupportedLocaleEnum;
import io.arbitrix.core.integration.bitget.config.BitgetProperties;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.integration.bitget.util.SignatureUtils;

import java.net.URI;

/**
 * @author mcx
 * @date 2023/9/26
 * @description
 */
@Component
public class BitgetSignatureInterceptor implements RequestInterceptor {
    private final BitgetProperties bitgetProperties;
    private final ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil;

    public BitgetSignatureInterceptor(BitgetProperties bitgetProperties, ExchangeMarketOpenUtilV2 exchangeMarketOpenUtil) {
        this.bitgetProperties = bitgetProperties;
        this.exchangeMarketOpenUtil = exchangeMarketOpenUtil;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!exchangeMarketOpenUtil.exchangeCanStart(ExchangeNameEnum.BITGET)) {
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentType = "application/json";
        final URI requestURI = URI.create(template.url());
        try {
            String sign = SignatureUtils.generate(timestamp,
                    template.method(),
                    requestURI.getPath(),
                    requestURI.getQuery(),
                    template.requestBody() == null ? "" : template.requestBody().asString(),
                    bitgetProperties.getSecretKey());

            String localFormat = "locale=%s";

            template.header(HttpHeader.ACCESS_KEY, bitgetProperties.getApiKey());
            template.header(HttpHeader.ACCESS_PASSPHRASE, bitgetProperties.getPassPhrase());
            template.header(HttpHeader.ACCESS_SIGN, sign);
            template.header(HttpHeader.CONTENT_TYPE, contentType);
            template.header(HttpHeader.COOKIE, String.format(localFormat, SupportedLocaleEnum.EN_US.getName()));
            template.header(HttpHeader.LOCALE, SupportedLocaleEnum.EN_US.getName());
            template.header(HttpHeader.ACCESS_TIMESTAMP, timestamp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
