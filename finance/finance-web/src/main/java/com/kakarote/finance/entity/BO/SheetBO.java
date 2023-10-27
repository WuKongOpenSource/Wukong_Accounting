package com.kakarote.finance.entity.BO;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : zjj
 * @since : 2023/3/7
 */
@Getter
@Setter
public class SheetBO {

    private int index;

    /**
     * sheet 名
     */
    private String name;

    /**
     * 自定义标题批注 map
     */
    private LinkedHashMap<String, String> headerNameComment;

    /**
     * 数据
     */
    private List<Map<String, Object>> rows;

    /**
     * 表头默认值
     */
    private Map<String, String[]> headerNameDefaultValue;
}
