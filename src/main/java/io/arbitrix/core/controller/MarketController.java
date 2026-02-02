package io.arbitrix.core.controller;

import org.eclipse.jetty.util.StringUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.arbitrix.core.common.event.BookTickerEvent;
import io.arbitrix.core.facade.MarketFacade;
import io.arbitrix.core.utils.ExchangeMarketOpenUtilV2;
import io.arbitrix.core.utils.SystemClock;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/market")
public class MarketController {

    @Resource
    private MarketFacade marketFacade;
    @Resource
    private ExchangeMarketOpenUtilV2 exchangeMarketOpenUtilV2;

    @GetMapping("/serverTime")
    public Map<String, String> getServerTime(@RequestParam(required = false) String exchangeName) {
        if (StringUtil.isEmpty(exchangeName)) {
            exchangeName = exchangeMarketOpenUtilV2.getExchange().name();
        }
        Map<String, String> result = new HashMap<>(2);
        long localServerTime = SystemClock.now();
        long exchangeServerTime = marketFacade.getServerTime(exchangeName);
        result.put("localServerTime", String.valueOf(localServerTime));
        result.put("exchangeServerTime", String.valueOf(exchangeServerTime));
        result.put("diff", String.valueOf(localServerTime - exchangeServerTime));
        result.put("exchangeName", exchangeName);
        return result;
    }

    @GetMapping("/lastTicker")
    public BookTickerEvent lastTicker(@RequestParam(required = false) String exchangeName, @RequestParam(required = false) String symbol) {
        if (StringUtil.isEmpty(exchangeName)) {
            exchangeName = exchangeMarketOpenUtilV2.getExchange().name();
        }
        if (StringUtil.isEmpty(symbol)) {
            symbol = "BTCUSDT";
        }
        return marketFacade.lastTicker(exchangeName, symbol);
    }
}
