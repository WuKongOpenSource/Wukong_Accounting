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
 * 凭证详情关联标签类型表
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
@TableName("wk_finance_certificate_association")
@ApiModel(value = "FinanceCertificateAssociation对象", description = "凭证详情关联标签类型表")
public class FinanceCertificateAssociation implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "association_id", type = IdType.ASSIGN_ID)
    private Long associationId;

    @ApiModelProperty("凭证详情id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long detailId;

    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("关联模块id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long relationId;

    @ApiModelProperty("标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义")
    private Integer label;

    @ApiModelProperty("核算表id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adjuvantId;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "名称")
    private String name;

    @TableField(exist = false)
    @ApiModelProperty(value = "标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义")
    private String labelName;

    @TableField(exist = false)
    @ApiModelProperty(value = "编码")
    private String carteNumber;

    @TableField(exist = false)
    @ApiModelProperty(value = "规格（存货）")
    private String specification;

    @TableField(exist = false)
    @ApiModelProperty(value = "单位（存货）")
    private String unit;
}
