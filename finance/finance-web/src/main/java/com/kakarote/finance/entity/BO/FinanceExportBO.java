package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author dsc
 * @title: FinanceAddInitialBO
 * @description: 导出多栏账用
 * @date 2021/12/2614:43
 */
@Data
@ToString
@ApiModel("导出多栏账用")
public class FinanceExportBO {

    @ApiModelProperty(value = "数据")
    private List<Map<String, Object>> dataList;

    @ApiModelProperty(value = "表头")
    private List<String> headList;

    @ApiModelProperty(value = "子表头")
    private List<Map<String, List<String>>> childHeadList;

    @ApiModelProperty(value = "科目id")
    private Long subjectId;

    @ApiModelProperty(value = "科目类型")
    private String subjectType;
}
