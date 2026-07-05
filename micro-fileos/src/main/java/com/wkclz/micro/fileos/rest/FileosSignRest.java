package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.fileos.bean.req.SignUrlReq;
import com.wkclz.micro.fileos.bean.req.SignUrlsReq;
import com.wkclz.micro.fileos.bean.resp.SignUrlResp;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(Route.PREFIX)
@Tag(name = "文件签名")
@Validated
public class FileosSignRest {

    @Autowired
    private FileosSignApi fileosSignApi;

    @GetMapping(Route.SIGN_URL)
    @Operation(summary = "生成签名URL")
    public R<SignUrlResp> signUrl(@Valid SignUrlReq req) {
        String url;
        if (req.getExpireMinutes() != null && req.getExpireMinutes() > 0) {
            url = fileosSignApi.sign(req.getFileId(), req.getExpireMinutes(), TimeUnit.MINUTES);
        } else {
            url = fileosSignApi.sign(req.getFileId());
        }
        SignUrlResp resp = new SignUrlResp();
        resp.setUrl(url);
        return R.ok(resp);
    }

    @PostMapping(Route.SIGN_URLS)
    @Operation(summary = "批量生成签名URL")
    public R<List<SignUrlResp>> signUrls(@Valid @RequestBody SignUrlsReq req) {
        List<String> urls;
        if (req.getExpireMinutes() != null && req.getExpireMinutes() > 0) {
            urls = fileosSignApi.sign(req.getFileIds(), req.getExpireMinutes(), TimeUnit.MINUTES);
        } else {
            urls = fileosSignApi.sign(req.getFileIds());
        }
        List<SignUrlResp> cp = BeanUtil.cp(urls, SignUrlResp.class);
        return R.ok(cp);
    }
}
