package io.arbitrix.core.integration.okx;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiDetail;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiInfo;
import io.arbitrix.core.integration.okx.wss.dto.req.LoginArg;
import io.arbitrix.core.integration.okx.wss.dto.req.LoginRequest;
import io.arbitrix.core.common.util.JacksonUtil;

import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <a href="https://www.okx.com/docs-v5/zh/#websocket-api-login">...</a>
 *
 * @author jonathan.ji
 */
@Component
public class LoginRequestSign {
    private static final String LOGIN_REQUEST_PATH = "/users/self/verify";

    @Value("${okx.api.detail:}")
    private String okxApiDetailStr;

    public LoginRequest sign(int priority) {
        OkxApiInfo okxApiInfo = this.getOkxApiInfo(priority);
        String apiKey = okxApiInfo.getApiKey();
        String secretKey = okxApiInfo.getSecretKey();
        String passprhase = okxApiInfo.getPassprhase();

        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String hmacUri = timestamp + "GET" + LOGIN_REQUEST_PATH;
        String okAccessSign = encodeAccessSign(hmacUri, secretKey);

        LoginArg loginArg = LoginArg.builder().apiKey(apiKey).passphrase(passprhase).timestamp(timestamp).sign(okAccessSign).build();
        List<LoginArg> args = Collections.singletonList(loginArg);
        return LoginRequest.builder().op("login").args(args).build();
    }

    private String encodeAccessSign(String hmacUri, String secretKey) {
        byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secretKey).hmac(hmacUri);
        return Base64.getEncoder().encodeToString(hmac);
    }

    private OkxApiInfo getOkxApiInfo(int priority) {
        try {
            OkxApiDetail okxApiDetail = JacksonUtil.from(okxApiDetailStr, OkxApiDetail.class);
            List<OkxApiInfo> okxApiInfoList = okxApiDetail.getOkxApiInfoList();
            okxApiInfoList.sort(Comparator.comparing(OkxApiInfo::getPriority));
            return okxApiInfoList.get(priority);
        } catch (Exception ex) {
            throw new RuntimeException("Incorrect OKX API info priority: " + priority);
        }
    }
    public List<OkxApiInfo> getOkxApiInfoList() {
        try {
            OkxApiDetail okxApiDetail = JacksonUtil.from(okxApiDetailStr, OkxApiDetail.class);
            List<OkxApiInfo> okxApiInfoList = okxApiDetail.getOkxApiInfoList();
            okxApiInfoList.sort(Comparator.comparing(OkxApiInfo::getPriority));
            return okxApiInfoList;
        } catch (Exception ex) {
            throw new RuntimeException("Incorrect OKX API info priority: ", ex);
        }
    }
}
