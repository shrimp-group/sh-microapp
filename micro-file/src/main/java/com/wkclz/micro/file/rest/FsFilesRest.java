package com.wkclz.micro.file.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.file.api.FsApi;
import com.wkclz.micro.file.pojo.dto.MdmFsFilesDto;
import com.wkclz.micro.file.pojo.entity.MdmFsFiles;
import com.wkclz.micro.file.service.MdmFsFilesService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_files (附件) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class FsFilesRest {

    @Autowired
    private FsApi fsApi;
    @Autowired
    private MdmFsFilesService mdmFsFilesService;

    /**
     * @api {get} /micro-file/files/page 6. 附件-获取分页
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription 附件-获取分页
     *
     * @apiParam {String} [tenantCode] <code>param</code>租户编码
     * @apiParam {String} [busnessType] <code>param</code>业务类型
     * @apiParam {Long} [fileSize] <code>param</code>文件大小
     * @apiParam {String} [fileName] <code>param</code>文件名
     * @apiParam {String} [fileType] <code>param</code>文件类型
     * @apiParam {String} [ossSp] <code>param</code>OSS服务商
     * @apiParam {String} [bucket] <code>param</code>Bucket
     * @apiParam {String} [downloadPath] <code>param</code>下载路径
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [busnessType] 业务类型
     * @apiSuccess {Long} [fileSize] 文件大小
     * @apiSuccess {String} [fileName] 文件名
     * @apiSuccess {String} [fileType] 文件类型
     * @apiSuccess {String} [ossSp] OSS服务商
     * @apiSuccess {String} [bucket] Bucket
     * @apiSuccess {String} [downloadPath] 下载路径
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "tenantCode": "tenantCode",
     *                 "busnessType": "busnessType",
     *                 "fileSize": "fileSize",
     *                 "fileName": "fileName",
     *                 "fileType": "fileType",
     *                 "ossSp": "ossSp",
     *                 "bucket": "bucket",
     *                 "downloadPath": "downloadPath",
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
    @GetMapping(Route.FILES_PAGE)
    public R mdmFsFilesPage(MdmFsFiles entity) {
        Assert.notNull(entity.getTimeFrom(), "timeFrom can not be null");
        Assert.notNull(entity.getTimeTo(), "timeTo can not be null");
        PageData<MdmFsFiles> page = mdmFsFilesService.getPage(entity);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return R.ok(page);
        }
        List<MdmFsFilesDto> rows = page.getRecords().stream().map(MdmFsFilesDto::copy).collect(Collectors.toList());
        fsApi.sign(rows, MdmFsFilesDto::getFileId, MdmFsFilesDto::setPreviewUrl);

        // 转换成新 page
        PageData<MdmFsFilesDto> newPage = PageData.of(rows, page.getTotal(), page.getCurrent(), page.getSize());
        return R.ok(newPage);
    }

    /**
     * @api {get} /micro-file/files/info 7. 附件-获取详情
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription 附件-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [busnessType] 业务类型
     * @apiSuccess {Long} [fileSize] 文件大小
     * @apiSuccess {String} [fileName] 文件名
     * @apiSuccess {String} [fileType] 文件类型
     * @apiSuccess {String} [ossSp] OSS服务商
     * @apiSuccess {String} [bucket] Bucket
     * @apiSuccess {String} [downloadPath] 下载路径
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
     *          "tenantCode": "tenantCode",
     *          "busnessType": "busnessType",
     *          "fileSize": "fileSize",
     *          "fileName": "fileName",
     *          "fileType": "fileType",
     *          "ossSp": "ossSp",
     *          "bucket": "bucket",
     *          "downloadPath": "downloadPath",
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
    @GetMapping(Route.FILES_INFO)
    public R mdmFsFilesInfo(MdmFsFiles entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        entity = mdmFsFilesService.getInfo(entity);
        MdmFsFilesDto dto = MdmFsFilesDto.copy(entity);
        dto.setPreviewUrl(fsApi.sign(entity));
        return R.ok(dto);
    }

    /**
     * @api {post} /micro-file/files/remove 8. 附件-删除
     * @apiGroup FS
     *
     * @apiVersion 0.0.1
     * @apiDescription 附件-删除
     *
     * @apiParam {Long} [id] <code>body</code>数据id
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
    @PostMapping(Route.FILES_REMOVE)
    public R mdmFsFilesRemove(@RequestBody MdmFsFiles entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        mdmFsFilesService.remove(entity);
        return R.ok(1);
    }

}

