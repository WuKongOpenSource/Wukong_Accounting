package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@ApiModel("科目余额查询请求")
@EqualsAndHashCode(callSuper = false)
public class FinanceBalanceRequestBO extends FinanceReportRequestBO {

    @ApiModelProperty(value = "科目ID")
    private List<Long> subjectIds;
}
