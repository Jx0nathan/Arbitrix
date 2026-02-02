package io.arbitrix.core.integration.bitget.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.integration.bitget.rest.enums.ResponseCode;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseRes<T> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Response 00000 means success, greater than 0 means failure, less than 0 means system reservation
     */
    private String code;

    /**
     * Prompt information
     */
    private String msg;

    /**
     * system time
     */
    private Long requestTime;
    /**
     * Return Data
     */
    private T data;

    public boolean isSuccess() {
        return ResponseCode.SUCCESS.getCode().equals(code);
    }

    public boolean isInsufficientBalance() {
        return ResponseCode.INSUFFICIENT_BALANCE.getCode().equals(code);
    }

    public boolean isOrderNotExist() {
        return ResponseCode.ORDER_NOT_EXIST.getCode().equals(code);
    }
}
