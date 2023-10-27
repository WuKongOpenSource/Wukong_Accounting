package com.kakarote.finance.entity.VO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.kakarote.core.feign.admin.entity.FileEntity;
import com.kakarote.finance.entity.PO.FinanceCertificateDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: FinanceCertificateVO
 * @Author: Blue
 * @Description: FinanceCertificateVO
 * @Date: 2021/8/25 16:04
 */
@Data
@ToString
@ApiModel("凭证获取对象")
public class FinanceCertificateVO {

    @ApiModelProperty(value = "凭证Id")
    private Long certificateId;

    @ApiModelProperty(value = "凭证字")
    private Long voucherId;

    @ApiModelProperty(value = "凭证号")
    private String certificateNum;

    @ApiModelProperty(value = "凭证字")
    private String voucherName;

    @ApiModelProperty(value = "凭证号码")
    private String voucherNum;

    @ApiModelProperty(value = "凭证日期")
    private Date certificateTime;

    @ApiModelProperty(value = "批次 比如附件批次")
    private String batchId;

    @ApiModelProperty(value = "附单据张数")
    private Integer fileNum;

    @ApiModelProperty(value = "合计借方金额")
    private BigDecimal debtorBalance;

    @ApiModelProperty(value = "合计贷方金额")
    private BigDecimal creditBalance;

    @ApiModelProperty(value = "合计")
    private String total;

    @ApiModelProperty(value = "制单人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUserId;

    @ApiModelProperty(value = "制单人")
    private String createUserName;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "0待审核、1通过、2拒绝、3审核中 4:撤回 5 未提交 6 创建 7 已删除 8 作废")
    private Integer checkStatus;

    @ApiModelProperty(value = "审核人")
    private Long examineUserId;

    @ApiModelProperty(value = "审核人")
    private String examineUserName;

    @ApiModelProperty(value = "结账类型 1.普通结账 2.结账损益 3.转出未交增值税 4.计提地税 5.计提所得税")
    private Integer statementType;

    @ApiModelProperty(value = "是否结账 1是 0否")
    private Integer isStatement;

    @ApiModelProperty(value = "凭证详情")
    private List<FinanceCertificateDetail> certificateDetails;

    private List<FileEntity> fileEntityList;
}
