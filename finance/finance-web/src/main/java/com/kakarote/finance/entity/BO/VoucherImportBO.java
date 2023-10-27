package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author : zjj
 * @since : 2023/3/8
 */
@Getter
@Setter
public class VoucherImportBO {

    @ApiModelProperty(value = "行号")
    private Integer rowNumber;

    @ApiModelProperty(value = "日期")
    @NotEmpty(message = "不能为空")
    private Date certificateTime;

    @ApiModelProperty("凭证字")
    @NotEmpty(message = "不能为空")
    private String voucherName;

    @ApiModelProperty("凭证号")
    @NotEmpty(message = "不能为空")
    @Min(value = 0, message = "格式输入不正确")
    private Integer voucherNum;

    @ApiModelProperty(value = "附件数")
    private Integer fileNum;

    @ApiModelProperty("摘要")
    private String digestContent;

    @ApiModelProperty("科目编码")
    @NotEmpty(message = "不能为空")
    private String subjectNumber;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty(value = "借方金额")
    private BigDecimal debtorBalance;

    @ApiModelProperty(value = "贷方金额")
    private BigDecimal creditBalance;

    @ApiModelProperty(value = "客户")
    private String labelOne;

    @ApiModelProperty(value = "供应商")
    private String labelTwo;

    @ApiModelProperty(value = "职员")
    private String labelThree;

    @ApiModelProperty(value = "项目")
    private String labelFour;

    @ApiModelProperty(value = "部门")
    private String labelFive;

    @ApiModelProperty(value = "存货")
    private String labelSix;

    @ApiModelProperty(value = "数量")
    private Integer quality;

    @ApiModelProperty(value = "单价")
    private Integer price;

    @ApiModelProperty(value = "错误信息")
    private Set<String> errors = new HashSet<>();

    @ApiModelProperty(value = "源数据")
    private Map<String, Object> sourceMap;

    /**
     * 必填写一个的字段
     *
     * @return data
     */
    public static List<String> mustFillOnlyOne() {
        return Arrays.asList("debtorBalance", "creditBalance");
    }

    /**
     * 忽略的字段
     *
     * @param fieldName 字段名
     * @return bool
     */
    public static boolean ignoreFields(String fieldName) {
        return Arrays.asList("rowNumber", "errors", "sourceMap").contains(fieldName);
    }
}
