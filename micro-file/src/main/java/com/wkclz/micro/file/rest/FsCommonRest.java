package com.wkclz.micro.file.rest;

import com.wkclz.core.base.R;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.api.FsApi;
import com.wkclz.micro.file.config.FsConfig;
import com.wkclz.micro.file.helper.FileTypeHelper;
import com.wkclz.micro.file.pojo.dto.MdmFsFilesDto;
import com.wkclz.micro.file.pojo.entity.MdmFsFiles;
import com.wkclz.micro.file.service.MdmFsFilesService;
import com.wkclz.tool.tools.RegularTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RestController
@RequestMapping(Route.PREFIX)
public class FsCommonRest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FsApi fsApi;
    @Autowired
    private FsConfig fsConfig;
    @Autowired
    private FileTypeHelper fileTypeHelper;
    @Autowired
    private MdmFsFilesService mdmFsFilesService;


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
     * @apiParam {String} [busnessType] <code>param</code>业务类型,如：个人头像,可传 personal_avatar
     * @apiParam {String} [fileName] <code>param</code>文件名。可覆盖默认文件名
     * @apiParam {String} file <code>body</code>文件
     *
     * @apiSuccess {String} previewUrl 上传的附件访问地址（完整url）
     * @apiSuccess {Integer} fileSize 文件大小
     * @apiSuccess {String} busnessType 业务分类
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
     *          "busnessType": "default",
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
                          String busnessType, String bucket, String fileName) {
        return upload(file, busnessType, bucket, fileName, false);
    }

    @PostMapping(Route.COMMON_UPLOAD_PUBLIC)
    public R commonUploadPublic( @RequestParam("file") MultipartFile file,
            String busnessType, String bucket, String fileName) {
        return upload(file, busnessType, bucket, fileName, true);
    }


    public R upload(
            MultipartFile file,
            String busnessType,
            String bucket,
            String fileName,
            boolean isPublic
            ) {
        if(file.isEmpty()) {
            return R.warn("file is empty");
        }
        if (StringUtils.isBlank(busnessType)) {
            return R.warn("busnessType 为业务类型，请指定以便对附件进行分类！");
        }
        if (!RegularTool.isLegalChar(busnessType)) {
            return R.warn("busnessType 包含有非法字符！");
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
        Integer imageMaxSizeMb = fsConfig.getImageMaxSizeMb();
        if (fileTypeHelper.isImage(originalFilename) && size > imageMaxSizeMb * 1024 * 1024) {
            return R.warn("上传图片不能超过 {}Mb", imageMaxSizeMb);
        }
        Integer videoMaxSizeMb = fsConfig.getVideoMaxSizeMb();
        if (fileTypeHelper.isVideo(originalFilename) && size > videoMaxSizeMb * 1024 * 1024) {
            return R.warn("上传视频不能超过 {}Mb", videoMaxSizeMb);
        }

        logger.info(file.getOriginalFilename() + "-->" + size);

        // 上传的附件地址
        MdmFsFilesDto dto =
            isPublic ?
            fsApi.uploadPublic(file, busnessType, bucket) :
            fsApi.upload(file, busnessType, bucket);

        MdmFsFiles f = new MdmFsFiles();
        f.setTenantCode(SessionHelper.getTenantCode());
        f.setBusnessType(busnessType);
        f.setFileSize(file.getSize());
        f.setFileName(originalFilename);
        f.setFileType(fileType);

        f.setOssSp(dto.getOssSp());
        f.setFileId(dto.getFileId());
        f.setBucket(dto.getBucket());
        mdmFsFilesService.insert(f);

        dto.setFileName(originalFilename);
        dto.setFileSize(f.getFileSize());
        dto.setFileType(f.getFileType());
        return R.ok(dto);
    }


}
