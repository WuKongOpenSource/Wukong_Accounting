package com.kakarote.finance.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.common.enums.SystemCodeEnum;
import com.kakarote.core.exception.CrmException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ExcelImportUtils
 * @Author: Blue
 * @Description: 导入字段处理工具
 * @Date: 2021/9/2 13:48
 */
public class ExcelImportUtils {

    public static Map<String, Integer> queryMapByList(List<JSONObject> jsonObjects, List<Object> rowList, Map<String, Integer> map) {
        for (int i = 0; i < rowList.size(); i++) {
            Object object = rowList.get(i);
            for (JSONObject json : jsonObjects) {
                if (json.getInteger("isNull") == 1) {
                    if (StrUtil.equals("*" + json.getString("name"), object.toString())) {
                        map.put(json.getString("name"), i);
                    }
                } else {
                    if (StrUtil.equals(json.getString("name"), object.toString())) {
                        map.put(json.getString("name"), i);
                    }
                }
            }
        }
        return map;
    }

    public static JSONObject queryField(String fieldName, String formType, Integer type, String name, Integer isNull) {
        JSONObject json = new JSONObject();
        json.fluentPut("fieldName", fieldName)
                .fluentPut("formType", formType)
                .fluentPut("type", type)
                .fluentPut("name", name).fluentPut("isNull", isNull);
        return json;
    }

    public static String getFilePath(MultipartFile file) {
        String dirPath = FileUtil.getTmpDirPath();
        try {
            InputStream inputStream = file.getInputStream();
            File fromStream = FileUtil.writeFromStream(inputStream, dirPath + "/" + IdUtil.simpleUUID() + file.getOriginalFilename());
            return fromStream.getAbsolutePath();
        } catch (IOException e) {
            throw new CrmException(SystemCodeEnum.SYSTEM_UPLOAD_FILE_ERROR);
        }
    }
}
