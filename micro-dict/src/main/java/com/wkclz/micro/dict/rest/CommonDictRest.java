package com.wkclz.micro.dict.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dict.pojo.dto.MdmDictDto;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.micro.dict.service.MdmDictItemService;
import com.wkclz.tool.utils.StringUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created: wangkaicun @ 2018-10-30 15:11:51
 */
@RestController
public class CommonDictRest {

    @Resource
    private MdmDictItemService mdmDictItemService;



    /**
     * @api {get} /common/dict/list 21. 字典内容列表（单字典,不分页）
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典内容列表（不分页）
     *
     * @apiParam {String} dictType <code>param</code>字典类型英文(只支持一个)
     *
     * @apiParamExample {param} 请求样例:
     * ?dictType=1
     *
     * @apiSuccess {Integer} [id] id
     * @apiSuccess {String} [dictType] 字典类型
     * @apiSuccess {String} [dictKey] 字典项
     * @apiSuccess {String} [dictValue] 字典值
     * @apiSuccess {String} [description] 描述
     * @apiSuccess {Integer} [sort] 排序
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "id": "id",
     *             "dictType": "dictType",
     *             "dictKey": "dictKey",
     *             "dictValue": "dictValue",
     *             "description": "description",
     *         },
     *         ...
     *     ],
     * }
     *
     */
    @GetMapping(Route.COMMON_DICT_LIST)
    public R commonDictList(MdmDictDto dto) {
        String dictType = dto.getDictType();

        if (StringUtils.isBlank(dictType)) {
            return R.error("dictType 不能为空");
        }
        if (!dictType.equals(dictType.toUpperCase())) {
            dictType = StringUtil.camelToUnderline(dictType).toUpperCase();
        }
        List<String> dictTypes = Arrays.asList(dictType);
        List<MdmDictItem> items = mdmDictItemService.getDictItemsByDictTypes(dictTypes);
        return R.ok(items);
    }


    /**
     * @api {get} /common/dicts/list 22. 字典内容列表（多字典,不分页）
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典内容列表（不分页）
     *
     * @apiParam {String} dictType <code>param</code>字典类型英文逗号分隔查询多个
     *
     * @apiParamExample {param} 请求样例:
     * ?dictType=1
     *
     * @apiSuccess {Integer} [id] id
     * @apiSuccess {String} [dictType] 字典类型
     * @apiSuccess {String} [dictKey] 字典项
     * @apiSuccess {String} [dictValue] 字典值
     * @apiSuccess {String} [description] 描述
     * @apiSuccess {Integer} [sort] 排序
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "XXX_TYPE": [{
     *             "id": "id",
     *             "dictType": "dictType",
     *             "dictKey": "dictKey",
     *             "dictValue": "dictValue",
     *             "description": "description",
     *         }],
     *         ...
     *     },
     * }
     *
     */
    @GetMapping(Route.COMMON_DICTS_LIST)
    public R commonDictsList(MdmDictDto dto) {
        String dictType = dto.getDictType();

        if (StringUtils.isBlank(dictType)) {
            return R.error("dictType 不能为空");
        }
        if (!dictType.equals(dictType.toUpperCase())) {
            dictType = StringUtil.camelToUnderline(dictType).toUpperCase();
        }
        List<String> dictTypes = Arrays.asList(dictType.split(","));
        List<MdmDictItem> items = mdmDictItemService.getDictItemsByDictTypes(dictTypes);
        Map<String, List<MdmDictItem>> map = items.stream().collect(Collectors.groupingBy(MdmDictItem::getDictType));
        return R.ok(map);
    }

}
