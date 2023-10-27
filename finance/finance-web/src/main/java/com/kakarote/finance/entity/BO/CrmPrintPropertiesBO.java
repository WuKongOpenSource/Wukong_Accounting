package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 打印预览参数BO
 *
 * @author zhangzhiwei
 */
@ApiModel("打印预览参数BO")
@Getter
@Setter
@ToString
public class CrmPrintPropertiesBO {

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty(value = "纸张类型", dataType = "A4")
    private String pageSize;

    @ApiModelProperty("左边距")
    private String marginLeft;

    @ApiModelProperty("右边距")
    private String marginRight;

    @ApiModelProperty("上边距")
    private String marginTop;

    @ApiModelProperty("下边距")
    private String marginBottom;

    @ApiModelProperty("页面高度")
    private String pageHeight;

    @ApiModelProperty("页面宽度")
    private String pageWidth;

    @ApiModelProperty("页眉文本")
    private String headerHtml;

    @ApiModelProperty("页脚")
    private String footerHtml;

    @ApiModelProperty("横竖向")
    private String orientation;

    public CrmPrintPropertiesBO() {
        this.pageSize = "A4";
        this.marginLeft = "31.8mm";
        this.marginRight = "31.8mm";
        this.marginTop = "25.4mm";
        this.marginBottom = "25.4mm";
        this.pageHeight = "297mm";
        this.pageWidth = "210mm";
        this.orientation = "Portrait";
    }
}
