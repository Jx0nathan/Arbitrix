package io.arbitrix.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeInfo {
    private String direction;
    private String filledValue;
    private String filledPrice;
    private String quantity;
    private String timestamp;
}
