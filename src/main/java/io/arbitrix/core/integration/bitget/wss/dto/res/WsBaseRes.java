package io.arbitrix.core.integration.bitget.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @param <A> args
 * @param <D> data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsBaseRes<A, D> {

    private String code;
    @JsonAlias("message")
    private String msg;
    private String event;
    private String action;
    @JsonAlias("arg")
    private A args;
    private D data;
    public boolean isSuccess() {
        return StringUtils.isEmpty(code) || "0".equals(code);
    }
    public boolean isLogin() {
        return "login".equals(event);
    }
    public boolean hasData() {
        return data != null;
    }

    public boolean hasArgs() {
        return args != null;
    }
    public boolean isSnapshotAction() {
        return "snapshot".equalsIgnoreCase(action);
    }
    public boolean isUpdateAction() {
        return "update".equalsIgnoreCase(action);
    }
}