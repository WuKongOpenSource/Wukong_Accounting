package com.kakarote.finance.entity.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 辅助核算关联类型表
 * </p>
 *
 * @author dsc
 * @since 2021-12-22
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("wk_finance_adjuvant_carte")
@ApiModel(value = "FinanceAdjuvantCarte对象", description = "辅助核算关联类型表")
public class FinanceAdjuvantCarte implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "carte_id", type = IdType.ASSIGN_ID)
    private Long carteId;

    @ApiModelProperty("编码")
    private String carteNumber;

    @ApiModelProperty("名称")
    private String carteName;

    @ApiModelProperty("核算表id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adjuvantId;

    @ApiModelProperty("状态 1.正常启用 2.正常禁用 3.删除")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("规格（存货）")
    private String specification;

    @ApiModelProperty("单位（存货）")
    private String unit;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
