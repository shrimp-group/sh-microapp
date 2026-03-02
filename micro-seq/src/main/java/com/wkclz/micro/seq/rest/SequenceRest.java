package com.wkclz.micro.seq.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.seq.pojo.entity.MdmSequence;
import com.wkclz.micro.seq.service.MdmSequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_sequence (序列生成) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class SequenceRest {

    @Autowired
    private MdmSequenceService mdmSequenceService;


    /**
     * @api {get} /micro-seq/sequence/page 1. 序列生成-获取分页
     * @apiGroup SEQ
     *
     * @apiVersion 0.0.1
     * @apiDescription 序列生成-获取分页
     *
     * @apiParam {String} [seqName] <code>param</code>名称(模糊)
     * @apiParam {String} [prefix] <code>param</code>前缀(模糊)
     * @apiParam {Integer} [sequence] <code>param</code>当前序列
     * @apiParam {Integer} [codeLength] <code>param</code>序列长度(不计前缀长度)
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [seqName] 名称
     * @apiSuccess {String} [prefix] 前缀
     * @apiSuccess {Integer} [sequence] 当前序列
     * @apiSuccess {Integer} [codeLength] 序列长度(不计前缀长度)
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "seqName": "seqName",
     *                 "prefix": "prefix",
     *                 "sequence": "sequence",
     *                 "codeLength": "codeLength",
     *                 "sort": "sort",
     *                 "createTime": "createTime",
     *                 "createBy": "createBy",
     *                 "updateTime": "updateTime",
     *                 "updateBy": "updateBy",
     *                 "remark": "remark",
     *                 "version": "version",
     *             },
     *             ...
     *         ],
     *         "current": 1,
     *         "size": 10,
     *         "total": 1,
     *         "page": 1,
     *     }
     * }
     *
     */
    @GetMapping(Route.SEQUENCE_PAGE)
    public R mdmSequencePage(MdmSequence entity) {
        PageData<MdmSequence> page = mdmSequenceService.getSequencePage(entity);
        return R.ok(page);
    }

    /**
     * @api {get} /micro-seq/sequence/info 2. 序列生成-获取详情
     * @apiGroup SEQ
     *
     * @apiVersion 0.0.1
     * @apiDescription 序列生成-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [seqName] 名称
     * @apiSuccess {String} [prefix] 前缀
     * @apiSuccess {Integer} [sequence] 当前序列
     * @apiSuccess {Integer} [codeLength] 序列长度(不计前缀长度)
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
     *          "seqName": "seqName",
     *          "prefix": "prefix",
     *          "sequence": "sequence",
     *          "codeLength": "codeLength",
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
    @GetMapping(Route.SEQUENCE_INFO)
    public R mdmSequenceInfo(MdmSequence entity) {
        entity = mdmSequenceService.selectById(entity.getId());
        return R.ok(entity);
    }

    /**
     * @api {post} /micro-seq/sequence/update 3. 序列生成-更新
     * @apiGroup SEQ
     *
     * @apiVersion 0.0.1
     * @apiDescription 序列生成-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [seqName] <code>body</code>名称
     * @apiParam {String} [prefix] <code>body</code>前缀
     * @apiParam {Integer} [sequence] <code>body</code>当前序列
     * @apiParam {Integer} [codeLength] <code>body</code>序列长度(不计前缀长度)
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "seqName": "seqName",
     *      "prefix": "prefix",
     *      "sequence": "sequence",
     *      "codeLength": "codeLength",
     *      "sort": "sort",
     *      "remark": "remark",
     *      "version": "version",
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.SEQUENCE_UPDATE)
    public R mdmSequenceUpdate(@RequestBody MdmSequence entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        Assert.notNull(entity.getSeqName(), "seqName 不能为空");
        Assert.notNull(entity.getPrefix(), "prefix 不能为空");
        Assert.notNull(entity.getSequence(), "sequence 不能为空");
        Assert.notNull(entity.getCodeLength(), "sodeLength 不能为空");
        entity = mdmSequenceService.update(entity);
        return R.ok(entity);
    }

}

