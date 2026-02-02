package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.integration.okx.wss.dto.req.WssArg;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketTickerInfo {

    private WssArg arg;

    private List<MarketTickerInfoDetail> data;

}
