package io.arbitrix.core.integration.bitget.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsBaseReq<T> {

    private String op;

    private List<T> args;
}