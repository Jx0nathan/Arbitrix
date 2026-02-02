package io.arbitrix.core.integration.bybit.wss;

import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.arbitrix.core.common.enums.ExchangeNameEnum;
import io.arbitrix.core.common.enums.WSStreamType;
import io.arbitrix.core.common.monitor.WSSMonitor;
import io.arbitrix.core.integration.bybit.config.BybitProperties;
import io.arbitrix.core.integration.bybit.ping.BybitWSPinger;
import io.arbitrix.core.integration.bybit.util.SignatureUtils;
import io.arbitrix.core.integration.bybit.wss.dto.req.WsBaseReq;
import io.arbitrix.core.integration.bybit.wss.dto.res.WSAdminRes;
import io.arbitrix.core.integration.bybit.wss.dto.res.WSStreamBaseRes;
import io.arbitrix.core.integration.bybit.wss.enums.WebSocketOp;
import io.arbitrix.core.integration.bybit.wss.listener.WSSMessageListener;
import io.arbitrix.core.utils.SystemClock;
import io.arbitrix.core.common.util.JacksonUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mcx
 * @date 2023/9/25
 * @description
 */
@Log4j2
public class BybitWebSocketConnection extends WebSocketListener {
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();
    private final Map<String, WSSMessageListener> scribeListenerMap = new ConcurrentHashMap<>();
    private final Set<String> allSubscribeReq = Collections.synchronizedSet(new HashSet<>());
    private final Lock connectLock = new ReentrantLock();
    private final Lock loginLock = new ReentrantLock();
    private final Condition loginStatusCondition = loginLock.newCondition();
    private volatile boolean islogin = false;
    // 重连状态,防止重复重连,用到的代码注释掉了,后面有问题可以再加
    private volatile boolean reconnectStatus = false;
    private volatile WebSocket webSocket;
    private final BybitProperties config;
    // 这里单独定义一个baseUrl是因为可能创建多个BybitWebSocketConnection(private,public), config有两个地址,无法区分
    private final String baseUrl;
    private final boolean needAuth;
    private final WSSMessageListener listener;
    private final WSSMessageListener errorListener;
    private final WSStreamType wsStreamType;
    private BybitWSPinger pinger;

    public BybitWebSocketConnection(BybitProperties config,
                                    String baseUrl,
                                    boolean needAuth,
                                    WSSMessageListener listener,
                                    WSSMessageListener errorListener,
                                    WSStreamType wsStreamType) {
        this.config = config;
        this.baseUrl = baseUrl;
        this.needAuth = needAuth;
        this.listener = listener;
        this.errorListener = errorListener;
        this.wsStreamType = wsStreamType;
        connect();
    }

    public static BybitConnectionBuilder builder() {
        return new BybitConnectionBuilder();
    }

    private void connect() {
        connectLock.lock();
        try {
            if (webSocket == null) {
                log.info("BybitWebSocketConnection.connect.webSocket start");
                Request request = new Request.Builder()
                        .url(this.baseUrl)
                        .build();
                webSocket = OK_HTTP_CLIENT.newWebSocket(request, this);
                if (needAuth) {
                    authentication();
                }
                log.info("BybitWebSocketConnection.connect.webSocket end");
            } else {
                log.info("BybitWebSocketConnection.connect.webSocket has been connected");
            }
        } catch (Exception e) {
            reconnectStatus = false;
            log.error("BybitWebSocketConnection.connect error", e);
        } finally {
            connectLock.unlock();
        }
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        log.info("BybitWebSocketConnection.onOpen");
        reconnectStatus = false;
        if (pinger == null) {
            pinger = new BybitWSPinger(this);
            pinger.start();
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String message) {
        long receiveTime = SystemClock.now();
        try {
            WSAdminRes adminresMsg = JacksonUtil.from(message, WSAdminRes.class);
            if (adminresMsg.isAuth()) {
                log.info("BybitWebSocketConnection.onMessage receive auth msg:{}", message);
                loginLock.lock();
                try {
                    if (adminresMsg.getSuccess()) {
                        islogin = true;
                    } else {
                        islogin = false;
                    }
                    return;
                } finally {
                    loginStatusCondition.signalAll();
                    loginLock.unlock();
                }
            }
            if (adminresMsg.isPingPong()) {
                log.debug("BybitWebSocketConnection.onMessage receive ping-pong msg:{}", message);
                return;
            }
            if (adminresMsg.isSubscribe()) {
                if (adminresMsg.getSuccess()) {
                    log.info("BybitWebSocketConnection.onMessage receive subscribe success msg:{}", message);
                } else {
                    log.error("BybitWebSocketConnection.onMessage receive subscribe fail msg:{}", message);
                }
                return;
            }
            if (!adminresMsg.isAdminMsg()) {
                WSStreamBaseRes streamMsg = JacksonUtil.from(message, WSStreamBaseRes.class);
                WSSMonitor.recordDelay(ExchangeNameEnum.BYBIT.name(), this.wsStreamType,streamMsg, receiveTime);
                if (streamMsg.hasData() && Objects.nonNull(scribeListenerMap.get(streamMsg.getTopic()))) {
                    scribeListenerMap.get(streamMsg.getTopic()).onReceive(message);
                    return;
                }
                if (Objects.nonNull(this.listener)) {
                    this.listener.onReceive(message);
                    return;
                }
            }
            log.info("BybitWebSocketConnection.onMessage receive unknow msg:{}", message);
        } catch (Exception e) {
            log.error("BybitWebSocketConnection.onMessage receive error msg:{}", message, e);
            if (Objects.nonNull(this.errorListener)) {
                this.errorListener.onReceive(message);
            }
        }
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.info("Connection dropped！" + reason);
        close();
        reConnect();
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.info("BybitWebSocketConnection.onClosing");
        close();
        reConnect();
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        log.error("BybitWebSocketConnection.onFailure ", t);
        close();
        reConnect();
    }

    private void reConnect() {
        reconnectStatus = true;
        log.info("BybitWebSocketConnection.reconnection start  ...");
        connect();
        if (CollectionUtils.isNotEmpty(allSubscribeReq)) {
            subscribe(new ArrayList<>(allSubscribeReq));
        }
        log.info("BybitWebSocketConnection.reconnection end  ...");
    }


    private void close() {
        islogin = false;
        webSocket.close(1000, "BybitWebSocketConnection.websocket Long time no message was sent or received！");
        webSocket = null;
    }

    public void subscribe(List<String> channels) {
        allSubscribeReq.addAll(channels);
        sendMessage(new WsBaseReq<>(WebSocketOp.SUBSCRIBE.getValue(), channels));
    }

    public void subscribe(List<String> channels, WSSMessageListener listener) {
        channels.forEach(channel -> {
            scribeListenerMap.put(channel, listener);
        });
        subscribe(channels);
    }

    public void unsubscribe(List<String> channels) {
        channels.forEach(allSubscribeReq::remove);
        channels.forEach(scribeListenerMap::remove);
        sendMessage(new WsBaseReq<>(WebSocketOp.UNSUBSCRIBE.getValue(), channels));
    }

    public void sendMessage(String message) {
        log.info("BybitWebSocketConnection.start send message: {}", message);
        webSocket.send(message);
    }

    public void sendMessage(WsBaseReq<?> req) {
        String jsonMsg = JacksonUtil.toJsonStr(req);
        sendMessage(jsonMsg);
    }


    private void authentication() {
        Validate.notNull(this.config.getApiKey(), "apiKey is null");
        Validate.notNull(this.config.getSecretKey(), "secretKey is null");

        WsBaseReq<String> authReq = authArgs();
        sendMessage(authReq);
        //休眠1s，等待登录结果
        log.info("BybitWebSocketConnection.login in ......");
        while (!this.islogin) {
            loginLock.lock();
            try {
                loginStatusCondition.await(10, TimeUnit.SECONDS);
                if (this.islogin) {
                    break;
                }
                authReq = authArgs();
                sendMessage(authReq);
            } catch (Exception e) {
                log.error("BybitWebSocketConnection.login error", e);
            } finally {
                loginLock.unlock();
            }
        }
        log.info("BybitWebSocketConnection.login in ......end");
    }

    private WsBaseReq<String> authArgs() {
        String timestamp = String.valueOf(Instant.now().plus(10, ChronoUnit.SECONDS).toEpochMilli());
        String sign = SignatureUtils.wsGenerateSign(timestamp, this.config.getSecretKey());
        return WsBaseReq.auth(this.config.getApiKey(), timestamp, sign);
    }
}
