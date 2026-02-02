package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OkxSubscribeOrderInfoDetail {
    /**
     * 产品ID BTC-USDT
     */
    private String instId;

    /**
     * 产品类型,SPOT
     */
    private String instType;

    /**
     * 订单ID
     */
    private String ordId;

    /**
     * 由用户设置的订单ID来识别您的订单
     */
    private String clOrdId;

    /**
     * 持仓创建时间，Unix时间戳的毫秒数格式，如 1597026383085
     */
    private String cTime;

    /**
     * 保证金币种，仅适用于单币种保证金账户下的全仓币币杠杆订单
     */
    private String ccy;


    private String code;

    /**
     * 订单交易累计的手续费与返佣
     */
    private String fee;

    /**
     * 最新一笔成交的手续费金额或者返佣金额
     */
    private String fillFee;

    /**
     * 最新一笔成交的手续费币种或者返佣币种
     */
    private String fillFeeCcy;

    /**
     * 委托价格
     */
    private String px;

    /**
     * 最新成交价格
     */
    private String fillPx;

    /**
     * 原始委托数量，币币/币币杠杆，以币为单位
     */
    private String sz;

    /**
     * 最新成交数量
     */
    private String fillSz;

    /**
     * 最新成交时间
     */
    private String fillTime;

    /**
     * 订单类型
     * market：市价单
     * limit：限价单
     * post_only： 只做maker单
     * fok：全部成交或立即取消单
     * ioc：立即成交并取消剩余单
     * optimal_limit_ioc：市价委托立即成交并取消剩余（仅适用交割、永续）
     */
    private String ordType;

    /**
     * 修改订单时使用的request ID，如果没有修改，该字段为""
     */
    private String reqId;

    /**
     * 订单方向，buy sell
     */
    private String side;

    /**
     * 订单状态
     * canceled：撤单成功
     * live：等待成交
     * partially_filled： 部分成交
     * filled：完全成交
     */
    private String state;


    private String uTime;

}
