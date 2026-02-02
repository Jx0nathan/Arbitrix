package io.arbitrix.core;

import io.arbitrix.core.common.constant.Constants;
import io.arbitrix.core.common.util.IPUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Arbitrix - High-performance cryptocurrency market making engine
 *
 * @author jonathan.ji
 */
@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = {
        "io.arbitrix.core.integration.bitget.rest.api",
        "io.arbitrix.core.integration.bybit.rest.api"
})
public class Application {

    public static void main(String[] args) {
        System.setProperty(Constants.LOCAL_IP, IPUtils.getIp());
        SpringApplication.run(Application.class, args);
    }
}
