package com.kakarote.finance.entity.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 凭证详情
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
@TableName("wk_finance_certificate_detail")
@ApiModel(value = "FinanceCertificateDetail对象", description = "凭证详情")
public class FinanceCertificateDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "detail_id", type = IdType.ASSIGN_ID)
    private Long detailId;

    @ApiModelProperty("摘要内容")
    private String digestContent;

    @ApiModelProperty("会计科目内容")
    private String subjectContent;

    @ApiModelProperty("数量")
    private String quantity;

    @ApiModelProperty("借方金额")
    private BigDecimal debtorBalance;

    @ApiModelProperty("贷方金额")
    private BigDecimal creditBalance;

    @ApiModelProperty("凭证id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certificateId;

    @ApiModelProperty("科目编码")
    private String subjectNumber;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("科目id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("单价")
    private BigDecimal price;

    @ApiModelProperty("财务初始余额id（辅助核算需要）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assistId;

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

    @TableField(exist = false)
    @ApiModelProperty(value = "关联类型")
    private List<FinanceCertificateAssociation> associationBOS;

    @TableField(exist = false)
    @ApiModelProperty(value = "批次")
    private String batchId;

    @TableField(exist = false)
    @ApiModelProperty(value = "科目名称")
    private String subjectName;

}
