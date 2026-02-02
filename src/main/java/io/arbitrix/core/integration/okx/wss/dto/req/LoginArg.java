package io.arbitrix.core.integration.okx.wss.dto.req;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginArg {
    private String apiKey;
    private String passphrase;
    private String timestamp;
    private String sign;
}
