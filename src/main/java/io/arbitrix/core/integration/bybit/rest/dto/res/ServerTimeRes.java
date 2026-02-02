package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerTimeRes {

    private String timeSecond;
    private String timeNano;

    public String milliSecondStr() {
        if (StringUtils.isEmpty(timeNano)) {
            return null;
        }
        // sub millisecond string
        return timeNano.substring(0, 13);
    }
}
