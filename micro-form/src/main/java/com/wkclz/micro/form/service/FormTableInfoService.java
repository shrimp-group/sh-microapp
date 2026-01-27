package com.wkclz.micro.form.service;

import com.wkclz.mybatis.bean.ColumnQuery;
import com.wkclz.mybatis.bean.DataTypeEnum;
import com.wkclz.mybatis.service.TableInfoService;
import com.wkclz.tool.utils.StringUtil;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormTableInfoService {

    @Autowired
    private TableInfoService tableInfoService;

    public List<ColumnQuery> getColumnInfos() {
        ColumnQuery query = new ColumnQuery();
        List<ColumnQuery> infos = tableInfoService.getColumnInfos4Options(query);
        // 转换
        for (ColumnQuery c : infos) {
            c.setColumnName(StringUtil.underlineToCamel(c.getColumnName()));
            String dataType = c.getDataType().toUpperCase();
            if (EnumUtils.isValidEnum(DataTypeEnum.class, dataType)) {
                DataTypeEnum e = DataTypeEnum.valueOf(dataType);
                c.setInputType(e.getInputType());
                c.setJavaType(e.getJavaType());
                c.setTsType(e.getTsType());
                c.setInputType(e.getInputType());
            }
        }
        return infos;
    }

}
