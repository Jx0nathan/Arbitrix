package io.arbitrix.core.integration.bitget.wss.dto;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import io.arbitrix.core.common.util.JacksonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

@Data
@Log4j2
public class BookInfo {
    private List<String[]> asks;
    private List<String[]> bids;
    private String checksum;
    private String ts;


    public BookInfo() {

    }

    public BookInfo merge(BookInfo updateInfo) {

        this.asks = merge(this.asks, updateInfo.getAsks(), false);
        log.info("asks sort uniq:{}", JacksonUtil.toJsonStr(this.asks));
        this.bids = merge(this.bids, updateInfo.getBids(), true);
        log.info("bids sort uniq:{}", JacksonUtil.toJsonStr(this.bids));
        return this;
    }

    //isReverse: true->desc,false->asc
    private List<String[]> merge(List<String[]> allList, List<String[]> updateList, boolean isReverse) {

        Map<String, String[]> priceAndValue = allList.stream().collect(Collectors.toMap(o -> o[0], o -> o));


        for (String[] update : updateList) {

            if (new BigDecimal(update[1]).compareTo(BigDecimal.ZERO) == 0) {
                priceAndValue.remove(update[0]);
                continue;
            }
            priceAndValue.put(update[0], update);

        }

        List<String[]> newAllList = new ArrayList<>(priceAndValue.values());

        if (isReverse) {
            newAllList.sort((o1, o2) -> new BigDecimal(o2[0]).compareTo(new BigDecimal(o1[0])));
        } else {
            newAllList.sort(Comparator.comparing(o -> new BigDecimal(o[0])));
        }

        return newAllList;
    }

    public <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    public boolean checkSum(int checkSum, int gear) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gear; i++) {
            if (i < this.getBids().size()) {
                String[] bids = this.getBids().get(i);
                sb.append(bids[0]).append(":").append(bids[1]).append(":");
            }

            if (i < this.getAsks().size()) {
                String[] asks = this.getAsks().get(i);
                sb.append(asks[0]).append(":").append(asks[1]).append(":");
            }
        }

        String s = sb.toString();
        String str = s.substring(0, s.length() - 1);


        CRC32 crc32 = new CRC32();
        crc32.update(str.getBytes());

        int value = (int) crc32.getValue();

        log.info("check val:{}", str);
        log.info("start checknum mergeVal: {},checkVal:{}", value, checkSum);
        return value == checkSum;

    }
}