package io.arbitrix.core.integration.okx.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import io.arbitrix.core.integration.okx.LoginRequestSign;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiInfo;
import io.arbitrix.core.integration.okx.streamer.OKXWalletStreamer;
import io.arbitrix.core.integration.okx.wsshandler.OkxAccountWebsocketHandler;
import io.arbitrix.core.strategy.base.condition.ExchangeConditional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Configuration
@ExchangeConditional(exchangeName = "OKX")
public class OkxConfiguration{
    @Resource
    public LoginRequestSign loginRequestSign;
    @Resource
    public ConfigurableListableBeanFactory beanFactory;
    @Resource
    private OKXWalletStreamer okxWalletStreamer;
    @PostConstruct
    public void prepareOkx(){
        // 注册okxAccountWebsocketHandler
        List<OkxApiInfo> okxApiInfoList = loginRequestSign.getOkxApiInfoList();
        for (int i = 0; i < okxApiInfoList.size(); i++) {
            beanFactory.registerSingleton("okxAccountWebsocketHandler" + i, new OkxAccountWebsocketHandler(loginRequestSign, i, okxWalletStreamer));
        }
    }
}
