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
@ApiModel("创建新账套")
public class FinanceNewAccountSetBO {

    @ApiModelProperty(value = "账套id")
    private Long accountId;

    @ApiModelProperty(value = "本位币")
    private Long currencyId;

    @ApiModelProperty(value = "启用期间")
    private String startTime;

    @ApiModelProperty(value = "会计制度id")
    private Long bookkeeperId;


}
