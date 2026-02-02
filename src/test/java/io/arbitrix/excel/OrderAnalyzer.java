package io.arbitrix.excel;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDateTime;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.http.Consts.UTF_8;

public class OrderAnalyzer {

    public static void main(String[] args) throws Exception {
        String inputFilePath = "/Users/user/Documents/arbitrix/src/test/java/io/arbitrix/excel/TradeHistory1430.xlsx";
        String outputFilePath = "/Users/user/Documents/arbitrix/src/test/java/io/arbitrix/excel/TradeHistoryConvert.xlsx";
        try (InputStream inputStream = new FileInputStream(inputFilePath);
             FileOutputStream outputStream = new FileOutputStream(outputFilePath);
             Workbook workbook = new XSSFWorkbook()) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputStream.readAllBytes());
            SyncHeaderReadListener syncReadListener = new SyncHeaderReadListener();
            EasyExcelFactory.read(byteArrayInputStream, syncReadListener).doReadAll();
            List<LinkedHashMap<Integer, String>> list = syncReadListener.getList();
            Sheet sheet = workbook.createSheet("Sheet1");
            AtomicInteger rowNum = new AtomicInteger();

            List<TradeInfo> tradeInfoList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                TradeInfo tradeInfo = new TradeInfo();
                for (Map.Entry<Integer, String> entry : list.get(i).entrySet()) {
                    if (entry.getKey() == 0) {
                        tradeInfo.setDirection(entry.getValue());
                    }
                    if (entry.getKey() == 1) {
                        tradeInfo.setFilledValue(entry.getValue());
                    }
                    if (entry.getKey() == 2) {
                        tradeInfo.setFilledPrice(entry.getValue());
                    }
                    if (entry.getKey() == 3) {
                        tradeInfo.setQuantity(entry.getValue());
                    }
                    if (entry.getKey() == 4) {
                        tradeInfo.setTimestamp(entry.getValue());
                    }
                }
                tradeInfoList.add(tradeInfo);
            }

            List<TradeInfo> newTradeInfoList = processTrade(tradeInfoList);
            getEventInSomeHours(newTradeInfoList);
            validateOrderStep(newTradeInfoList);

            System.out.println("ori.size is " + tradeInfoList.size() + " new.size is " + newTradeInfoList.size());
            for (int i = 0; i < newTradeInfoList.size(); i++) {
                Row currentRow = sheet.createRow(i);
                TradeInfo item = newTradeInfoList.get(i);
                for (int f = 0; f < 5; f++) {
                    if (f == 0) {
                        Cell cell = currentRow.createCell(0);
                        cell.setCellValue(item.getDirection());
                    }
                    if (f == 1) {
                        Cell cell = currentRow.createCell(1);
                        cell.setCellValue(item.getFilledValue());
                    }
                    if (f == 2) {
                        Cell cell = currentRow.createCell(2);
                        cell.setCellValue(item.getFilledPrice());
                    }
                    if (f == 3) {
                        Cell cell = currentRow.createCell(3);
                        cell.setCellValue(item.getQuantity());
                    }
                    if (f == 4) {
                        Cell cell = currentRow.createCell(4);
                        cell.setCellValue(item.getTimestamp());
                    }
                }
            }
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class SyncHeaderReadListener extends AnalysisEventListener<LinkedHashMap<Integer, String>> {
        private final List<LinkedHashMap<Integer, String>> list = new ArrayList<>();
        private final Map<Integer, String> headers = new HashMap<>();

        @Override
        public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext context) {
            list.add(data);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            headers.putAll(headMap);
        }

        public List<LinkedHashMap<Integer, String>> getList() {
            return list;
        }

        public Map<Integer, String> getHeaders() {
            return headers;
        }
    }

    private static List<TradeInfo> processTrade(List<TradeInfo> tradeInfoList) {
        List<TradeInfo> newTradeInfoList = new ArrayList<>();
        boolean skip = false;
        for (int i = 0; i < tradeInfoList.size(); i++) {
            if (skip) {
                skip = false;
                continue;
            }

            if (i == tradeInfoList.size() - 1) {
                newTradeInfoList.add(tradeInfoList.get(i));
            } else {
                BigDecimal prePrice = new BigDecimal(tradeInfoList.get(i).getFilledPrice());
                BigDecimal nextPrice = new BigDecimal(tradeInfoList.get(i + 1).getFilledPrice());
                if (prePrice.compareTo(nextPrice) != 0) {
                    newTradeInfoList.add(tradeInfoList.get(i));
                    skip = false;
                } else {
                    TradeInfo tradeInfo = new TradeInfo();
                    tradeInfo.setDirection(tradeInfoList.get(i).getDirection());
                    tradeInfo.setTimestamp(tradeInfoList.get(i).getTimestamp());
                    tradeInfo.setFilledPrice(tradeInfoList.get(i).getFilledPrice());
                    tradeInfo.setFilledValue(new BigDecimal(tradeInfoList.get(i).getFilledValue()).add(new BigDecimal(tradeInfoList.get(i + 1).getFilledValue())).toString());
                    tradeInfo.setQuantity(new BigDecimal(tradeInfoList.get(i).getQuantity()).add(new BigDecimal(tradeInfoList.get(i + 1).getQuantity())).toString());
                    newTradeInfoList.add(tradeInfo);
                    skip = true;
                }
            }
        }
        return newTradeInfoList;
    }

    private static void validateOrderStep(List<TradeInfo> tradeInfoList) {
        for (int i = 0; i < tradeInfoList.size(); i++) {
            if (i == tradeInfoList.size() - 1) {
                continue;
            }
            BigDecimal prePrice = new BigDecimal(tradeInfoList.get(i).getFilledPrice());
            BigDecimal nextPrice = new BigDecimal(tradeInfoList.get(i + 1).getFilledPrice());
            System.out.println("price step " + prePrice.subtract(nextPrice).abs() + " prePrice " + prePrice + " nextPrice " + nextPrice);
        }
    }

    private static void getEventInSomeHours(List<TradeInfo> tradeInfoList) {
        Map<String, List<TradeInfo>> eventMap = new HashMap<>();
        for (TradeInfo tradeInfo : tradeInfoList) {
            int hour = covertTime(tradeInfo.getTimestamp());
            if (eventMap.get(String.valueOf(hour)) == null) {
                List<TradeInfo> list = new ArrayList<>();
                list.add(tradeInfo);
                eventMap.put(String.valueOf(hour), list);
            } else {
                eventMap.get(String.valueOf(hour)).add(tradeInfo);
            }
        }

        eventMap.forEach((k, v) -> {
            System.out.println("hour is " + k + " size is " + v.size());
        });
    }

    public static int covertTime(String timeString) {
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-dd H:mm");

        // 将时间字符串解析为 LocalDateTime 对象
        LocalDateTime dateTime = LocalDateTime.parse(timeString, formatter);

        // 获取当前小时数
        return dateTime.getHour();
    }
}
