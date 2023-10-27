package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: FinanceSubjectUpdateBO
 * @Author: Blue
 * @Description: FinanceSubjectUpdateBO
 * @Date: 2021/8/26 18:43
 */
@Data
@ToString
@ApiModel("修改科目对象")
public class FinanceSubjectUpdateBO {

    @ApiModelProperty(value = "科目id")
    private List<Long> ids;

    @ApiModelProperty(value = "科目状态")
    private Integer status;
}
