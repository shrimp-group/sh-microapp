package com.wkclz.micro.wxapp.rest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.wkclz.core.base.R;
import com.wkclz.micro.wxapp.Route;
import com.wkclz.micro.wxapp.config.WxMaConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
@Tag(name = "小程序素材", description = "微信小程序临时素材管理接口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxMaMediaRest {

    @Autowired
    private WxMaConfiguration configuration;


    @Operation(summary = "1. 上传临时素材")
    @PostMapping(Route.CUSTOMER_WX_MEDIA_UPLOAD)
    public R<List<String>> uploadMedia(HttpServletRequest request) throws WxErrorException {
        final WxMaService wxService = configuration.getMaService();

        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        if (!resolver.isMultipart(request)) {
            return R.ok(Lists.newArrayList());
        }

        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);


        Iterator<String> it = multiRequest.getFileNames();
        List<String> result = Lists.newArrayList();
        while (it.hasNext()) {
            try {
                MultipartFile file = multiRequest.getFile(it.next());
                File newFile = new File(Files.createTempDir(), file.getOriginalFilename());
                log.info("filePath is ：" + newFile.toString());
                file.transferTo(newFile);
                WxMediaUploadResult uploadResult = wxService.getMediaService().uploadMedia(WxMaConstants.KefuMsgType.IMAGE, newFile);
                log.info("media_id ： " + uploadResult.getMediaId());
                result.add(uploadResult.getMediaId());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return R.ok(result);
    }

    @Operation(summary = "2. 下载临时素材")
    @GetMapping(Route.CUSTOMER_WX_MEDIA_DOWNLOAD)
    public File getMedia(@PathVariable String mediaId) throws WxErrorException {
        final WxMaService wxService = configuration.getMaService();
        return wxService.getMediaService().getMedia(mediaId);
    }
}
