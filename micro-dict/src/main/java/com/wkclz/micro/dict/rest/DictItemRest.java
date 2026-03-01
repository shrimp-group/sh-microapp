package com.wkclz.micro.dict.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dict.cache.DictCache;
import com.wkclz.micro.dict.pojo.dto.MdmDictDto;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.micro.dict.service.MdmDictItemService;
import com.wkclz.tool.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Created: wangkaicun @ 2018-10-30 15:11:51
 */
@RestController
@RequestMapping(Route.PREFIX)
public class DictItemRest {

    @Autowired
    private DictCache dictCache;
    @Autowired
    private MdmDictItemService mdmDictItemService;


    /**
     * @api {get} /dict/item/list 11. 字典内容列表（不分页）
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典内容列表（不分页）
     *
     * @apiParam {Integer} dictType <code>param</code>字典类型【可全下划线大写，可小写驼峰】
     * @apiParam {String} [dictLabel] <code>param</code>字典项
     * @apiParam {String} [dictValue] <code>param</code>字典值【支持模糊查询】
     * @apiParam {String} [description] <code>param</code>描述【支持模糊查询】
     * @apiParam {Integer} [enableFlag] <code>param</code>生效状态
     *
     * @apiParamExample {param} 请求样例:
     * ?dictType=1
     *
     * @apiSuccess {Integer} [id] id
     * @apiSuccess {Integer} [dictType] 字典类型
     * @apiSuccess {String} [dictLabel] 字典项
     * @apiSuccess {String} [dictValue] 字典值
     * @apiSuccess {String} [description] 描述
     * @apiSuccess {Integer} [enableFlag] 生效状态
     * @apiSuccess {Integer} [sort] 排序
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "id": "id",
     *             "dictType": "dictType",
     *             "dictLabel": "dictLabel",
     *             "dictValue": "dictValue",
     *             "description": "description",
     *             "enableFlag": "enableFlag",
     *         },
     *         ...
     *     ],
     * }
     *
     */
    @GetMapping(Route.DICT_ITEM_LIST)
    public R dictItemList(MdmDictItem entity) {
        String dictType = entity.getDictType();

        if (StringUtils.isBlank(dictType)) {
            return R.error("dictType 不能为空");
        }
        if (!dictType.equals(dictType.toUpperCase())) {
            entity.setDictType(StringUtil.camelToUnderline(entity.getDictType()).toUpperCase());
        }
        List<MdmDictItem> dictItemList = mdmDictItemService.getDictItemList(dictType);
        return R.ok(dictItemList);
    }


    /**
     * @api {post} /dict/item/save 12. 字典内容添加/修改
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典内容添加/修改
     *
     * @apiParam {Integer} [id] <code>body</code>id【修改时必需】
     * @apiParam {Integer} [dictType] <code>body</code>字典类型
     * @apiParam {String} [dictLabel] <code>body</code>字典项
     * @apiParam {String} [dictValue] <code>body</code>字典值
     * @apiParam {String} [description] <code>body</code>描述
     * @apiParam {Integer} [enableFlag] <code>body</code>生效状态
     * @apiParam {Integer} [sort] <code>body</code>排序
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "dictType": "dictType",
     *      "dictLabel": "dictLabel",
     *      "dictValue": "dictValue",
     *      "description": "description",
     *      "enableFlag": "enableFlag",
     *      "sort": "sort"
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectDto
     * }
     *
     */
    @PostMapping(Route.DICT_ITEM_SAVE)
    @Transactional(rollbackFor = Exception.class)
    public R dictItemSave(@RequestBody MdmDictDto dto) {
        Assert.notNull(dto.getDictType(), "字典类型不能为空");
        if (dto.getItems() == null) {
            dto.setItems(new ArrayList<>());
        } else  {
            for (MdmDictItem item : dto.getItems()) {
                Assert.notNull(item.getDictType(), "字典类型不能为空");
                Assert.notNull(item.getDictValue(), "字典值不能为空");
                Assert.notNull(item.getDictLabel(), "字典标签不能为空");
                if (item.getEnableFlag() == null) {
                    item.setEnableFlag(1);
                }
                if (item.getSort() == null) {
                    item.setSort(99);
                }
                item.setDictType(dto.getDictType());
            }
        }
        Integer modifys = mdmDictItemService.dictItemSave(dto);
        if (modifys > 0) {
            dictCache.clearCache();
        }
        return R.ok(modifys);
    }

}
