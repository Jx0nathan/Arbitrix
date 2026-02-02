package io.arbitrix.core.integration.bitget.wss;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.arbitrix.core.integration.bitget.ping.BitgetWSPinger;
import io.arbitrix.core.integration.bitget.util.SignatureUtils;
import io.arbitrix.core.integration.bitget.wss.dto.BookInfo;
import io.arbitrix.core.integration.bitget.wss.dto.req.SubscribeReq;
import io.arbitrix.core.integration.bitget.wss.dto.req.WsBaseReq;
import io.arbitrix.core.integration.bitget.wss.dto.req.WsLoginReq;
import io.arbitrix.core.integration.bitget.wss.dto.res.WsBaseRes;
import io.arbitrix.core.integration.bitget.wss.enums.WebSocketOp;
import io.arbitrix.core.integration.bitget.wss.listener.SubscriptionListener;
import io.arbitrix.core.common.util.JacksonUtil;

import java.time.Instant;
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
public class BitGetWebSocketConnection extends WebSocketListener {
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();
    private final Map<SubscribeReq, SubscriptionListener> scribeMap = new ConcurrentHashMap<>();
    private final Map<SubscribeReq, BookInfo> allBook = new ConcurrentHashMap<>();
    private final Set<SubscribeReq> allSuribe = Collections.synchronizedSet(new HashSet<>());
    private final Lock connectLock = new ReentrantLock();
    private final Lock loginLock = new ReentrantLock();
    private final Condition loginStatusCondition = loginLock.newCondition();
    private volatile boolean isLogin = false;
    // 重连状态,防止重复重连,用到的代码注释掉了,后面有问题可以再加
    private volatile boolean reconnectStatus = false;
    private final String pushUrl;
    private final Boolean needLogin;
    private final String apiKey;
    private final String secretKey;
    private final String passPhrase;
    private WebSocket webSocket;
    private final SubscriptionListener listener;
    private final SubscriptionListener errorListener;
    private BitgetWSPinger bitgetWSPinger;

    public BitGetWebSocketConnection(BitgetConnectionBuilder bitgetConnectionBuilder) {
        this.pushUrl = bitgetConnectionBuilder.getPushUrl();
        this.needLogin = bitgetConnectionBuilder.isLogin();
        this.listener = bitgetConnectionBuilder.getListener();
        this.errorListener = bitgetConnectionBuilder.getErrorListener();
        this.apiKey = bitgetConnectionBuilder.getApiKey();
        this.secretKey = bitgetConnectionBuilder.getSecretKey();
        this.passPhrase = bitgetConnectionBuilder.getPassPhrase();
        connect();
    }

    public static BitgetConnectionBuilder builder() {
        return new BitgetConnectionBuilder();
    }

    private void connect() {
        connectLock.lock();
        try {
            if (webSocket == null) {
                log.info("BitGetWebSocketConnection.connect started");
                Request request = new Request.Builder()
                        .url(this.pushUrl)
                        .build();
                webSocket = OK_HTTP_CLIENT.newWebSocket(request, this);
                if (this.needLogin) {
                    login();
                }
                log.info("BitGetWebSocketConnection.connect end");
            } else {
                log.info("BitGetWebSocketConnection.connect.webSocket has been connected");
            }
        } catch (Exception e) {
            reconnectStatus = false;
            log.error("BitGetWebSocketConnection.connect error", e);
        } finally {
            connectLock.unlock();
        }
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        log.info("BitGetWebSocketConnection.onOpen");
        reconnectStatus = false;
        if (bitgetWSPinger == null) {
            bitgetWSPinger = new BitgetWSPinger(this);
            bitgetWSPinger.start();
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String message) {
        try {
            if ("pong".equals(message)) {
                log.debug("BitGetWebSocketConnection.onMessage.pong");
                return;
            }
            WsBaseRes<SubscribeReq, Object> receivedMsg = JacksonUtil.from(message, new TypeReference<>() {
            });
            if (!receivedMsg.isSuccess()) {
                log.error("BitGetWebSocketConnection.onMessage.receive error msg:{}", message);
                if (Objects.nonNull(this.errorListener)) {
                    this.errorListener.onReceive(message);
                }
                return;
            }
            if (receivedMsg.isLogin()) {
                log.info("BitGetWebSocketConnection.onMessage.login msg:{}", message);
                loginLock.lock();
                try {
                    isLogin = true;
                    return;
                } finally {
                    loginStatusCondition.signalAll();
                    loginLock.unlock();
                }
            }
            if (receivedMsg.hasData()) {
                //check sum
                if (!checkSum(receivedMsg)) {
                    return;
                }
                if (receivedMsg.hasArgs() && Objects.nonNull(scribeMap.get(receivedMsg.getArgs()))) {
                    scribeMap.get(receivedMsg.getArgs()).onReceive(message);
                    return;
                }
                if (Objects.nonNull(this.listener)) {
                    this.listener.onReceive(message);
                    return;
                }
            }
            log.info("BitGetWebSocketConnection.onMessage receive op msg:{}", message);
        } catch (Exception e) {
            log.error("BitGetWebSocketConnection.onMessage receive error msg:{}", message, e);
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
        log.info("Connection is about to disconnect！");
        close();
        reConnect();
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        log.error("onFailure ", t);
        close();
        reConnect();
    }

    private void reConnect() {
        reconnectStatus = true;
        log.info("start reconnection ...");
        connect();
        if (CollectionUtils.isNotEmpty(allSuribe)) {
            subscribe(new ArrayList<>(allSuribe));
        }
    }


    private void close() {
        isLogin = false;
        webSocket.close(1000, "Long time no message was sent or received！");
        webSocket = null;
    }

    public void subscribe(List<SubscribeReq> channels) {
        allSuribe.addAll(channels);
        sendMessage(new WsBaseReq<>(WebSocketOp.SUBSCRIBE.getValue(), channels));
    }

    public void subscribe(List<SubscribeReq> channels, SubscriptionListener listener) {
        channels.forEach(channel -> {
            scribeMap.put(channel, listener);
        });
        subscribe(channels);
    }

    public void unsubscribe(List<SubscribeReq> channels) {
        channels.forEach(allSuribe::remove);
        channels.forEach(scribeMap::remove);
        sendMessage(new WsBaseReq<>(WebSocketOp.UNSUBSCRIBE.getValue(), channels));
    }

    public void sendMessage(String message) {
        log.info("start send message: {}", message);
        webSocket.send(message);
    }

    public void sendMessage(WsBaseReq<?> req) {
        String jsonMsg = JacksonUtil.toJsonStr(req);
        sendMessage(jsonMsg);
    }


    public void login() {
        Validate.notNull(this.apiKey, "apiKey is null");
        Validate.notNull(this.secretKey, "secretKey is null");
        Validate.notNull(this.passPhrase, "passphrase is null");

        WsLoginReq arg = buildArgs();
        sendMessage(new WsBaseReq<>(WebSocketOp.LOGIN.getValue(), Lists.newArrayList(arg)));
        //休眠1s，等待登录结果
        log.info("login in ......");
        while (!this.isLogin) {
            loginLock.lock();
            try {
                loginStatusCondition.await(10, TimeUnit.SECONDS);
                if (this.isLogin) {
                    break;
                }
                arg = buildArgs();
                sendMessage(new WsBaseReq<>(WebSocketOp.LOGIN.getValue(), Lists.newArrayList(arg)));
            } catch (Exception e) {
                log.error("login error", e);
            } finally {
                loginLock.unlock();
            }
        }
        log.info("login in ......end");
    }

    private WsLoginReq buildArgs() {
        String timestamp = Long.valueOf(Instant.now().getEpochSecond()).toString();
        String sign = SignatureUtils.wsGenerateSign(timestamp, this.secretKey);
        return WsLoginReq.builder()
                .apiKey(this.apiKey)
                .passphrase(this.passPhrase)
                .timestamp(timestamp)
                .sign(sign).build();
    }

    private boolean checkSum(WsBaseRes<SubscribeReq, Object> message) {
        try {
            if (!message.hasArgs() || Objects.isNull(message.getAction())) {
                return true;
            }
            SubscribeReq subscribeReq = message.getArgs();
            if (!subscribeReq.isBooksChannel()) {
                return true;
            }
            List<BookInfo> bookInfos = JacksonUtil.fromList(JacksonUtil.toJsonStr(message.getData()), BookInfo.class);
            if (CollectionUtils.isEmpty(bookInfos)) {
                return true;
            }
            BookInfo bookInfo = bookInfos.get(0);

            if (message.isSnapshotAction()) {
                allBook.put(subscribeReq, bookInfo);
                return true;
            }
            if (message.isUpdateAction()) {
                BookInfo all = allBook.get(subscribeReq);
                boolean checkNum = all.merge(bookInfo).checkSum(Integer.parseInt(bookInfo.getChecksum()), 25);
                if (!checkNum) {
                    ArrayList<SubscribeReq> subList = new ArrayList<>();
                    subList.add(subscribeReq);
                    this.subscribe(subList);
                }
                return checkNum;
            }
        } catch (Exception e) {
            log.error("checkSum error", e);
        }
        return true;
    }
}
