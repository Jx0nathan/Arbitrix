package io.arbitrix.core.integration.okx.rest.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author jonathan.ji
 */
@Data
public class OkxOrdersHistory {

    /**
     * 产品类型
     */
    private String instType;

    /**
     * 产品ID
     */
    private String instId;

    /**
     * 保证金币种，仅适用于单币种保证金模式下的全仓币币杠杆订单
     */
    private String ccy;

    /**
     * 订单ID
     */
    private String ordId;

    /**
     * 客户自定义订单ID
     */
    private String clOrdId;

    /**
     * 订单标签
     */
    private String tag;

    /**
     * 委托价格，对于期权，以币(如BTC, ETH)为单位
     */
    private String px;

    /**
     * 期权价格，以USD为单位 仅适用于期权，其他业务线返回空字符串""
     */
    private String pxUsd;

    /**
     * 期权订单的隐含波动率 仅适用于期权，其他业务线返回空字符串""
     */
    private String pxVol;

    /**
     * 期权的价格类型
     * px：代表按价格下单，单位为币 (请求参数 px 的数值单位是BTC或ETH)
     * pxVol：代表按pxVol下单
     * pxUsd：代表按照pxUsd下单，单位为USD (请求参数px 的数值单位是USD)
     */
    private String pxType;

    /**
     * 委托数量
     */
    private String sz;

    /**
     * 订单类型
     * market：市价单
     * limit：限价单
     * post_only：只做maker单
     * fok：全部成交或立即取消
     * ioc：立即成交并取消剩余
     * optimal_limit_ioc：市价委托立即成交并取消剩余（仅适用交割、永续）
     * mmp：做市商保护(仅适用于组合保证金账户模式下的期权订单)
     * mmp_and_post_only：做市商保护且只做maker单(仅适用于组合保证金账户模式下的期权订单)
     */
    private String ordType;

    /**
     * 订单方向
     */
    private String side;

    /**
     * 持仓方向
     */
    private String posSide;

    /**
     * 交易模式
     */
    private String tdMode;

    /**
     * 累计成交数量
     */
    private String accFillSz;

    /**
     * 最新成交价格，如果成交数量为0，该字段为""
     */
    private String fillPx;

    /**
     * 最新成交ID
     */
    private String tradeId;

    /**
     * 最新成交数量
     */
    private String fillSz;

    /**
     * 最新成交时间
     */
    private String fillTime;

    /**
     * 订单来源
     * 13:策略委托单触发后的生成的限价单
     */
    private String source;

    /**
     * 订单状态
     * canceled：撤单成功
     * filled：完全成交
     * mmp_canceled：做市商保护机制导致的自动撤单
     */
    private String state;

    /**
     * 成交均价，如果成交数量为0，该字段也为""
     */
    private String avgPx;

    /**
     * 杠杆倍数，0.01到125之间的数值，仅适用于 币币杠杆/交割/永续
     */
    private String lever;

    /**
     * 下单附带止盈止损时，客户自定义的策略订单ID
     */
    private String attachAlgoClOrdId;

    /**
     * 止盈触发价
     */
    private String tpTriggerPx;

    /**
     * 止盈触发价类型
     * last：最新价格
     * index：指数价格
     * mark：标记价格
     */
    private String tpTriggerPxType;

    /**
     * 止盈委托价
     */
    private String tpOrdPx;

    /**
     * 止损触发价
     */
    private String slTriggerPx;

    /**
     * 止损触发价类型
     * last：最新价格
     * index：指数价格
     * mark：标记价格
     */
    private String slTriggerPxType;

    /**
     * 止损委托价
     */
    private String slOrdPx;

    /**
     * 自成交保护ID
     * 如果自成交保护不适用则返回""
     */
    private String stpId;

    /**
     * 自成交保护模式
     * 如果自成交保护不适用则返回""
     */
    private String stpMode;

    /**
     * 交易手续费币种
     */
    private String feeCcy;

    /**
     * 手续费与返佣
     * 对于币币和杠杆，为订单交易累计的手续费，平台向用户收取的交易手续费，为负数。如： -0.01
     * 对于交割、永续和期权，为订单交易累计的手续费和返佣
     */
    private String fee;

    /**
     * 返佣金币种
     */
    private String rebateCcy;

    /**
     * 返佣金额，仅适用于币币和杠杆，平台向达到指定lv交易等级的用户支付的挂单奖励（返佣），如果没有返佣金，该字段为“”。手续费返佣为正数，如：0.01
     */
    private String rebate;

    /**
     * 币币市价单委托数量sz的单位
     * base_ccy: 交易货币 ；quote_ccy：计价货币
     * 仅适用于币币市价订单
     * 默认买单为quote_ccy，卖单为base_ccy
     */
    private String tgtCcy;

    /**
     * 收益，适用于有成交的平仓订单，其他情况均为0
     */
    private String pnl;

    /**
     * 订单种类
     * normal：普通委托
     * twap：TWAP自动换币
     * adl：ADL自动减仓
     * full_liquidation：强制平仓
     * partial_liquidation：强制减仓
     * delivery：交割
     * ddh：对冲减仓类型订单
     */
    private String category;

    /**
     * 是否只减仓，true 或 false
     */
    private boolean reduceOnly;

    /**
     * 订单取消来源的原因枚举值代码
     */
    private String cancelSource;

    /**
     * 订单取消来源的对应具体原因
     */
    private String cancelSourceReason;

    /**
     * 客户自定义策略订单ID。策略订单触发，且策略单有algoClOrdId时有值，否则为"",
     */
    private String algoClOrdId;

    /**
     * 策略委托单ID，策略订单触发时有值，否则为""
     */
    private String algoId;

    /**
     * 订单状态更新时间，Unix时间戳的毫秒数格式，如1597026383085
     */
    @JsonProperty("uTime")
    private String updateTime;

    /**
     * 订单创建时间，Unix时间戳的毫秒数格式，如 1597026383085
     */
    @JsonProperty("cTime")
    private String createTime;
}
