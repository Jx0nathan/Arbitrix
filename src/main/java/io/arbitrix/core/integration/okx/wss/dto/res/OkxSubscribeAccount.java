package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.integration.okx.wss.dto.req.WssAccountPrivateArgInfo;

import java.util.List;

@NoArgsConstructor
@Data
public class OkxSubscribeAccount {


    private WssAccountPrivateArgInfo arg;
    private List<OkxBalanceData> data;
}
