package com.wkclz.micro.fun.rest;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.fun.pojo.dto.FunCategoryDto;
import com.wkclz.micro.fun.pojo.entity.FunCategory;
import com.wkclz.micro.fun.service.FunCategoryService;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_category (函数-分类) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class FunCategoryRest {

    @Autowired
    private FunCategoryService funCategoryService;

    /**
     * @api {get} /fun/category/list 1. 函数分类-列表
     * @apiGroup FUN_CATEGORY
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数分类-获取分页
     *
     * @apiParam {String} [pcode] <code>param</code>父类Code,0为顶级
     * @apiParam {String} [categoryCode] <code>param</code>分类编码
     * @apiParam {String} [categoryName] <code>param</code>分类名称
     * @apiParam {String} [description] <code>param</code>描述
     * @apiParam {Integer} [visible] <code>param</code>可见1/0
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [pcode] 父类Code,0为顶级
     * @apiSuccess {String} [categoryCode] 分类编码
     * @apiSuccess {String} [categoryName] 分类名称
     * @apiSuccess {String} [description] 描述
     * @apiSuccess {Integer} [visible] 可见1/0
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *          {
     *              "pcode": "pcode",
     *              "categoryCode": "categoryCode",
     *              "categoryName": "categoryName",
     *              "description": "description",
     *              "visible": "visible",
     *          },
     *          ...
     *      ]
     * }
     *
     */
    @GetMapping(Route.FUN_CATEGORY_LIST)
    public R funCategoryList(FunCategory entity) {
        List<FunCategory> list = funCategoryService.getFunCategoryList(entity);
        return R.ok(list);
    }

    /**
     * @api {get} /fun/category/tree 2. 函数分类-树
     * @apiGroup FUN_CATEGORY
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数分类-树
     *
     * @apiParam {Integer} [visible] <code>param</code>可见1/0
     *
     * @apiParamExample {param} 请求样例:
     * ?visible=1
     *
     * @apiSuccess {String} [pcode] 父类Code,0为顶级
     * @apiSuccess {String} [categoryCode] 分类编码
     * @apiSuccess {String} [categoryName] 分类名称
     * @apiSuccess {String} [description] 描述
     * @apiSuccess {Integer} [visible] 可见1/0
     * @apiParam {Object[]} [children] 子级
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *          {
     *              "pcode": "pcode",
     *              "categoryCode": "categoryCode",
     *              "categoryName": "categoryName",
     *              "description": "description",
     *              "visible": "visible",
     *              “children”： [],
     *          },
     *          ...
     *      ]
     * }
     *
     */
    @GetMapping(Route.FUN_CATEGORY_TREE)
    public R funCategoryTree(FunCategory entity) {
        List<FunCategoryDto> tree = funCategoryService.getFunCategoryTree(entity);
        return R.ok(tree);
    }


    /**
     * @api {get} /fun/category/info 3. 函数分类-详情
     * @apiGroup FUN_CATEGORY
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数分类-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据Id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [pcode] 父类Code,0为顶级
     * @apiSuccess {String} [categoryCode] 分类编码
     * @apiSuccess {String} [categoryName] 分类名称
     * @apiSuccess {String} [description] 描述
     * @apiSuccess {Integer} [visible] 可见1/0
     * @apiSuccess {Integer} [sort] 排序
     * @apiSuccess {Date} [createTime] 创建时间
     * @apiSuccess {String} [createBy] 创建人
     * @apiSuccess {Date} [updateTime] 更新时间
     * @apiSuccess {String} [updateBy] 更新人
     * @apiSuccess {String} [remark] 备注
     * @apiSuccess {Integer} [version] 版本号
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "id": "id",
     *          "pcode": "pcode",
     *          "categoryCode": "categoryCode",
     *          "categoryName": "categoryName",
     *          "description": "description",
     *          "visible": "visible",
     *          "sort": "sort",
     *          "createTime": "createTime",
     *          "createBy": "createBy",
     *          "updateTime": "updateTime",
     *          "updateBy": "updateBy",
     *          "remark": "remark",
     *          "version": "version",
     *     }
     * }
     *
     */
    @GetMapping(Route.FUN_CATEGORY_INFO)
    public R funCategoryInfo(FunCategory entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        entity = funCategoryService.selectById(entity.getId());
        return R.ok(entity);
    }


    /**
     * @api {post} /fun/category/create 4. 函数分类-创建
     * @apiGroup FUN_CATEGORY
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数分类-创建
     *
     * @apiParam {String} [pcode] <code>body</code>父类Code,0为顶级
     * @apiParam {String} [categoryCode] <code>body</code>分类编码[留宿时自动生成]
     * @apiParam {String} [categoryName] <code>body</code>分类名称
     * @apiParam {String} [description] <code>body</code>描述
     * @apiParam {Integer} [visible] <code>body</code>可见1/0
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "pcode": "pcode",
     *      "categoryCode": "categoryCode",
     *      "categoryName": "categoryName",
     *      "description": "description",
     *      "visible": "visible",
     *      "sort": "sort",
     *      "remark": "remark"
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.FUN_CATEGORY_CREATE)
    public R funCategoryCreate(@RequestBody FunCategory entity) {
        checkParan(entity);
        entity = funCategoryService.create(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /fun/category/update 5. 函数分类-update
     * @apiGroup FUN_CATEGORY
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数分类-update
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [pcode] <code>body</code>父类Code,0为顶级
     * @apiParam {String} [categoryCode] <code>body</code>分类编码
     * @apiParam {String} [categoryName] <code>body</code>分类名称
     * @apiParam {String} [description] <code>body</code>描述
     * @apiParam {Integer} [visible] <code>body</code>可见1/0
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} [version] <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "pcode": "pcode",
     *      "categoryCode": "categoryCode",
     *      "categoryName": "categoryName",
     *      "description": "description",
     *      "visible": "visible",
     *      "sort": "sort",
     *      "remark": "remark",
     *      "version": "version"
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.FUN_CATEGORY_UPDATE)
    public R funCategoryUpdate(@RequestBody FunCategory entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        checkParan(entity);

        entity = funCategoryService.update(entity);
        return R.ok(entity);
    }


    /**
     * @api {post} /fun/category/remove 6. 函数分类-删除
     * @apiGroup FUN_CATEGORY
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数分类-删除
     *
     * @apiParam {Long} [id] <code>body</code> 主键 id
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
    @PostMapping(Route.FUN_CATEGORY_REMOVE)
    public R funCategoryRemove(@RequestBody FunCategory entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Integer rt = funCategoryService.customDelete(entity);
        return R.ok(rt);
    }



    /**
     * @api {get} /fun/category/options 5. 函数分类-获取下拉选项
     * @apiGroup FUN_CATEGORY
     *
     * @apiVersion 0.0.1
     * @apiDescription 函数分类-获取分页
     *
     * @apiSuccess {String} [pcode] 父类Code,0为顶级
     * @apiSuccess {String} [categoryCode] 分类编码
     * @apiSuccess {String} [categoryName] 分类名称
     * @apiSuccess {Integer} [visible] 可见1/0
     * @apiParam {Object[]} [children] 子级
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *          {
     *              "pcode": "pcode",
     *              "categoryCode": "categoryCode",
     *              "categoryName": "categoryName",
     *              "visible": "visible",
     *              "children": [],
     *          },
     *          ...
     *      ]
     * }
     *
     */
    @GetMapping(Route.FUN_CATEGORY_OPTIONS)
    public R funCategoryOptions() {
        FunCategory funCategory = new FunCategory();
        funCategory.setVisible(1);
        List<FunCategoryDto> tree = funCategoryService.getFunCategoryOptions(funCategory);
        return R.ok(tree);
    }
    
    
    
    private static void checkParan(FunCategory entity) {
        Assert.notNull(entity.getCategoryName(), "categoryName 不能为空");
        Assert.notNull(entity.getPcode(), "pcode 不能为空");
        if (entity.getSort() == null) {
            entity.setSort(99);
        }
        if (entity.getVisible() == null) {
            entity.setVisible(1);
        }
    }

}

