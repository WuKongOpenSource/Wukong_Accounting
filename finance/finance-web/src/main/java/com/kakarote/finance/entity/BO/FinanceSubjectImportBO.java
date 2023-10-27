package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : zjj
 * @since : 2023/3/9
 */
@Data
public class FinanceSubjectImportBO {

    @ApiModelProperty(value = "科目ID")
    private Long subjectId;

    @ApiModelProperty(value = "科目编码")
    private String number;

    @ApiModelProperty(value = "科目名称")
    private String subjectName;

    @ApiModelProperty(value = "方向")
    private Integer balanceDirection;

    @ApiModelProperty(value = "辅助核算 ID")
    private Long adjuvantId;

    @ApiModelProperty(value = "核算标签")
    private Integer label;

    @ApiModelProperty(value = "核算类别")
    private String labelName;

    @ApiModelProperty(value = "是否金额核算")
    private boolean isCash;
}
