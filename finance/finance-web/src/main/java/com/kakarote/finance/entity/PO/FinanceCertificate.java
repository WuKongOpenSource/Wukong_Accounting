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

/**
 * <p>
 * 凭证表
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
@TableName("wk_finance_certificate")
@ApiModel(value = "FinanceCertificate对象", description = "凭证表")
public class FinanceCertificate implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "certificate_id", type = IdType.ASSIGN_ID)
    private Long certificateId;

    @ApiModelProperty("凭证字")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long voucherId;

    @ApiModelProperty("凭证号")
    private Integer certificateNum;

    @ApiModelProperty("凭证日期")
    private LocalDateTime certificateTime;

    @ApiModelProperty("批次 比如附件批次")
    private String batchId;

    @ApiModelProperty("附单据张数")
    private Integer fileNum;

    @ApiModelProperty("合计借方金额")
    private BigDecimal debtorBalance;

    @ApiModelProperty("合计贷方金额")
    private BigDecimal creditBalance;

    @ApiModelProperty("合计")
    private String total;

    @ApiModelProperty("制单人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("0待审核、1通过、2拒绝、3审核中 4:撤回 5 未提交 6 创建 7 已删除 8 作废")
    private Integer checkStatus;

    @ApiModelProperty("审核人id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long examineUserId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
