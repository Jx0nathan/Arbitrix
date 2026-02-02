package io.arbitrix.okx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.arbitrix.core.integration.okx.config.OkxProperties;
import io.arbitrix.core.integration.okx.rest.dto.req.OkxApiInfo;
import io.arbitrix.core.integration.okx.rest.UriAndHeaderEncoder;

import java.util.Arrays;
import java.util.List;

public class UriAndHeaderEncoderTest {
    private UriAndHeaderEncoder uriAndHeaderEncoder;

    @BeforeEach
    public void setUp() {
        OkxProperties okxProperties = new OkxProperties();
        uriAndHeaderEncoder = new UriAndHeaderEncoder(okxProperties);
    }

    @Test
    public void randomOkxApiInfoTest() {
        OkxApiInfo item1 = new OkxApiInfo("1", "1", "1", 1);
        OkxApiInfo item2 = new OkxApiInfo("2", "2", "2", 2);
        OkxApiInfo item3 = new OkxApiInfo("3", "3", "3", 3);
        List<OkxApiInfo> okxApiInfoList = Arrays.asList(item1, item2, item3);

        OkxApiInfo result1 = uriAndHeaderEncoder.randomOkxApiInfo("0", okxApiInfoList);
        Assertions.assertEquals(result1.getApiKey(), "1");

        OkxApiInfo result2 = uriAndHeaderEncoder.randomOkxApiInfo("1", okxApiInfoList);
        Assertions.assertEquals(result2.getApiKey(), "2");

        OkxApiInfo result3 = uriAndHeaderEncoder.randomOkxApiInfo("2", okxApiInfoList);
        Assertions.assertEquals(result3.getApiKey(), "3");

        OkxApiInfo result4 = uriAndHeaderEncoder.randomOkxApiInfo("3", okxApiInfoList);
        Assertions.assertEquals(result4.getApiKey(), "1");

        OkxApiInfo result5 = uriAndHeaderEncoder.randomOkxApiInfo("4", okxApiInfoList);
        Assertions.assertEquals(result5.getApiKey(), "2");

        OkxApiInfo result6 = uriAndHeaderEncoder.randomOkxApiInfo("5", okxApiInfoList);
        Assertions.assertEquals(result6.getApiKey(), "3");

        OkxApiInfo result7 = uriAndHeaderEncoder.randomOkxApiInfo("ae70fbeb28a34f24ab419ddbd844d958", okxApiInfoList);
        Assertions.assertEquals(result7.getApiKey(), "3");
    }
}
