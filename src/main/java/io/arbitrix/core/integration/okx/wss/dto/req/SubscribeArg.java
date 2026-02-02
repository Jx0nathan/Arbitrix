package io.arbitrix.core.integration.okx.wss.dto.req;

import lombok.Builder;
import lombok.Data;

/**
 * @author jonathan.ji
 */
@Builder
@Data
public class SubscribeArg {
    /**
     * 频道名
     */
    private String channel;

    /**
     * 产品类型
     */
    private String instType;

    /**
     * 产品ID
     */
    private String instId;

    private String ccy;
}
