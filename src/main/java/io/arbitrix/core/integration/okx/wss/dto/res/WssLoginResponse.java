package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WssLoginResponse {
    private String event;
    private String code;
    private String msg;
}
