package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;


/**
 * <p>
 * 账套表
 * </p>
 *
 * @author dsc
 * @since 2021-08-28
 */
@Data
@ToString
@ApiModel("账套授权员工删除")
public class FinanceAccountSetBO {

    @ApiModelProperty(value = "账套id")
    private Long accountId;

    @ApiModelProperty(value = "员工id")
    private Long userId;

    @ApiModelProperty(value = "是否是创始人（0 不是  1 是）")
    private Integer isFounder;


}
