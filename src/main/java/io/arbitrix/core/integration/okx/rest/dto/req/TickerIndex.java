package io.arbitrix.core.integration.okx.rest.dto.req;

import lombok.Data;

/**
 * @author jonathan.ji
 */
@Data
public class TickerIndex {

    /**
     * 指数
     */
    private String instId;

    /**
     * 最新指数价格
     */
    private String idxPx;

    /**
     * 24小时指数最高价格
     */
    private String high24h;

    /**
     * UTC 0 时开盘价
     */
    private String sodUtc0;

    /**
     * 24小时指数开盘价格
     */
    private String open24h;

    /**
     * 24小时指数最低价格
     */
    private String low24h;

    /**
     * UTC+8 时开盘价
     */
    private String sodUtc8;

    /**
     * 指数价格更新时间，Unix时间戳的毫秒数格式，如1597026383085
     */
    private String ts;
}
