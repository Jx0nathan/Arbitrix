package io.arbitrix.core.integration.okx.utils;

public class OkxClientOrderIdUtil {

    public static String convert2OKX(String uuid) {
        return uuid.replace("-", "");
    }
    public static String convertToArbitrix(String uuidWithoutDash) {
        return String.format("%s-%s-%s-%s-%s", uuidWithoutDash.substring(0, 8), uuidWithoutDash.substring(8, 12), uuidWithoutDash.substring(12, 16), uuidWithoutDash.substring(16, 20), uuidWithoutDash.substring(20));
    }
}
