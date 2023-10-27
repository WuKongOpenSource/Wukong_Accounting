package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:修改报表配置BO
 * @author: zjj
 * @date: 2021-08-31 17:35
 */
@Data
@ToString
@ApiModel("修改报表配置BO")
public class FinanceUpdateReportConfigBO {

    @ApiModelProperty(value = "公式")
    private List<FinanceFormulaBO> formulaBOList = new ArrayList<>();

    @ApiModelProperty(value = "配置ID")
    private Long id;

    @ApiModelProperty(value = "行次")
    private Integer sort;
}
