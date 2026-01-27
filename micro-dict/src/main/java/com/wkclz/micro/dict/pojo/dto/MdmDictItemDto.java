package com.wkclz.micro.dict.pojo.dto;

import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict_item (字典内容) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmDictItemDto extends MdmDictItem {


    private List<String> dictTypes;
    private String typeDescription;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmDictItemDto copy(MdmDictItem source) {
        MdmDictItemDto target = new MdmDictItemDto();
        MdmDictItem.copy(source, target);
        return target;
    }
}

