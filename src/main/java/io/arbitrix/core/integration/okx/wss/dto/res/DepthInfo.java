package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import io.arbitrix.core.common.monitor.event.MonitorableEvent;
import io.arbitrix.core.integration.okx.wss.dto.req.WssArg;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
public class DepthInfo implements MonitorableEvent {

    private WssArg arg;

    private String action;

    private List<DataItem> data;

    @Override
    public String eventTime() {
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }
        return data.get(0).getTs();
    }

    @Override
    public String topic() {
        return arg.getChannel();
    }

    /**
     * asks和bids值数组举例说明： ["411.8", "10", "0", "4"]
     * - 411.8为深度价格
     * - 10为此价格的数量 （合约交易为合约，现货/币币杠杆为交易币的数量
     * - 0该字段已弃用(始终为0)
     * - 4为此价格的订单数量
     */
    @Data
    public static class DataItem {
        private List<List<String>> asks;
        private List<List<String>> bids;
        private String ts;
        private long seqId;
    }
}
