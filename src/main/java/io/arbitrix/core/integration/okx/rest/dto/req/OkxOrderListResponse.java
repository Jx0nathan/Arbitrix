package io.arbitrix.core.integration.okx.rest.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class OkxOrderListResponse {
    private String code;
    private List<OkxOrderListResponseData> data;
}
