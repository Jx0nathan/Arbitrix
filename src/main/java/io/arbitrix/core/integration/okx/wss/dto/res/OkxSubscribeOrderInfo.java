package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OkxSubscribeOrderInfo {
    private Arg arg;
    private List<OkxSubscribeOrderInfoDetail> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Arg {
        private String channel;
        private String instType;
        private String instId;
        private String uid;
    }
}
