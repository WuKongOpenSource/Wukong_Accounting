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
 * 账套表
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
@TableName("wk_finance_account_set")
@ApiModel(value = "FinanceAccountSet对象", description = "账套表")
public class FinanceAccountSet implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("账套ID")
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "account_id", type = IdType.ASSIGN_ID)
    private Long accountId;

    @ApiModelProperty("公司编码")
    private String companyCode;

    @ApiModelProperty("公司名称")
    private String companyName;

    @ApiModelProperty("公司简介")
    private String companyProfile;

    @ApiModelProperty("所在行业")
    private String industry;

    @ApiModelProperty("所在地")
    private String location;

    @ApiModelProperty("法人代表")
    private String legalRepresentative;

    @ApiModelProperty("身份证号")
    private String idNum;

    @ApiModelProperty("营业执照号")
    private String businessLicenseNum;

    @ApiModelProperty("组织机构代码")
    private String organizationCode;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("联系人")
    private String contacts;

    @ApiModelProperty("办公电话")
    private String officeTelephone;

    @ApiModelProperty("手机号码")
    private String mobile;

    @ApiModelProperty("传真号码")
    private String faxNum;

    @ApiModelProperty("qq号码")
    private String qqNum;

    @ApiModelProperty("email")
    private String email;

    @ApiModelProperty("其他")
    private String other;

    @ApiModelProperty("详细地址")
    private String address;

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

    @ApiModelProperty("本位币id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long currencyId;

    @ApiModelProperty("启用期间")
    private LocalDateTime startTime;

    @ApiModelProperty("会计制度")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bookkeeperId;

    @ApiModelProperty("是否有账套（0 没有  1 有）")
    private Integer status;


}
