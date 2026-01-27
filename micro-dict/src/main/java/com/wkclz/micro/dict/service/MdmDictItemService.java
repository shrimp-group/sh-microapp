package com.wkclz.micro.dict.service;

import com.wkclz.micro.dict.dao.MdmDictItemMapper;
import com.wkclz.micro.dict.pojo.dto.MdmDictItemDto;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict_item (字典内容) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_dict_item 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmDictItemService extends BaseService<MdmDictItem, MdmDictItemMapper> {

    public List<MdmDictItemDto> getDictItemList(MdmDictItem model) {
        return mapper.getDictItemList(model);
    }

    public List<MdmDictItemDto> getAllDictItem() {
        return mapper.getAllDictItem();
    }

    public List<MdmDictItem> getDictItemsByDictTypes(List<String> dictTypes) {
        if (CollectionUtils.isEmpty(dictTypes)) {
            return Collections.emptyList();
        }
        return mapper.getDictItemsByDictTypes(dictTypes);
    }


}

