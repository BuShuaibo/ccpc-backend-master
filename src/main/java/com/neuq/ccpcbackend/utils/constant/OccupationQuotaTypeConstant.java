package com.neuq.ccpcbackend.utils.constant;

import lombok.Data;

import java.util.Map;

/**
 * 占用名额类型常量类
 */
@Data
public class OccupationQuotaTypeConstant {
    private OccupationQuotaTypeConstant() {
        // 私有构造方法防止实例化
    }

    public static final Integer NORMAL = 0;    // 普通名额
    public static final Integer FEMALE = 1;    // 女队名额
    public static final Integer WILDCARD = 2;  // 外卡名额
    public static final Integer STAR = 3;      // 打星名额

    private static final Map<String, String> ID_NAME_MAP = Map.of(
            NORMAL.toString(), "普通名额",
            FEMALE.toString(), "女队名额",
            WILDCARD.toString(), "外卡名额",
            STAR.toString(), "打星名额"
    );
    
    public static String getTypeById(String id){
        return ID_NAME_MAP.get(id);
    }
}
