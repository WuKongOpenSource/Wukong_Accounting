package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : zjj
 * @since : 2023/10/13
 */
@Data
@ApiModel("科目余额表查询 BO")
public class FinanceSubjectBalanceQueryBO extends FinanceDetailAccountBO{

    @ApiModelProperty(value = "会计期间开始时间")
    private String startTime;

    @ApiModelProperty(value = "会计期间结束时间")
    private String endTime;

    @ApiModelProperty(value = "科目id")
    private Long subjectId;

    @ApiModelProperty(value = "开始科目id")
    private Long startSubjectId;

    @ApiModelProperty(value = "结束科目id")
    private Long endSubjectId;

    @ApiModelProperty(value = "小科目级次")
    private Integer minLevel;

    @ApiModelProperty(value = "大科目级次")
    private Integer maxLevel;
}
