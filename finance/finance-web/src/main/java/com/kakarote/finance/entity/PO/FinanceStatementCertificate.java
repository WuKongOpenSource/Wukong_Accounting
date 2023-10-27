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
 * 结账和凭证关联表
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
@TableName("wk_finance_statement_certificate")
@ApiModel(value = "FinanceStatementCertificate对象", description = "结账和凭证关联表")
public class FinanceStatementCertificate implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "statement_certificate_id", type = IdType.ASSIGN_ID)
    private Long statementCertificateId;

    @ApiModelProperty("结账id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long statementId;

    @ApiModelProperty("凭证id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certificateId;

    @ApiModelProperty("生成凭证时间")
    private LocalDateTime certificateTime;

    @ApiModelProperty("结账状态 1.未结账 2.已结账")
    private Integer status;

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
