package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zjj
 * @title: FinanceAddInitialBO
 * @description: 添加辅助核算初始余额
 * @date 2021/12/2614:43
 */
@Data
@ToString
@ApiModel("添加辅助核算初始余额")
public class FinanceAddInitialBO {

    @NotNull
    @ApiModelProperty("科目id")
    private Long subjectId;

    @NotNull
    @ApiModelProperty(value = "核算表id")
    private Long AdjuvantId;

    @NotEmpty
    @ApiModelProperty(value = "辅助核算类型ID")
    private List<Long> CarteIdList;
}
