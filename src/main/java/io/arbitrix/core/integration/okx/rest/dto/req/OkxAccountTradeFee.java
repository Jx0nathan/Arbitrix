package io.arbitrix.core.integration.okx.rest.dto.req;

import lombok.Data;

/**
 * @author jonathan.ji
 */
@Data
public class OkxAccountTradeFee {

    /**
     * 交割手续费率
     */
    private String delivery;

    /**
     * 行权手续费率
     */
    private String exercise;

    /**
     * 产品类型
     */
    private String instType;

    /**
     * 手续费等级
     */
    private String level;

    /**
     * USDT&USDⓈ&Crypto 交易区挂单手续费率，永续和交割合约时，为币本位合约费率
     */
    private String maker;

    /**
     * USDT 合约挂单手续费率，仅适用于交割/永续
     *
     * maker/taker的值：正数，代表是返佣的费率；负数，代表平台扣除的费率 (这个很坑的是和网页上的说法正好是反的)
     */
    private String makerU;

    /**
     * USDC 交易区的挂单手续费率，包括 USDC 现货和 USDC 合约
     */
    private String makerUSDC;

    /**
     * USDT&USDⓈ&Crypto 交易区的吃单手续费率，永续和交割合约时，为币本位合约费率
     *
     * maker/taker的值：正数，代表是返佣的费率；负数，代表平台扣除的费率 (这个很坑的是和网页上的说法正好是反的)
     *
     */
    private String taker;

    /**
     * USDT 合约吃单手续费率，仅适用于交割/永续
     */
    private String takerU;

    /**
     * USDC 交易区的吃单手续费率，包括 USDC 现货和 USDC 合约
     */
    private String takerUSDC;

    /**
     * 数据返回时间，Unix时间戳的毫秒数格式，如 1597026383085
     */
    private String ts;
}
