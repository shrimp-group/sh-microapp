package com.wkclz.micro.dict.rest;

import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dict.bean.entity.MdmDictItem;
import com.wkclz.micro.dict.bean.req.CommonDictListReq;
import com.wkclz.micro.dict.bean.req.CommonDictsListReq;
import com.wkclz.micro.dict.bean.resp.DictItemResp;
import com.wkclz.micro.dict.service.MdmDictItemService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.tool.utils.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created: wangkaicun @ 2018-10-30 15:11:51
 */
@Tag(name = "3.公共字典", description = "公共字典查询接口（无需权限）")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class CommonDictRest {

    private static final int MAX_DICT_TYPES = 50;

    @Autowired
    private MdmDictItemService mdmDictItemService;

    @Operation(summary = "1.公共字典-单字典查询", description = "根据字典类型查询单个字典项列表")
    @GetMapping(Route.COMMON_DICT_LIST)
    public R<List<DictItemResp>> commonDictList(@Valid CommonDictListReq req) {
        String dictType = req.getDictType();
        if (!dictType.equals(dictType.toUpperCase())) {
            dictType = StringUtil.camelToUnderline(dictType).toUpperCase();
        }
        List<String> dictTypes = List.of(dictType);
        List<MdmDictItem> items = mdmDictItemService.getDictItemsByDictTypes(dictTypes);
        List<DictItemResp> respList = BeanUtil.cp(items, DictItemResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.公共字典-多字典查询", description = "根据多个字典类型查询字典项列表（上限50）")
    @GetMapping(Route.COMMON_DICTS_LIST)
    public R<Map<String, List<DictItemResp>>> commonDictsList(@Valid CommonDictsListReq req) {
        String dictType = req.getDictType();
        List<String> dictTypes = Arrays.stream(dictType.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());
        if (dictTypes.size() > MAX_DICT_TYPES) {
            throw ValidationException.of("dictType 数量不能超过 " + MAX_DICT_TYPES);
        }
        List<MdmDictItem> items = mdmDictItemService.getDictItemsByDictTypes(dictTypes);
        Map<String, List<DictItemResp>> map = items.stream().collect(
                Collectors.groupingBy(MdmDictItem::getDictType,
                        Collectors.mapping(e -> BeanUtil.cp(e, DictItemResp.class), Collectors.toList())
                )
        );
        return R.ok(map);
    }

}
