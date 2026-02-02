package io.arbitrix.core.utils;

import io.arbitrix.core.common.util.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author jonathan.ji
 */
@Log4j2
@Component
public class ApplicationExecuteStrategyUtil {

    public static final String APPLICATION_EXECUTE_STRATEGY_NAME = "application_execute_strategy_name";

    private static String applicationExecuteStrategyName;

    public ApplicationExecuteStrategyUtil() {
        applicationExecuteStrategyName = EnvUtil.getProperty(APPLICATION_EXECUTE_STRATEGY_NAME);
        if (StringUtils.isEmpty(applicationExecuteStrategyName)) {
            log.error("application_execute_strategy_name is empty, please check the config");
            throw new RuntimeException("application_execute_strategy_name is empty, please check the config");
        } else {
            log.info("application_execute_strategy_name is {}", applicationExecuteStrategyName);
        }
    }

    public boolean isCanExecuteCurStrategy(String curStrategyName) {
        return applicationExecuteStrategyName.equals(curStrategyName);
    }

    public String getApplicationExecuteStrategyName() {
        return applicationExecuteStrategyName;
    }
}
