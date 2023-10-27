package com.kakarote.finance.entity.BO;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author : zjj
 * @since : 2023/3/10
 */
@Data
@TableName(autoResultMap = true)
public class CertificateWithVoucherBO {

    @ApiModelProperty(value = "日期")
    private Date certificateTime;

    @ApiModelProperty("凭证字")
    private String voucherName;

    @ApiModelProperty("凭证号")
    private Integer voucherNum;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;
}
