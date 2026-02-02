package io.arbitrix.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jonathan.ji
 */
@Getter
@AllArgsConstructor
public enum OrderLevel {

    FIRST_LEVEL(1),

    SECOND_LEVEL(2),

    THIRD_LEVEL(3),

    FOURTH_LEVEL(4),

    FIFTH_LEVEL(5);

    private final int level;

    /**
     * 根据level获取OrderLevel
     */
    public static OrderLevel getOrderLevelByLevel(int level) {
        for (OrderLevel orderLevel : OrderLevel.values()) {
            if (orderLevel.getLevel() == level) {
                return orderLevel;
            }
        }
        return null;
    }

    public static List<OrderLevel> warpOrderLevelList(List<String> levelList) {
        List<OrderLevel> orderLevelList = new ArrayList<>();
        for(String item : levelList){
            OrderLevel orderLevel = getOrderLevelByLevel(Integer.parseInt(item));
            if(orderLevel != null){
                orderLevelList.add(orderLevel);
            }
        }
        return orderLevelList;
    }
}
