package io.arbitrix.core.integration.bitget.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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

    /**
     * signature algorithm
     *
     * @param timestamp
     * @param method
     * @param requestPath
     * @param queryString
     * @param body
     * @param secretKey
     * @return java.lang.String
     * @description ACCESS-SIGN of The request header is correct timestamp + method + requestPath
     * + "?" + queryString + body The string (+indicates string connection) is encrypted using the HMAC SHA256 method and output through BASE64 encoding。
     */
    public static String generate(String timestamp, String method, String requestPath,
                                  String queryString, String body, String secretKey)
            throws CloneNotSupportedException, InvalidKeyException, UnsupportedEncodingException {

        method = method.toUpperCase();
        body = StringUtils.defaultIfBlank(body, StringUtils.EMPTY);
        queryString = StringUtils.isBlank(queryString) ? StringUtils.EMPTY : "?" + queryString;

        String preHash = timestamp + method + requestPath + queryString + body;
        log.info(preHash);
        return doSign(secretKey, preHash);
    }


    /**
     * Ws signature
     *
     * @param timestamp
     * @param secretKey
     * @return
     */
    public static String wsGenerateSign(String timestamp, String secretKey) {
        String hash = "";
        try {
            String preHash = timestamp + "GET" + "/user/verify";
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
        return Base64.getEncoder().encodeToString(mac.doFinal(preHash.getBytes(SignatureUtils.CHARSET)));
    }
}
