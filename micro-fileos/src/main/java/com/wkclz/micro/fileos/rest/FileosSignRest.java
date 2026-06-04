package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosSignApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(Route.PREFIX)
public class FileosSignRest {

    @Autowired
    private FileosSignApi fileosSignApi;

    @GetMapping(Route.SIGN_URL)
    public R<String> signUrl(@RequestParam String fileId, @RequestParam(required = false) Integer expireMinutes) {
        String url;
        if (expireMinutes != null && expireMinutes > 0) {
            url = fileosSignApi.sign(fileId, expireMinutes, TimeUnit.MINUTES);
        } else {
            url = fileosSignApi.sign(fileId);
        }
        return R.ok(url);
    }

    @PostMapping(Route.SIGN_URLS)
    public R<List<String>> signUrls(@RequestBody List<String> fileIds) {
        List<String> urls = fileosSignApi.sign(fileIds);
        return R.ok(urls);
    }

}
