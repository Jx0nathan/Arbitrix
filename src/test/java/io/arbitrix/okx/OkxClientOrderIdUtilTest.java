package io.arbitrix.okx;

import org.junit.jupiter.api.Test;
import io.arbitrix.core.integration.okx.utils.OkxClientOrderIdUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OkxClientOrderIdUtilTest {
    @Test
    public void clientOrderIdConvertTest(){
        String originalUuid = UUID.randomUUID().toString();
        String uuidWithoutDash = originalUuid.replace("-", "");
        String okxOrderId = OkxClientOrderIdUtil.convert2OKX(originalUuid);
        String arbitrixOrderId = OkxClientOrderIdUtil.convertToArbitrix(okxOrderId);
        assertEquals(originalUuid, arbitrixOrderId);
        assertEquals(uuidWithoutDash, okxOrderId);
    }
}
