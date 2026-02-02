package io.arbitrix.core.integration.okx.rest.dto.req;

import lombok.Data;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
public class OkxApiDetail {

    private List<OkxApiInfo> okxApiInfoList;

}
