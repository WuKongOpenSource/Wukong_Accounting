package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : zjj
 * @since : 2023/3/9
 */
@Data
public class FinanceAdjuvantCarteImportBO {

    @ApiModelProperty("核算 Id")
    private Long adjuvantId;

    @ApiModelProperty(value = "核算标签")
    private Integer label;

    @ApiModelProperty(value = "核算项 ID")
    private Long carteId;

    @ApiModelProperty(value = "名称")
    private String carteName;

    @ApiModelProperty(value = "编码")
    private String carteNumber;

    @ApiModelProperty(value = "类别")
    private String adjuvantName;
}
