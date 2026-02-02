package io.arbitrix.core.integration.okx.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {
    private String op;
    private List<SubscribeArg> args;
}
