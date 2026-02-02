package io.arbitrix.core.integration.bybit.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.common.monitor.event.MonitorableEvent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WSStreamBaseRes<T> implements MonitorableEvent {
    private String id;
    private String topic;
    private String type;
    @JsonAlias("creationTime")
    private String ts;
    private T data;

    public boolean isStreamData() {
        if (this.topic != null) {
            return true;
        }
        return false;
    }

    public boolean hasData() {
        if (this.data != null) {
            return true;
        }
        return false;
    }

    @Override
    public String eventTime() {
        return ts;
    }

    @Override
    public String topic() {
        return topic;
    }
}