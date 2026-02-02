package io.arbitrix.core.integration.okx.rest.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OkxApiInfo {
    private String apiKey;
    private String secretKey;
    private String passprhase;
    private int priority;
}
