package io.arbitrix.core.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.arbitrix.core.common.response.AccountTradeFee;
import io.arbitrix.core.common.response.AccountBalance;
import io.arbitrix.core.common.response.CoinBalance;
import io.arbitrix.core.facade.AccountFacade;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jonathan.ji
 */
@RestController
public class AccountController {

    @Resource
    private AccountFacade accountFacade;

    @GetMapping("/getAccountTradeFee")
    public List<AccountTradeFee> getAccountTradeFee(@RequestParam String exchangeName, @RequestParam(required = false) String symbol) {
        if (StringUtils.isEmpty(symbol)) {
            return accountFacade.getAccountTradeFee(exchangeName);
        } else {
            return List.of(accountFacade.getAccountTradeFeeBySymbol(exchangeName, symbol));
        }
    }

    @GetMapping("/getCoinBalance")
    public List<CoinBalance> getAllCoinBalance(@RequestParam String exchangeName, @RequestParam(required = false) String coin) {
        if (StringUtils.isEmpty(coin)) {
            return accountFacade.getAllCoinBalance(exchangeName);
        } else {
            return List.of(accountFacade.getCoinBalance(exchangeName, coin));
        }
    }
}
