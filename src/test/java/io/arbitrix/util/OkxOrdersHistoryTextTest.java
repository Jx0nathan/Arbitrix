package io.arbitrix.util;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxOrdersHistory;
import io.arbitrix.core.integration.okx.rest.OkxTradeClient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OkxOrdersHistoryTextTest {

    @Autowired
    private OkxTradeClient okxTradeClient;

    @Test
    @Ignore
    public void testOkxOrdersHistory() throws IOException {
        List<OkxOrdersHistory> dataList = new ArrayList<>();

        boolean hasMoreData = true;
        String lastOrderId = "";

        while (hasMoreData) {
            List<OkxOrdersHistory> okxOrdersHistoryList = okxTradeClient.getOrderHistory("SPOT", "ETH-USDT", "filled", "", lastOrderId, "1694667600000", "");
            if (okxOrdersHistoryList.isEmpty()) {
                hasMoreData = false;
                break;
            }

            for (OkxOrdersHistory okxOrdersHistory : okxOrdersHistoryList) {
                dataList.add(okxOrdersHistory);
                lastOrderId = okxOrdersHistory.getOrdId();
            }
        }


        try {
            writeListToTxtFile01(dataList, "order.txt");
            writeListToTxtFile02(dataList, "price.txt");
            writeListToTxtFile03(dataList, "size.txt");
            writeListToTxtFile04(dataList, "side.txt");
            writeListToTxtFile05(dataList, "fee.txt");
            writeListToTxtFile06(dataList, "time.txt");
            System.out.println("Data written to the file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing the data to the file: " + e.getMessage());
        }
    }

    public static void writeListToTxtFile01(List<OkxOrdersHistory> dataList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (OkxOrdersHistory obj : dataList) {
                // 将对象的每个字段拼接成一个字符串
                String line = obj.getOrdId();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void writeListToTxtFile02(List<OkxOrdersHistory> dataList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (OkxOrdersHistory obj : dataList) {
                String line = obj.getPx();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void writeListToTxtFile03(List<OkxOrdersHistory> dataList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (OkxOrdersHistory obj : dataList) {
                String line = obj.getSz();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void writeListToTxtFile04(List<OkxOrdersHistory> dataList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (OkxOrdersHistory obj : dataList) {
                String line = obj.getSide();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void writeListToTxtFile05(List<OkxOrdersHistory> dataList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (OkxOrdersHistory obj : dataList) {
                String line = obj.getFee();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void writeListToTxtFile06(List<OkxOrdersHistory> dataList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (OkxOrdersHistory obj : dataList) {
                Instant instant = Instant.ofEpochMilli(Long.parseLong(obj.getUpdateTime()));
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = dateTime.format(formatter);
                writer.write(formattedDateTime);
                writer.newLine();
            }
        }
    }

    public static void writeListToTxtFile11(List<OkxOrdersHistory> dataList, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (OkxOrdersHistory obj : dataList) {
                StringBuilder lineBuilder = new StringBuilder();
                // 将对象的每个字段拼接成一个字符串
                lineBuilder.append(obj.getOrdId()).append(",");
                lineBuilder.append(obj.getPx()).append(",");
                lineBuilder.append(obj.getSz()).append(",");
                lineBuilder.append(obj.getSide()).append(",");
                lineBuilder.append(obj.getFee()).append(",");

                Instant instant = Instant.ofEpochMilli(Long.parseLong(obj.getUpdateTime()));
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = dateTime.format(formatter);
                lineBuilder.append(formattedDateTime);
                // 添加其他字段...

                String line = lineBuilder.toString();
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
