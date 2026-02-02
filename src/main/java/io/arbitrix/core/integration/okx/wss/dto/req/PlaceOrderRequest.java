package io.arbitrix.core.integration.okx.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
public class PlaceOrderRequest {

    private String id;

    private String op;

    private List<Arg> args;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public class Arg {
        /**
         * 由用户设置的订单ID
         */
        private String clOrdId;

        /**
         * 订单方向，buy sell
         */
        private String side;

        /**
         * 产品ID，如 BTC-USD-190927-5000-C
         */
        private String instId;

        /**
         * 交易模式
         * 保证金模式 isolated：逐仓 cross： 全仓
         * 非保证金模式 cash：现金
         */
        private String tdMode;

        /**
         * 订单类型
         * market：市价单
         * limit：限价单
         * post_only：只做maker单
         * fok：全部成交或立即取消
         * ioc：立即成交并取消剩余
         * optimal_limit_ioc：市价委托立即成交并取消剩余（仅适用交割、永续）
         */
        private String ordType;

        /**
         * 委托数量
         */
        private String sz;

        /**
         * 委托价
         */
        private String px;
    }
}
