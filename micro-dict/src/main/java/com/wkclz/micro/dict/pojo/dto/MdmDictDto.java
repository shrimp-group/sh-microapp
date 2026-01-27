package com.wkclz.micro.dict.pojo.dto;

import com.wkclz.micro.dict.pojo.entity.MdmDict;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict (字典) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmDictDto extends MdmDict {

    private List<String> dictTypes;
    private List<MdmDictItem> items;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmDictDto copy(MdmDict source) {
        MdmDictDto target = new MdmDictDto();
        MdmDict.copy(source, target);
        return target;
    }
}

