package com.wkclz.micro.file.rest;

import com.wkclz.core.base.R;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.api.FileUploadApi;
import com.wkclz.micro.file.bean.dto.MdmFileRecordDto;
import com.wkclz.micro.file.bean.entity.MdmFileRecord;
import com.wkclz.micro.file.config.FileConfig;
import com.wkclz.micro.file.helper.FileTypeHelper;
import com.wkclz.micro.file.service.MdmFileRecordService;
import com.wkclz.tool.tools.RegularTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-29 上午11:20
 */
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class FileCommonRest {

    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private FileUploadApi fileUploadApi;
    @Autowired
    private FileTypeHelper fileTypeHelper;
    @Autowired
    private MdmFileRecordService mdmFileRecordService;


    /**
     * 接口组：通用
     */


    /**
     * @api {POST} /common/upload 0. common-文件上传
     * @apiGroup COMMON
     *
     * @apiVersion 0.0.1
     * @apiDescription 文件上传
     *
     * @apiParam {String} [businessType] <code>param</code>业务类型,如：个人头像,可传 personal_avatar
     * @apiParam {String} [fileName] <code>param</code>文件名。可覆盖默认文件名
     * @apiParam {String} file <code>body</code>文件
     *
     * @apiSuccess {String} previewUrl 上传的附件访问地址（完整url）
     * @apiSuccess {Integer} fileSize 文件大小
     * @apiSuccess {String} businessType 业务分类
     * @apiSuccess {String} fileName 附件名
     * @apiSuccess {String} fileType 附件类型
     * @apiSuccess {String} ossSp 文件存储服务商
     * @apiSuccess {String} bucket Bucket
     * @apiSuccess {String} fileId 文件唯一ID
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *      "code": 1,
     *      "data": {
     *          "previewUrl": "http://oss.domain.com/xxx/file.exname",
     *          "fileSize": 1234,
     *          "businessType": "default",
     *          "fileName": "file.exname",
     *          "fileType": "exname",
     *          "ossSp": "ALI_OSS",
     *          "bucket": "test",
     *          "fileId": "xxx/file.exname",
     *      }
     * }
     *
     */


    @PostMapping(Route.COMMON_UPLOAD)
    public R commonUpload(@RequestParam("file") MultipartFile file,
                          String businessType, String bucket, String fileName) {
        return upload(file, businessType, bucket, fileName, false);
    }

    @PostMapping(Route.COMMON_UPLOAD_PUBLIC)
    public R commonUploadPublic(@RequestParam("file") MultipartFile file,
            String businessType, String bucket, String fileName) {
        return upload(file, businessType, bucket, fileName, true);
    }

    public R upload(
            MultipartFile file,
            String businessType,
            String bucket,
            String fileName,
            boolean isPublic
            ) {
        if (file.isEmpty()) {
            return R.warn("file is empty");
        }
        if (StringUtils.isBlank(businessType)) {
            return R.warn("businessType 为业务类型，请指定以便对附件进行分类！");
        }
        if (!RegularTool.isLegalChar(businessType)) {
            return R.warn("businessType 包含有非法字符！");
        }

        int size = (int) file.getSize();

        // 文件原名称
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNotBlank(fileName)) {
            originalFilename = fileName;
        }
        // 文件类型
        String fileType = FileTypeHelper.getExtName(originalFilename);

        // 当图片大于2M 不允许上传
        Integer imageMaxSizeMb = fileConfig.getImageMaxSizeMb();
        if (fileTypeHelper.isImage(originalFilename) && size > imageMaxSizeMb * 1024 * 1024) {
            return R.warn("上传图片不能超过 {}Mb", imageMaxSizeMb);
        }
        Integer videoMaxSizeMb = fileConfig.getVideoMaxSizeMb();
        if (fileTypeHelper.isVideo(originalFilename) && size > videoMaxSizeMb * 1024 * 1024) {
            return R.warn("上传视频不能超过 {}Mb", videoMaxSizeMb);
        }
        Integer maxSizeMb = fileConfig.getMaxSizeMb();
        if (!fileTypeHelper.isImage(originalFilename) && !fileTypeHelper.isVideo(originalFilename) && size > maxSizeMb * 1024 * 1024) {
            return R.warn("上传文件不能超过 {}Mb", maxSizeMb);
        }

        if (!fileTypeHelper.validateFileContent(file)) {
            return R.warn("文件内容与扩展名不匹配，可能为伪装文件！");
        }

        log.info("{} --> {}", originalFilename, size);

        // 上传的附件地址
        MdmFileRecordDto dto =
            isPublic ?
                    fileUploadApi.uploadPublic(file, businessType, bucket) :
                    fileUploadApi.upload(file, businessType, bucket);

        MdmFileRecord f = new MdmFileRecord();
        f.setTenantCode(SessionHelper.getTenantCode());
        f.setBusinessType(businessType);
        f.setFileSize(file.getSize());
        f.setFileName(originalFilename);
        f.setFileType(fileType);
        f.setOssSp(dto.getOssSp());
        f.setFileId(dto.getFileId());
        f.setBucket(dto.getBucket());

        try {
            mdmFileRecordService.insert(f);
        } catch (Exception e) {
            log.error("文件上传到OSS成功但数据库记录插入失败，请手动清理OSS文件: fileId={}, bucket={}", dto.getFileId(), dto.getBucket(), e);
            throw e;
        }

        dto.setFileName(originalFilename);
        dto.setFileSize(f.getFileSize());
        dto.setFileType(fileType);
        return R.ok(dto);
    }

}
