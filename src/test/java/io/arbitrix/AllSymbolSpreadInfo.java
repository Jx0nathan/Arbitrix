package io.arbitrix;

import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import io.arbitrix.core.common.util.JacksonUtil;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import io.arbitrix.core.common.event.BookTickerEvent;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AllSymbolSpreadInfo {
    private static final String symbols = "USDTUSD,BTCUSDT,ETHUSDT,BCHUSDT,LTCUSDT,BNBUSDT,ADAUSDT,BATUSDT,ETCUSDT,XLMUSDT,ZRXUSDT,DOGEUSDT,ATOMUSDT,NEOUSDT,VETUSDT,QTUMUSDT,ONTUSDT,KNCUSDT,VTHOUSDT,COMPUSDT,MKRUSDT";
    private static final String symbols2 = "ONEUSDT,STORJUSDT,BANDUSDT,UNIUSDT,SOLUSDT,EGLDUSDT,PAXGUSDT,AVAXUSDT,CTSIUSDT,ROSEUSDT,CELOUSDT,KDAUSDT,KSMUSDT,ACHUSDT,DARUSDT,FLOKIUSDT,LOOMUSDT,STMXUSDT,OXTUSDT,ZENUSDT";
    private static final String symbols3 = "FILUSDT,AAVEUSDT,GRTUSDT,SHIBUSDT,CRVUSDT,AXSUSDT,DOTUSDT,YFIUSDT,1INCHUSDT,FTMUSDT,USDCUSDT,MATICUSDT,MANAUSDT,ALGOUSDT,LINKUSDT,EOSUSDT,ZECUSDT,ENJUSDT,NEARUSDT,OMGUSDT,SUSHIUSDT";
    private static final String symbols4 = "LRCUSDT,NMRUSDT,SLPUSDT,ANTUSDT,CHZUSDT,OGNUSDT,GALAUSDT,TLMUSDT,SNXUSDT,AUDIOUSDT,ENSUSDT,LPTUSDT,REQUSDT,APEUSDT,FLUXUSDT,COTIUSDT,VOXELUSDT,RLCUSDT,BICOUSDT,API3USDT,BNTUSDT,IMXUSDT,FLOWUSDT,GTCUSDT,THETAUSDT,TFUELUSDT,OCEANUSDT,LAZIOUSDT,SANTOSUSDT,ALPINEUSDT,PORTOUSDT,RENUSDT,CELRUSDT,SKLUSDT,VITEUSDT,WAXPUSDT";
    private static final String symbols5 = "LTOUSDT,FETUSDT,BONDUSDT,LOKAUSDT,ICPUSDT,TUSDT,OPUSDT,MXCUSDT,JAMUSDT,TRACUSDT,PROMUSDT,DIAUSDT,ARBUSDT,RNDRUSDT,SYSUSDT,RADUSDT,ILVUSDT,LDOUSDT,RAREUSDT,LSKUSDT,DGBUSDT,REEFUSDT,ALICEUSDT,FORTHUSDT,ASTRUSDT,BTRSTUSDT,GALUSDT,SANDUSDT,BALUSDT,GLMUSDT,CLVUSDT,TUSDUSDT,QNTUSDT,STGUSDT,AXLUSDT,KAVAUSDT,APTUSDT,MASKUSDT";
    private static final String symbols6 = "BOSONUSDT,PONDUSDT,POLYXUSDT,IOSTUSDT,XECUSDT,BLURUSDT,ETHBTC,BNBBTC,LTCBTC,BCHBTC,XTZBTC,ADABTC,LINKBTC,VETBTC,UNIBTC,SOLBTC,LRCBTC,MATICBTC,DOTBTC,MANABTC,ATOMBTC,AVAXBTC,WBTCBTC,DOGEBTC,BTCBUSD,BNBBUSD,ETHBUSD";
    private static final String symbols7 = "HBARBUSD,ONEBUSD,BTCUSDC,ETHUSDC,SOLUSDC,ADAUSDC,BTCDAI,ETHDAI,ADAETH,BTCUSD,ETHUSD,BCHUSD,LTCUSD,USDTUSD,BNBUSD,ADAUSD,BATUSD,ETCUSD,XLMUSD,ZRXUSD,LINKUSD,RVNUSD,DASHUSD,ZECUSD,ALGOUSD,IOTAUSD,WAVESUSD,ATOMUSD,NEOUSD,QTUMUSD,ICXUSD,ENJUSD,ONTUSD,ZILUSD,VETUSD";
    private static final String symbols8 = "XTZUSD,HBARUSD,OMGUSD,MATICUSD,EOSUSD,DOGEUSD,KNCUSD,VTHOUSD,USDCUSD,COMPUSD,MANAUSD,MKRUSD,DAIUSD,ONEUSD,BANDUSD,STORJUSD,UNIUSD,SOLUSD,EGLDUSD,PAXGUSD,OXTUSD,ZENUSD,FILUSD,AAVEUSD,GRTUSD,SUSHIUSD,ANKRUSD,CRVUSD,AXSUSD,AVAXUSD,CTSIUSD,DOTUSD,YFIUSD";
    private static final String symbols9 = "1INCHUSD,FTMUSD,NEARUSD,LRCUSD,LPTUSD,NMRUSD,SLPUSD,ANTUSD,XNOUSD,CHZUSD,OGNUSD,GALAUSD,TLMUSD,SNXUSD,AUDIOUSD,REQUSD,APEUSD";

    private static volatile Map<String, Queue<BigDecimal>> symbolQueueMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        process(symbolQueueMap, symbols);
        process(symbolQueueMap, symbols2);
        process(symbolQueueMap, symbols2);
        process(symbolQueueMap, symbols3);
        process(symbolQueueMap, symbols4);
        process(symbolQueueMap, symbols5);
        process(symbolQueueMap, symbols6);
        process(symbolQueueMap, symbols7);
        process(symbolQueueMap, symbols8);
        process(symbolQueueMap, symbols9);

        Thread.sleep(1000L * 60 * 20);
        Map<String, BigDecimal> processMap = new ConcurrentHashMap<>();
        symbolQueueMap.forEach((k, v) -> {
            BigDecimal sum = v.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(new BigDecimal(v.size()), 8, BigDecimal.ROUND_HALF_UP);
            processMap.put(k, avg);
        });

        // 获取 ConcurrentHashMap 的键集合
        Set<String> keySet = processMap.keySet();

        // 将键集合转换为列表，方便排序
        List<String> keyList = new ArrayList<>(keySet);

        // 对键列表进行排序
        Collections.sort(keyList);

        // 遍历排序后的键列表，输出对应的值
        for (String key : keyList) {
            BigDecimal value = processMap.get(key);
            System.out.println(key + ":" + value);
        }
    }

    public static void process(Map<String, Queue<BigDecimal>> symbolQueueMap, String symbolStr) {
        Runnable runnable = () -> {
            WebSocketStreamClientImpl client = new WebSocketStreamClientImpl("wss://stream.binance.com:9443");
            String[] symbolArr = symbolStr.split(",");
            for (String symbol : symbolArr) {
                client.bookTicker(symbol.trim(), response -> {
                    try {
                        BookTickerEvent message = JacksonUtil.toObj(response, BookTickerEvent.class);
                        calculateSpread(symbol.trim(), message.getBidPrice(), message.getAskPrice(), symbolQueueMap);
                    } catch (Exception ex) {
                        System.out.printf("Exception: %s%n", ex.getMessage());
                    }
                });
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private static void calculateSpread(String symbol, String bidPrice, String askPrice, Map<String, Queue<BigDecimal>> symbolQueueMap) {
        BigDecimal spread = new BigDecimal(askPrice).subtract(new BigDecimal(bidPrice));
        Queue<BigDecimal> queue = symbolQueueMap.get(symbol.trim());
        if (queue == null) {
            queue = new CircularFifoQueue<>(10000);
            queue.add(spread);
            symbolQueueMap.put(symbol.trim(), queue);
        } else {
            queue.add(spread);
        }
    }
}
