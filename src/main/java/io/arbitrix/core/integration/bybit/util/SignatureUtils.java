package io.arbitrix.core.integration.bybit.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Log4j2
public class SignatureUtils {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String CHARSET = "UTF-8";
    private static Mac MAC;

    static {
        try {
            SignatureUtils.MAC = Mac.getInstance(SignatureUtils.HMAC_SHA256);
        } catch (NoSuchAlgorithmException var1) {
            throw new RuntimeErrorException(new Error("Can't get Mac's instance."));
        }
    }

    public static String generate(String timestamp, String apiKey, String secretKey, String recvWindow,
                                  String queryString, String body)
            throws CloneNotSupportedException, InvalidKeyException, UnsupportedEncodingException {
        body = StringUtils.defaultIfBlank(body, StringUtils.EMPTY);
        queryString = StringUtils.isBlank(queryString) ? StringUtils.EMPTY : queryString;

        String preHash = timestamp + apiKey + recvWindow + queryString + body;
        log.info(preHash);
        return doSign(secretKey, preHash);
    }

    public static String wsGenerateSign(String timestamp, String secretKey) {
        String hash = "";
        try {
            String preHash = "GET/realtime" + timestamp;
            hash = doSign(secretKey, preHash);
        } catch (Exception e) {
            throw new RuntimeException("wsGenerateSign error", e);
        }
        return hash;
    }

    private static String doSign(String secretKey, String preHash) throws UnsupportedEncodingException, CloneNotSupportedException, InvalidKeyException {
        byte[] secretKeyBytes = secretKey.getBytes(SignatureUtils.CHARSET);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, SignatureUtils.HMAC_SHA256);
        Mac mac = (Mac) SignatureUtils.MAC.clone();
        mac.init(secretKeySpec);
        return byteArrayToHexString(mac.doFinal(preHash.getBytes(SignatureUtils.CHARSET)));
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }
}
