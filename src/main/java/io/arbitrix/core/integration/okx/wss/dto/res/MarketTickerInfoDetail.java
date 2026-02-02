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
public class MarketTickerInfoDetail {

    /**
     * 产品类型
     */
    private String instType;

    /**
     * 产品ID
     */
    private String instId;

    /**
     * 最新成交价
     */
    private String last;

    /**
     * 最新成交的数量
     */
    private String lastSz;

    /**
     * 卖一价
     */
    private String askPx;

    /**
     * 卖一价对应的量
     */
    private String askSz;

    /**
     * 买一价
     */
    private String bidPx;

    /**
     * 买一价对应的数量
     */
    private String bidSz;

    /**
     * 24小时开盘价
     */
    private String open24h;

    /**
     * 24小时最高价
     */
    private String high24h;

    /**
     * 24小时最低价
     */
    private String low24h;

    /**
     * UTC 0 时开盘价
     */
    private String sodUtc0;

    /**
     * UTC+8 时开盘价
     */
    private String sodUtc8;

    /**
     * 24小时成交量，以币为单位。如果是衍生品合约，数值为交易货币的数量。如果是币币/币币杠杆，数值为计价货币的数量。
     */
    private String volCcy24h;

    /**
     * 24小时成交量，以张为单位。如果是衍生品合约，数值为合约的张数。如果是币币/币币杠杆，数值为交易货币的数量
     */
    private String vol24h;

    /**
     * 数据产生时间，Unix时间戳的毫秒数格式，如 1597026383085
     */
    private String ts;

}
