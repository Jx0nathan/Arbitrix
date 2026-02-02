package io.arbitrix.core.common.monitor.httpclient;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.httpcomponents.PoolingHttpClientConnectionManagerMetricsBinder;
import lombok.extern.log4j.Log4j2;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author mcx
 * @date 2023/10/31
 * @description
 */
@Log4j2
public class HttpClientPoolMetrics {
    @Autowired(required = false)
    private HttpClientConnectionManager connectionManager;

    public HttpClientPoolMetrics() {
    }

    @PostConstruct
    public void init() {
        if (Objects.isNull(connectionManager)) {
            log.warn("HttpClientConnectionManager is null, HttpClientConnectionManager metrics will not be collected");
            return;
        }
        if (!(connectionManager instanceof PoolingHttpClientConnectionManager)) {
            log.warn("HttpClientConnectionManager is not PoolingHttpClientConnectionManager, HttpClientConnectionManager metrics will not be collected");
            return;
        }
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = (PoolingHttpClientConnectionManager) connectionManager;
        new PoolingHttpClientConnectionManagerMetricsBinder(poolingHttpClientConnectionManager, "httpclient.pool").bindTo(Metrics.globalRegistry);
    }
}
