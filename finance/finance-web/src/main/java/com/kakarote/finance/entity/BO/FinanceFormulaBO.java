package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author zjj
 * @description: 计算公式BO
 * @date 2021/8/26 18:19
 */
@Data
@ToString
@ApiModel("计算公式BO")
public class FinanceFormulaBO {

    @ApiModelProperty(value = "科目ID")
    private String subjectId;

    @ApiModelProperty(value = "科目名称")
    private String subjectName;

    @ApiModelProperty(value = "科目标号")
    private String subjectNumber;

    @ApiModelProperty(value = "行")
    private Integer line;

    @ApiModelProperty(value = "运算符")
    private String operator;

    @ApiModelProperty(value = "取数规则")
    private Integer rules;

    @ApiModelProperty(value = "月值")
    private BigDecimal monthValue;

    @ApiModelProperty(value = "上月值")
    private BigDecimal lastMonthValue;

    @ApiModelProperty(value = "年值")
    private BigDecimal yearValue;

    @ApiModelProperty("年初")
    private BigDecimal initialPeriod;

    @ApiModelProperty("期末")
    private BigDecimal endPeriod;

    @ApiModelProperty("期初")
    private BigDecimal startPeriod;
}
