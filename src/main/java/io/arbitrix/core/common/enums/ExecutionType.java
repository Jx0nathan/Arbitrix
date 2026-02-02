package io.arbitrix.core.common.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Order execution type.
 * @author jonathan.ji
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum ExecutionType {
  /**
   * 新订单已被引擎接受
   */
  NEW,

  /**
   * 订单被用户取消
   */
  CANCELED,

  /**
   * 保留字段，当前未使用
   */
  REPLACED,

  /**
   *  新订单被拒绝 （这信息只会在撤消挂单再下单中发生，下新订单被拒绝但撤消挂单请求成功）
   */
  REJECTED,

  /**
   * 订单有新成交
   */
  TRADE,

  /**
   * 订单已根据 Time In Force 参数的规则取消（e.g. 没有成交的 LIMIT FOK 订单或部分成交的 LIMIT IOC 订单）或者被交易所取消（e.g. 强平或维护期间取消的订单）。
   */
  EXPIRED
}