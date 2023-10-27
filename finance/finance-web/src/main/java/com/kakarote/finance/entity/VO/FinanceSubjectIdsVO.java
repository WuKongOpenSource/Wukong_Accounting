package com.kakarote.finance.entity.VO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: FinanceSubjectIdsVO
 * @Author: Blue
 * @Description: FinanceSubjectIdsVO
 * @Date: 2021/8/27 20:14
 */
@Data
@ToString
@ApiModel("科目返回对象")
public class FinanceSubjectIdsVO {

    private List<Long> ids;

    @ApiModelProperty(value = "余额方向 1.借 2.贷")
    private Integer balanceDirection;

    @ApiModelProperty(value = "余额方向 1.借 2.贷")
    private Long subjectId;
}
