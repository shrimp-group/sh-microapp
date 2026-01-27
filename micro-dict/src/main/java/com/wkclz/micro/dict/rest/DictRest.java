package com.wkclz.micro.dict.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dict.cache.DictCache;
import com.wkclz.micro.dict.pojo.dto.MdmDictDto;
import com.wkclz.micro.dict.pojo.entity.MdmDict;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.micro.dict.service.MdmDictItemService;
import com.wkclz.micro.dict.service.MdmDictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created: wangkaicun @ 2018-10-30 15:11:51
 */
@RestController
public class DictRest {

    @Autowired
    private DictCache dictCache;
    @Autowired
    private MdmDictService mdmDictService;
    @Autowired
    private MdmDictItemService mdmDictItemService;

    /**
     * @api {get} /dict/page 01. 字典类型列表分页
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典类型列表分页
     *
     * @apiParam {String} [dictCtg] <code>param</code>字典分组
     * @apiParam {String} [dictType] <code>param</code>类型名称【支持模糊查询】
     * @apiParam {String} [description] <code>param</code>描述信息【支持模糊查询】
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Integer} [id] 字典ID
     * @apiSuccess {String} [dictCtg] 字典分组
     * @apiSuccess {String} [dictType] 字典类型
     * @apiSuccess {String} [description] 描述信息
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "data": [
     *             {
     *                 "id": "id",
     *                 "dictCtg": "dictCtg",
     *                 "dictType": "dictType",
     *                 "description": "description"
     *             },
     *             ...
     *         ],
     *         "totalCount": 1,
     *         "totalPage": 1,
     *         "pageNo": 1,
     *         "pageSize": 10
     *     }
     * }
     *
     */
    @GetMapping(Route.DICT_PAGE)
    public R dictPage(MdmDict model) {
        PageData<MdmDict> dicts = mdmDictService.getDictList(model);
        return R.ok(dicts);
    }


    /**
     * @api {get} /dict/info 02. 字典类型列表详情
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典类型详情
     *
     * @apiParam {Long} id <code>param</code>数据Id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [dictCtg] 类分组
     * @apiSuccess {String} [dictType] 类型
     * @apiSuccess {String} [description] 类型描述信息
     * @apiSuccess {Integer} [sort] 排序
     * @apiSuccess {Date} [createTime] 创建时间
     * @apiSuccess {Long} [createBy] 创建人
     * @apiSuccess {Date} [updateTime] 更新时间
     * @apiSuccess {Long} [updateBy] 更新人
     * @apiSuccess {String} [remark] 备注
     * @apiSuccess {Integer} [version] 版本号
     * @apiSuccess {Integer} [status] status
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "id": "id",
     *          "dictCtg": "dictCtg",
     *          "dictType": "dictType",
     *          "description": "description",
     *          "sort": "sort",
     *          "createTime": "createTime",
     *          "createBy": "createBy",
     *          "updateTime": "updateTime",
     *          "updateBy": "updateBy",
     *          "remark": "remark",
     *          "version": "version",
     *          "status": "status",
     *     }
     * }
     *
     */
    @GetMapping(Route.DICT_INFO)
    public R dictDetail(MdmDict model) {
        if (model.getId()==null) {
            return R.error("id can not be null");
        }
        model = mdmDictService.selectById(model.getId());
        if (model == null) {
            return R.error("id is error");
        }
        return R.ok(model);
    }


    /**
     * @api {post} /dict/save 03. 字典类型添加/修改
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 新增信息
     *
     * @apiParam {String} [id] <code>body</code>id【修改时必需】
     * @apiParam {String} dictCtg <code>body</code>字典分组
     * @apiParam {String} dictType <code>body</code>类型名称
     * @apiParam {String} [description] <code>body</code>描述信息
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "dictCtg": "dictCtg",
     *      "dictType": "dictType",
     *      "description": "description",
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
    @PostMapping(Route.DICT_SAVE)
    @Transactional(rollbackFor = Exception.class)
    public R dictSave(@RequestBody MdmDict model) {
        Assert.notNull(model.getDictCtg(), "dictCtg 不能为空");
        Assert.notNull(model.getDictType(), "dictType 不能为空");

        if (model.getId() == null) {
            mdmDictService.insert(model);
            model.setId(model.getId());
        } else {
            MdmDict mdmDict = mdmDictService.selectById(model.getId());
            if (mdmDict == null) {
                throw ValidationException.of("数据不存在");
            }
            // 如果把 dictType 都改了，要多加校验，子表也要改
            if (!mdmDict.getDictType().equals(model.getDictType())) {
                // 校验
                MdmDict param = new MdmDict();
                param.setDictType(model.getDictType());
                Long count = mdmDictService.selectCountByEntity(param);
                if (count > 0) {
                    throw ValidationException.of(model.getDictType() + " 重复使用，请纠正");
                }

                // 修改子表
                MdmDictItem itemParam = new MdmDictItem();
                itemParam.setDictType(mdmDict.getDictType());
                List<MdmDictItem> items = mdmDictItemService.selectByEntity(itemParam);
                if (!CollectionUtils.isEmpty(items)) {
                    items.forEach(l->{
                        l.setDictType(model.getDictType());
                        mdmDictItemService.updateByIdSelective(l);
                    });
                }
            }
            mdmDictService.updateById(model);
        }
        dictCache.clearCache();
        return R.ok(model);
    }


    /**
     * @api {post} /dict/remove 04. 字典类型删除
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典类型删除
     *
     * @apiParam {Integer} [id] <code>body</code> 主键 id
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "id": 1
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.DICT_REMOVE)
    @Transactional(rollbackFor = Exception.class)
    public R dictRemove(@RequestBody MdmDict entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Integer rt = mdmDictService.deleteById(entity);
        dictCache.clearCache();
        return R.ok(rt);
    }


    /**
     * @api {get} /dict/copy 05. 字典-COPY
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典复制
     *
     * @apiParam {String} [dictType] <code>param</code>字典类型
     * @apiParam {String} [dictTypes] <code>param</code>字典类型,英文逗号分隔
     *
     * @apiParamExample {param} 请求样例:
     * ?dictType=xxxx
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [{}]
     * }
     *
     */
    @GetMapping(Route.DICT_COPY)
    public R dictCopy(MdmDictDto dto) {
        List<MdmDictDto> copy = mdmDictService.copy(dto);
        return R.ok(copy);
    }


    /**
     * @api {post} /dict/paste 06. 字典-PASTE
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 字典粘贴
     *
     * @apiParam {Object[]} [json] <code>body</code>从 copy 中获得的 json 数据
     *
     * @apiParamExample {param} 请求样例:
     * {}
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.DICT_PASTE)
    public R dictPaste(@RequestBody List<MdmDictDto> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return R.error("没有可制作的数据！");
        }

        for (MdmDictDto d : dto) {
            if (StringUtils.isBlank(d.getDictCtg())) {
                return R.error("数据中缺少 dictCtg");
            }
            if (StringUtils.isBlank(d.getDictType())) {
                return R.error("数据中缺少 dictType");
            }
            List<MdmDictItem> items = d.getItems();
            if (CollectionUtils.isEmpty(items)) {
                return R.error("数据中缺少 items");
            }
            if (d.getSort() == null) {
                d.setSort(0);
            }
            for (MdmDictItem i : items) {

                if (StringUtils.isBlank(i.getDictType())) {
                    return R.error("数据中缺少 dictType");
                }
                if (StringUtils.isBlank(i.getDictValue())) {
                    return R.error("数据中缺少 dictValue");
                }
                if (StringUtils.isBlank(i.getDictLabel())) {
                    return R.error("数据中缺少 dictLabel");
                }
                if (i.getEnableFlag() == null) {
                    i.setEnableFlag(1);
                }
                if (i.getSort() == null) {
                    i.setSort(0);
                }
            }
        }

        Integer paste = mdmDictService.paste(dto);
        return R.ok(paste);
    }

    /**
     * @api {get} /dict/options 07. 字典类型选项
     * @apiGroup DICT
     *
     * @apiVersion 0.0.1
     * @apiDescription 获取所有字典类型的选项列表
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "dictType": "dictType",
     *             "description": "description",
     *             "sort": "sort",
     *         },
     *         ...
     *     ]
     * }
     *
     */
    @GetMapping(Route.DICT_OPTIONS)
    public R dictOptions() {
        List<MdmDict> dicts = mdmDictService.dictOptions();
        return R.ok(dicts);
    }

}
