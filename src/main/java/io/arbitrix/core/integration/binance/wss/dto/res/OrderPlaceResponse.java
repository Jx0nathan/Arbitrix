package io.arbitrix.core.integration.binance.wss.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jonathan.ji
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlaceResponse {

    private String id;

    private int status;

    private OrderPlaceResult result;

}
