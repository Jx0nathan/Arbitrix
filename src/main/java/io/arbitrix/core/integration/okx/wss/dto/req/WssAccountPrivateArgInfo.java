package io.arbitrix.core.integration.okx.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jonathan.ji
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WssAccountPrivateArgInfo extends WssPrivateArgInfo {
    /**
     * 产品类型
     */
    private String instType;

    /**
     * 产品ID
     */
    private String instId;

    /**
     * 用户标识
     */
    private String uid;

}

