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
 * 凭证模板
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
@TableName("wk_finance_template")
@ApiModel(value = "FinanceTemplate对象", description = "凭证模板")
public class FinanceTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    private Long templateId;

    @ApiModelProperty("凭证模板名称")
    private String name;

    @ApiModelProperty("凭证类别")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long typeId;

    @ApiModelProperty("凭证模板内容")
    private String content;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

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
