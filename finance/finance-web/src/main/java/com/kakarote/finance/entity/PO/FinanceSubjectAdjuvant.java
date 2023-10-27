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
 * 科目和辅助核算关联表
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
@TableName("wk_finance_subject_adjuvant")
@ApiModel(value = "FinanceSubjectAdjuvant对象", description = "科目和辅助核算关联表")
public class FinanceSubjectAdjuvant implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "subject_adjuvant_id", type = IdType.ASSIGN_ID)
    private Long subjectAdjuvantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long adjuvantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义")
    private Integer label;

    @ApiModelProperty("标签名称")
    private String labelName;

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
