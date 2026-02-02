package io.arbitrix.core.integration.bybit.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.integration.bybit.wss.enums.WebSocketOp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WSAdminRes {
    @JsonProperty("ret_msg")
    private String retMsg;
    private String op;
    @JsonProperty("conn_id")
    private String connId;
    private Boolean success;
    public boolean isAdminMsg() {
        if (this.op != null) {
            return true;
        }
        return false;
    }

    public boolean isAuth() {
        if (WebSocketOp.AUTH.getValue().equals(this.op)) {
            return true;
        }
        return false;
    }

    public boolean isPingPong() {
        if (WebSocketOp.PING.getValue().equals(this.op) || WebSocketOp.PONG.getValue().equals(this.op)) {
            return true;
        }
        return false;
    }

    public boolean isSubscribe() {
        if (WebSocketOp.SUBSCRIBE.getValue().equals(this.op)) {
            return true;
        }
        return false;
    }
}