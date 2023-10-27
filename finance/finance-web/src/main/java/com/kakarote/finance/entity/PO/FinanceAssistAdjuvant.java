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
 * 辅助核算关联模块表
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
@TableName("wk_finance_assist_adjuvant")
@ApiModel(value = "FinanceAssistAdjuvant对象", description = "辅助核算关联模块表")
public class FinanceAssistAdjuvant implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "assist_adjuvant_id", type = IdType.ASSIGN_ID)
    private Long assistAdjuvantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assistId;

    @ApiModelProperty("标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义")
    private Integer label;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("关联模块id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long relationId;

    private String labelName;

    @ApiModelProperty("核算表id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adjuvantId;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
