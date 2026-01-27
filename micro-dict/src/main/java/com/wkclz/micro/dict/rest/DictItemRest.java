package com.wkclz.micro.dict.rest;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.dict.cache.DictCache;
import com.wkclz.micro.dict.pojo.dto.MdmDictItemDto;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.micro.dict.service.MdmDictItemService;
import com.wkclz.tool.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Created: wangkaicun @ 2018-10-30 15:11:51
 */
@RestController
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
    public R dictItemList(MdmDictItemDto dto) {
        String dictType = dto.getDictType();

        if (StringUtils.isBlank(dictType)) {
            return R.error("dictType 不能为空");
        }
        if (!dictType.equals(dictType.toUpperCase())) {
            dto.setDictType(StringUtil.camelToUnderline(dto.getDictType()).toUpperCase());
        }

        dto.setOrderBy("sort ASC, id ASC");
        List<MdmDictItem> dictItemList = mdmDictItemService.selectByEntity(dto);
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
    public R dictItemSave(@RequestBody MdmDictItem model) {
        if (model.getDictType() == null) {
            return R.warn("字典类型不能为空");
        }
        if (StringUtils.isBlank(model.getDictValue())) {
            return R.warn("字典值不能为空");
        }
        if (StringUtils.isBlank(model.getDictLabel())) {
            return R.warn("字典标签不能为空");
        }
        if (model.getEnableFlag() == null) {
            model.setEnableFlag(1);
        }
        MdmDictItemDto param = new MdmDictItemDto();
        param.setDictType(model.getDictType());
        param.setDictValue(model.getDictValue());
        MdmDictItem mdmDictItem = mdmDictItemService.selectOneByEntity(param);
        if (mdmDictItem != null && model.getId() == null) {
            return R.error("键重复，添加失败");
        }
        if (mdmDictItem != null && model.getId() != null && !mdmDictItem.getId().equals(mdmDictItem.getId())) {
            return R.error("键重复，修改失败");
        }

        if (model.getId() == null) {
            mdmDictItemService.insert(model);
            model.setId(model.getId());
        } else {
            MdmDictItem oldModel = mdmDictItemService.selectById(model.getId());
            if (oldModel == null) {
                return R.error("id不正确，数据不存在");
            }
            mdmDictItemService.updateById(model);
        }
        dictCache.clearCache();
        return R.ok(model);
    }


    /**
     * @api {post} /dict/item/remove 13. 字典内容删除
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典内容删除
     *
     * @apiParam {Integer} [id] <code>body</code> 主键 id
     * @apiParam {Integer[]} [ids] <code>body</code> 主键 id组
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "id": 1,
     *     "ids": [1]
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.DICT_ITEM_REMOVE)
    @Transactional(rollbackFor = Exception.class)
    public R dictItemRemove(@RequestBody MdmDictItem entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Integer rt = mdmDictItemService.deleteById(entity);
        dictCache.clearCache();
        return R.ok(rt);
    }

}
