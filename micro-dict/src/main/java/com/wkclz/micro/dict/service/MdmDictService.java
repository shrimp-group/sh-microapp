package com.wkclz.micro.dict.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.dict.cache.DictCache;
import com.wkclz.micro.dict.mapper.MdmDictItemMapper;
import com.wkclz.micro.dict.mapper.MdmDictMapper;
import com.wkclz.micro.dict.bean.dto.MdmDictDto;
import com.wkclz.micro.dict.bean.dto.MdmDictItemDto;
import com.wkclz.micro.dict.bean.entity.MdmDict;
import com.wkclz.micro.dict.bean.entity.MdmDictItem;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict (字典) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_dict 的逻辑. 其他逻辑放 custom 中
 */

@Service
public class MdmDictService extends BaseService<MdmDict, MdmDictMapper> {

    @Autowired
    private DictCache dictCache;
    @Autowired
    private MdmDictItemMapper dictItemMapper;
    @Autowired
    private MdmDictItemService mdmDictItemService;

    public PageData<MdmDict> getDictPage(MdmDict model) {
        return PageQuery.page(model, mapper::getDictList);
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmDict dictCreate(MdmDict entity) {
        entity.setId(null);
        MdmDict param = new MdmDict();
        param.setDictType(entity.getDictType());
        long count = selectCountByEntity(param);
        if (count > 0) {
            throw ValidationException.of(entity.getDictType() + " 已存在，不可重复");
        }
        insert(entity);
        dictCache.clearCache();
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmDict dictUpdate(MdmDict entity) {
        MdmDict mdmDict = selectById(entity.getId());
        if (mdmDict == null) {
            throw ValidationException.of("数据不存在");
        }

        MdmDict param1 = new MdmDict();
        param1.setDictType(entity.getDictType());
        param1 = selectOneByEntity(param1);
        if (param1 != null && !param1.getId().equals(entity.getId())) {
            throw ValidationException.of(entity.getDictType() + " 已存在，不可重复");
        }

        // 如果把 dictType 都改了，要多加校验，子表也要改
        if (!mdmDict.getDictType().equals(entity.getDictType())) {
            dictItemMapper.updateDictTypeBatch(mdmDict.getDictType(), entity.getDictType());
        }
        updateById(entity);
        dictCache.clearCache();
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer dictRemove(Long id) {
        MdmDict mdmDict = selectById(id);
        if (mdmDict == null) {
            throw ValidationException.of("数据不存在");
        }

        MdmDictItem itemParam = new MdmDictItem();
        itemParam.setDictType(mdmDict.getDictType());
        long count = mdmDictItemService.selectCountByEntity(itemParam);
        if (count > 0) {
            throw ValidationException.of("请先删除字典枚举，再删除字典");
        }

        MdmDict deleteEntity = new MdmDict();
        deleteEntity.setId(id);
        Integer rt = deleteById(deleteEntity);
        dictCache.clearCache();
        return rt;
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

        // 校验和默认值设置
        for (MdmDictDto d : dtos) {
            if (StringUtils.isBlank(d.getDictCtg())) {
                throw ValidationException.of("数据中缺少 dictCtg");
            }
            if (StringUtils.isBlank(d.getDictType())) {
                throw ValidationException.of("数据中缺少 dictType");
            }
            List<MdmDictItem> items = d.getItems();
            if (CollectionUtils.isEmpty(items)) {
                throw ValidationException.of("数据中缺少 items");
            }
            if (d.getSort() == null) {
                d.setSort(0);
            }
            for (MdmDictItem i : items) {
                if (StringUtils.isBlank(i.getDictType())) {
                    throw ValidationException.of("数据中缺少 dictType");
                }
                if (StringUtils.isBlank(i.getDictValue())) {
                    throw ValidationException.of("数据中缺少 dictValue");
                }
                if (StringUtils.isBlank(i.getDictLabel())) {
                    throw ValidationException.of("数据中缺少 dictLabel");
                }
                if (i.getEnableFlag() == null) {
                    i.setEnableFlag(1);
                }
                if (i.getSort() == null) {
                    i.setSort(0);
                }
            }
        }

        String userCode = PrincipalContext.getUserCode();
        if (userCode == null) {
            userCode = "anonymous";
        }

        List<String> dictTypes = dtos.stream().map(MdmDictDto::getDictType).collect(Collectors.toList());
        MdmDictDto param = new MdmDictDto();
        MdmDictItemDto itemParam = new MdmDictItemDto();
        param.setDictTypes(dictTypes);
        itemParam.setDictTypes(dictTypes);

        List<MdmDictDto> existingDicts = mapper.dicts4Update(param);
        List<MdmDictItem> existingItems = dictItemMapper.dictItems4Update(itemParam);

        Map<String, MdmDictDto> existingDictMap = existingDicts.stream()
            .collect(Collectors.toMap(MdmDictDto::getDictType, d -> d, (a, b) -> a));
        Map<String, MdmDictItem> existingItemMap = existingItems.stream()
            .collect(Collectors.toMap(i -> i.getDictType() + ":" + i.getDictValue(), i -> i, (a, b) -> a));

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
            MdmDictDto existing = existingDictMap.get(dto.getDictType());
            if (existing != null) {
                boolean update = false;
                if (dto.getDictCtg() == null || !dto.getDictCtg().equals(existing.getDictCtg())) {
                    existing.setDictCtg(dto.getDictCtg());
                    update = true;
                }
                if (dto.getDescription() == null || !dto.getDescription().equals(existing.getDescription())) {
                    existing.setDescription(dto.getDescription());
                    update = true;
                }
                if (dto.getSort() == null || !dto.getSort().equals(existing.getSort())) {
                    existing.setSort(dto.getSort());
                    update = true;
                }
                if (dto.getRemark() == null || !dto.getRemark().equals(existing.getRemark())) {
                    existing.setRemark(dto.getRemark());
                    update = true;
                }
                if (update) {
                    dicts2Update.add(existing);
                }
            } else {
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
            String key = dto.getDictType() + ":" + dto.getDictValue();
            MdmDictItem existing = existingItemMap.get(key);
            if (existing != null) {
                boolean update = false;
                if (dto.getDictLabel() == null || !dto.getDictLabel().equals(existing.getDictLabel())) {
                    existing.setDictLabel(dto.getDictLabel());
                    update = true;
                }
                if (dto.getElType() == null || !dto.getElType().equals(existing.getElType())) {
                    existing.setElType(dto.getElType());
                    update = true;
                }
                if (dto.getDescription() == null || !dto.getDescription().equals(existing.getDescription())) {
                    existing.setDescription(dto.getDescription());
                    update = true;
                }
                if (dto.getEnableFlag() == null || !dto.getEnableFlag().equals(existing.getEnableFlag())) {
                    existing.setEnableFlag(dto.getEnableFlag());
                    update = true;
                }
                if (dto.getSort() == null || !dto.getSort().equals(existing.getSort())) {
                    existing.setSort(dto.getSort());
                    update = true;
                }
                if (dto.getRemark() == null || !dto.getRemark().equals(existing.getRemark())) {
                    existing.setRemark(dto.getRemark());
                    update = true;
                }
                if (update) {
                    items2Update.add(existing);
                }
            } else {
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

        int count = 0;
        if (CollectionUtils.isNotEmpty(dicts2Insert)) {
            count += mapper.insertBatch(dicts2Insert);
        }
        if (CollectionUtils.isNotEmpty(dicts2Update)) {
            for (MdmDict mdmDict : dicts2Update) {
                mapper.updateByIdSelective(mdmDict);
            }
            count += dicts2Update.size();
        }
        if (CollectionUtils.isNotEmpty(items2Insert)) {
            count += dictItemMapper.insertBatch(items2Insert);
        }
        if (CollectionUtils.isNotEmpty(items2Update)) {
            for (MdmDictItem mdmDictItem : items2Update) {
                dictItemMapper.updateByIdSelective(mdmDictItem);
            }
            count += items2Update.size();
        }

        dictCache.clearCache();
        return count;
    }

    public List<MdmDict> dictOptions() {
        return mapper.dictOptions();
    }

}
