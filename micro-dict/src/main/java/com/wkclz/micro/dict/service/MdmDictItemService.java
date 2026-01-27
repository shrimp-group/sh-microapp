package com.wkclz.micro.dict.service;

import cn.hutool.core.comparator.CompareUtil;
import com.wkclz.core.base.DbColumnEntity;
import com.wkclz.micro.dict.dao.MdmDictItemMapper;
import com.wkclz.micro.dict.pojo.dto.MdmDictDto;
import com.wkclz.micro.dict.pojo.dto.MdmDictItemDto;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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


    public Integer dictItemSave(MdmDictDto dto) {
        List<MdmDictItem> newItems = dto.getItems();

        // 历史所有  items
        MdmDictItemDto param = new MdmDictItemDto();
        param.setDictType(dto.getDictType());
        List<MdmDictItem> oldItems = selectByEntity(param);

        List<MdmDictItem> insers;
        List<MdmDictItem> updates = new ArrayList<>();
        List<MdmDictItem> deletes;

        // 新增的 items
        List<String> oldValues = oldItems.stream().map(MdmDictItem::getDictValue).toList();
        insers = newItems.stream().filter(item -> !oldValues.contains(item.getDictValue())).toList();

        // 删除的 items
        List<String> newValues = newItems.stream().map(MdmDictItem::getDictValue).toList();
        deletes = oldItems.stream().filter(item -> !newValues.contains(item.getDictValue())).toList();

        // 修改的 items
        for (MdmDictItem newItem : newItems) {
            for (MdmDictItem oldItem : oldItems) {
                if (newItem.getDictValue().equals(oldItem.getDictValue())) {
                    boolean u = false;
                    if (CompareUtil.compare(newItem.getDictLabel(), oldItem.getDictLabel()) != 0) {
                        oldItem.setDictLabel(newItem.getDictLabel());
                        u = true;
                    }
                    if (CompareUtil.compare(newItem.getDescription(), oldItem.getDescription()) != 0) {
                        oldItem.setDescription(newItem.getDescription());
                        u = true;
                    }
                    if (CompareUtil.compare(newItem.getElType(), oldItem.getElType()) != 0) {
                        oldItem.setElType(newItem.getElType());
                        u = true;
                    }
                    if (CompareUtil.compare(newItem.getEnableFlag(), oldItem.getEnableFlag()) != 0) {
                        oldItem.setEnableFlag(newItem.getEnableFlag());
                        u = true;
                    }
                    if (CompareUtil.compare(newItem.getSort(), oldItem.getSort()) != 0) {
                        oldItem.setSort(newItem.getSort());
                        u = true;
                    }
                    if (u) {
                        updates.add(oldItem);
                    }
                    break;
                }
            }
        }


        if (!org.springframework.util.CollectionUtils.isEmpty(insers)) {
            insertBatch(insers);
        }
        if (!org.springframework.util.CollectionUtils.isEmpty(updates)) {
            for (MdmDictItem update : updates) {
                updateById(update);
            }
        }
        if (!org.springframework.util.CollectionUtils.isEmpty(deletes)) {
            List<Long> ids = deletes.stream().map(DbColumnEntity::getId).toList();
            MdmDictItem delParam = new MdmDictItem();
            delParam.setIds(ids);
            deleteByIds(delParam);
        }

        return insers.size() + updates.size() + deletes.size();
    }

}

