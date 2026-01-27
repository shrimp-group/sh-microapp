package com.wkclz.micro.dict.service;

import com.wkclz.core.base.PageData;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.dict.dao.MdmDictItemMapper;
import com.wkclz.micro.dict.dao.MdmDictMapper;
import com.wkclz.micro.dict.pojo.dto.MdmDictDto;
import com.wkclz.micro.dict.pojo.dto.MdmDictItemDto;
import com.wkclz.micro.dict.pojo.entity.MdmDict;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict (字典) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_dict 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmDictService extends BaseService<MdmDict, MdmDictMapper> {

    @Autowired
    private MdmDictItemMapper dictItemMapper;

    public PageData<MdmDict> getDictList(MdmDict model) {
        return PageQuery.page(model, mapper::getDictList);
    }


    public List<MdmDictDto> copy(MdmDictDto dto) {
        MdmDictDto param = new MdmDictDto();
        MdmDictItemDto itemParam = new MdmDictItemDto();
        List<String> dictTypes = new ArrayList<>();

        if (StringUtils.isNotBlank(dto.getDictType())) {
            dictTypes.add(dto.getDictType());
        }
        if (CollectionUtils.isNotEmpty(dto.getDictTypes())) {
            dictTypes.addAll(dto.getDictTypes());
        }
        if (CollectionUtils.isNotEmpty(dictTypes)) {
            param.setDictTypes(dictTypes);
            itemParam.setDictTypes(dictTypes);
        }

        List<MdmDictDto> dicts = mapper.dicts4Copy(param);
        List<MdmDictItem> items = dictItemMapper.dictItems4Copy(itemParam);

        Map<String, List<MdmDictItem>> dictMap = items.stream().collect(Collectors.groupingBy(MdmDictItem::getDictType));
        dicts.forEach(t -> t.setItems(dictMap.get(t.getDictType())));
        return dicts;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer paste(List<MdmDictDto> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return 0;
        }
        String userCode = SessionHelper.getUserCode();
        if (userCode == null) {
            userCode = "anonymous";
        }

        List<String> dictTypes = dtos.stream().map(MdmDictDto::getDictType).collect(Collectors.toList());
        MdmDictDto param = new MdmDictDto();
        MdmDictItemDto itemParam = new MdmDictItemDto();
        param.setDictTypes(dictTypes);
        itemParam.setDictTypes(dictTypes);

        List<MdmDictDto> dicts = mapper.dicts4Update(param);
        List<MdmDictItem> items = dictItemMapper.dictItems4Update(itemParam);

        // dicts2Insert
        List<MdmDict> dicts2Insert = new ArrayList<>();
        // dicts2Update
        List<MdmDict> dicts2Update = new ArrayList<>();
        // items2Insert
        List<MdmDictItem> items2Insert = new ArrayList<>();
        // items2Update
        List<MdmDictItem> items2Update = new ArrayList<>();

        List<MdmDictItem> importItems = new ArrayList<>();
        for (MdmDictDto dto : dtos) {
            importItems.addAll(dto.getItems());
            boolean empty = true;
            for (MdmDictDto dict : dicts) {
                if (dto.getDictType().equals(dict.getDictType())) {
                    // compare 2 update
                    empty = false;
                    boolean update = false;
                    if (dto.getDictCtg() == null || !dto.getDictCtg().equals(dict.getDictCtg())) {
                        dict.setDictCtg(dto.getDictCtg());
                        update = true;
                    }
                    if (dto.getDescription() == null || !dto.getDescription().equals(dict.getDescription())) {
                        dict.setDescription(dto.getDescription());
                        update = true;
                    }
                    if (dto.getSort() == null || !dto.getSort().equals(dict.getSort())) {
                        dict.setSort(dto.getSort());
                        update = true;
                    }
                    if (dto.getRemark() == null || !dto.getRemark().equals(dict.getRemark())) {
                        dict.setRemark(dto.getRemark());
                        update = true;
                    }
                    if (update) {
                        dicts2Update.add(dict);
                    }
                }
            }
            // new 2 insert
            if (empty) {
                MdmDict insert = new MdmDict();
                insert.setDictCtg(dto.getDictCtg());
                insert.setDictType(dto.getDictType());
                insert.setDescription(dto.getDescription());
                insert.setSort(dto.getSort());
                insert.setRemark(dto.getRemark());
                insert.setCreateBy(userCode);
                insert.setUpdateBy(userCode);
                dicts2Insert.add(insert);
            }
        }


        for (MdmDictItem dto : importItems) {
            boolean empty = true;
            for (MdmDictItem item : items) {
                if (dto.getDictType().equals(item.getDictType()) && dto.getDictValue().equals(item.getDictValue()) ) {
                    // compare 2 update
                    empty = false;
                    boolean update = false;
                    if (dto.getDictLabel() == null || !dto.getDictLabel().equals(item.getDictLabel())) {
                        item.setDictLabel(dto.getDictLabel());
                        update = true;
                    }
                    if (dto.getElType() == null || !dto.getElType().equals(item.getElType())) {
                        item.setElType(dto.getElType());
                        update = true;
                    }
                    if (dto.getDescription() == null || !dto.getDescription().equals(item.getDescription())) {
                        item.setDescription(dto.getDescription());
                        update = true;
                    }
                    if (dto.getEnableFlag() == null || !dto.getEnableFlag().equals(item.getEnableFlag())) {
                        item.setEnableFlag(dto.getEnableFlag());
                        update = true;
                    }
                    if (dto.getSort() == null || !dto.getSort().equals(item.getSort())) {
                        item.setSort(dto.getSort());
                        update = true;
                    }
                    if (dto.getRemark() == null || !dto.getRemark().equals(item.getRemark())) {
                        item.setRemark(dto.getRemark());
                        update = true;
                    }
                    if (update) {
                        items2Update.add(item);
                    }
                    break;
                }
            }
            // new 2 insert
            if (empty) {
                MdmDictItem insert = new MdmDictItem();
                insert.setDictType(dto.getDictType());
                insert.setDictValue(dto.getDictValue());
                insert.setDictLabel(dto.getDictLabel());
                insert.setElType(dto.getElType());
                insert.setDescription(dto.getDescription());
                insert.setEnableFlag(dto.getEnableFlag());
                insert.setSort(dto.getSort());
                insert.setRemark(dto.getRemark());
                insert.setCreateBy(userCode);
                insert.setUpdateBy(userCode);
                items2Insert.add(insert);
            }
        }


        Integer count = 0;
        if (CollectionUtils.isNotEmpty(dicts2Insert)) {
           Integer i = mapper.insertBatch(dicts2Insert);
            count += i;
        }
        if (CollectionUtils.isNotEmpty(dicts2Update)) {
            for (MdmDict mdmDict : dicts2Update) {
                mapper.updateById(mdmDict);
            }
            count += dicts2Update.size();
        }
        if (CollectionUtils.isNotEmpty(items2Insert)) {
            Integer i = dictItemMapper.insertBatch(items2Insert);
            count += i;
        }
        if (CollectionUtils.isNotEmpty(items2Update)) {
            for (MdmDictItem mdmDictItem : items2Update) {
                dictItemMapper.updateById(mdmDictItem);
            }
            count += items2Update.size();
        }
        return count;
    }


    public List<MdmDict> dictOptions() {
        return mapper.dictOptions();
    }

}

