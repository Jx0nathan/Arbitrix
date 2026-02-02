package io.arbitrix.core.integration.bybit.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.monitor.rest.MonitorableResponse;
import io.arbitrix.core.integration.bybit.rest.enums.ResponseCode;

import java.io.Serializable;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseRestRes<T> implements Serializable, MonitorableResponse {

    private static final long serialVersionUID = -1L;
    private Boolean success;

    /**
     * Response 0 means success, greater than 0 means failure, less than 0 means system reservation
     */
    private Long retCode;

    /**
     * Prompt information
     */
    private String retMsg;

    /**
     * Current timestamp (ms)
     */
    private Long time;
    /**
     * Return Data
     */
    private T result;
    private Object retExtInfo;

    public boolean isSuccess() {
        boolean retCodeSuccess = Objects.nonNull(retCode) && retCode == 0;
        boolean success = Objects.nonNull(this.success) ? this.success : false;
        return retCodeSuccess || success;
    }

    public boolean isOrderNotExist() {
        return ResponseCode.ORDER_NOT_EXIST.getCode().equals(String.valueOf(retCode));
    }

    public boolean isInsufficientBalance() {
        return ResponseCode.INSUFFICIENT_BALANCE.getCode().equals(String.valueOf(retCode));
    }

    @Override
    public String serverTime() {
        if (Objects.isNull(time)) {
            return null;
        }
        return String.valueOf(time);
    }

    @Override
    public String exchangeName() {
        return ExchangeNameEnum.BYBIT.name();
    }
}
