package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.Data;

@Data
public class SportTickerInfoResDetail {
    /**
     * 合約名稱
     */
    private String symbol;

    /**
     * 買1價
     */
    private String bid1Price;

    /**
     * 買1價的數量
     */
    private String bid1Size;

    /**
     * 賣1價
     */
    private String ask1Price;

    /**
     * 賣1價的數量
     */
    private String ask1Size;

    /**
     * 最新市場成交價
     */
    private String lastPrice;

    /**
     * 24小時前的整點市價
     */
    private String prevPrice24h;

    /**
     * 市場價格相對24h前變化的百分比
     */
    private String price24hPcnt;

    /**
     * 最近24小時的最高價
     */
    private String highPrice24h;

    /**
     * 最近24小時的最低價
     */
    private String lowPrice24h;

    /**
     * 最近24小時成交額
     */
    private String turnover24h;

    /**
     * 最近24小時成交量
     */
    private String usdIndexPrice;

}
