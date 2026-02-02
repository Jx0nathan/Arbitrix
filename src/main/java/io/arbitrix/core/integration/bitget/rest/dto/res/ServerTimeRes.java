package io.arbitrix.core.integration.bitget.rest.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerTimeRes implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Server time
     */
    private String serverTime;
}
