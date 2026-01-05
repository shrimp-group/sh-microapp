package com.wkclz.micro.wxapp.rest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.wkclz.common.entity.Result;
import com.wkclz.micro.wxapp.config.WxMaConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * <pre>
 *  小程序临时素材接口
 *  Created by BinaryWang on 2017/6/16.
 * </pre>
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
public class WxMaMediaRest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WxMaConfiguration configuration;


    /**
     * @api {get} /customer/wx/media/upload 6. customer-上传临时素材
     * @apiGroup WX
     *
     * @apiVersion 0.0.1
     *
     * @apiParam {String} appid <code>PathVariable</code>appid
     *
     * @apiParamExample {json} 请求样例:
     * /customer/wx/portal/xxxxxxxxxxxx
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *      "code": 1,
     *      "data": [
     *          "mediaId1",
     *          "mediaId2"
     *      ]
     * }
     */
    @PostMapping(Routes.CUSTOMER_WX_MEDIA_UPLOAD)
    public Result uploadMedia(HttpServletRequest request) throws WxErrorException {
        final WxMaService wxService = configuration.getMaService();

        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        if (!resolver.isMultipart(request)) {
            return Result.data(Lists.newArrayList());
        }

        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);


        Iterator<String> it = multiRequest.getFileNames();
        List<String> result = Lists.newArrayList();
        while (it.hasNext()) {
            try {
                MultipartFile file = multiRequest.getFile(it.next());
                File newFile = new File(Files.createTempDir(), file.getOriginalFilename());
                this.logger.info("filePath is ：" + newFile.toString());
                file.transferTo(newFile);
                WxMediaUploadResult uploadResult = wxService.getMediaService().uploadMedia(WxMaConstants.KefuMsgType.IMAGE, newFile);
                this.logger.info("media_id ： " + uploadResult.getMediaId());
                result.add(uploadResult.getMediaId());
            } catch (IOException e) {
                this.logger.error(e.getMessage(), e);
            }
        }
        return Result.data(result);
    }

    /**
     * @api {get} /customer/wx/media/download/{mediaId} 7. customer-下载临时素材
     * @apiGroup WX
     *
     * @apiVersion 0.0.1
     *
     * @apiParam {String} appid <code>PathVariable</code>appid
     * @apiParam {String} mediaId <code>PathVariable</code>素材ID
     *
     * @apiParamExample {json} 请求样例:
     * /customer/wx/media/appidappidappidappid/download/mediaIdmediaIdmediaIdmediaId
     *
     * @apiSuccessExample {json} 返回样例:
     * File
     */
    @GetMapping(Routes.CUSTOMER_WX_MEDIA_DOWNLOAD)
    public File getMedia(@PathVariable String mediaId) throws WxErrorException {
        final WxMaService wxService = configuration.getMaService();
        return wxService.getMediaService().getMedia(mediaId);
    }
}
