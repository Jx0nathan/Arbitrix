package io.arbitrix.core.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jonathan.ji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadyExecuteContext {

    private Boolean canExecute;

    private List<String> cancelOrderIdList;

    private Integer createOrderLevel;

}
