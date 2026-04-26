package com.wkclz.micro.dict.service;

import com.wkclz.core.base.DbColumnEntity;
import com.wkclz.micro.dict.bean.dto.MdmDictDto;
import com.wkclz.micro.dict.bean.dto.MdmDictItemDto;
import com.wkclz.micro.dict.bean.entity.MdmDictItem;
import com.wkclz.micro.dict.mapper.MdmDictItemMapper;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict_item (字典内容) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_dict_item 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmDictItemService extends BaseService<MdmDictItem, MdmDictItemMapper> {


    public List<MdmDictItem> getDictItemList(String dictType) {
        return mapper.getDictItemList(dictType);
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


    @Transactional(rollbackFor = Exception.class)
    public Integer dictItemSave(MdmDictDto dto) {
        List<MdmDictItem> newItems = dto.getItems();

        // 历史所有  items
        MdmDictItem param = new MdmDictItem();
        param.setDictType(dto.getDictType());
        List<MdmDictItem> oldItems = selectByEntity(param);

        Set<String> oldValues = oldItems.stream().map(MdmDictItem::getDictValue).collect(Collectors.toSet());
        Set<String> newValues = newItems.stream().map(MdmDictItem::getDictValue).collect(Collectors.toSet());

        List<MdmDictItem> inserts = newItems.stream()
                .filter(item -> !oldValues.contains(item.getDictValue()))
                .collect(Collectors.toList());

        List<MdmDictItem> deletes = oldItems.stream()
                .filter(item -> !newValues.contains(item.getDictValue()))
                .collect(Collectors.toList());

        Map<String, MdmDictItem> oldItemMap = oldItems.stream()
                .collect(Collectors.toMap(MdmDictItem::getDictValue, i -> i, (a, b) -> a));

        List<MdmDictItem> updates = new ArrayList<>();
        for (MdmDictItem newItem : newItems) {
            MdmDictItem oldItem = oldItemMap.get(newItem.getDictValue());
            if (oldItem == null) {
                continue;
            }
            boolean changed = false;
            if (!Objects.equals(newItem.getDictLabel(), oldItem.getDictLabel())) {
                oldItem.setDictLabel(newItem.getDictLabel());
                changed = true;
            }
            if (!Objects.equals(newItem.getDescription(), oldItem.getDescription())) {
                oldItem.setDescription(newItem.getDescription());
                changed = true;
            }
            if (!Objects.equals(newItem.getElType(), oldItem.getElType())) {
                oldItem.setElType(newItem.getElType());
                changed = true;
            }
            if (!Objects.equals(newItem.getEnableFlag(), oldItem.getEnableFlag())) {
                oldItem.setEnableFlag(newItem.getEnableFlag());
                changed = true;
            }
            if (!Objects.equals(newItem.getSort(), oldItem.getSort())) {
                oldItem.setSort(newItem.getSort());
                changed = true;
            }
            if (changed) {
                updates.add(oldItem);
            }
        }

        if (!CollectionUtils.isEmpty(inserts)) {
            insertBatch(inserts);
        }
        if (!CollectionUtils.isEmpty(updates)) {
            for (MdmDictItem update : updates) {
                updateByIdSelective(update);
            }
        }
        if (!CollectionUtils.isEmpty(deletes)) {
            List<Long> ids = deletes.stream().map(DbColumnEntity::getId).toList();
            MdmDictItem delParam = new MdmDictItem();
            delParam.setIds(ids);
            deleteByIds(delParam);
        }

        return inserts.size() + updates.size() + deletes.size();
    }

}
