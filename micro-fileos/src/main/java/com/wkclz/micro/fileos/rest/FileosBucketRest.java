package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.service.MdmFileosBucketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Route.PREFIX)
public class FileosBucketRest {

    @Autowired
    private MdmFileosBucketService mdmFileosBucketService;

    @GetMapping(Route.BUCKET_PAGE)
    public R<PageData<MdmFileosBucket>> page(MdmFileosBucket entity) {
        PageData<MdmFileosBucket> page = mdmFileosBucketService.getBucketPage(entity);
        return R.ok(page);
    }

    @GetMapping(Route.BUCKET_INFO)
    public R<MdmFileosBucket> info(@RequestParam Long id) {
        MdmFileosBucket entity = mdmFileosBucketService.selectById(id);
        return R.ok(entity);
    }

    @PostMapping(Route.BUCKET_CREATE)
    public R<?> create(@RequestBody MdmFileosBucket entity) {
        if (entity.getDefaultFlag() != null && entity.getDefaultFlag() == 1) {
            mdmFileosBucketService.setDefaultFlag(entity);
        }
        mdmFileosBucketService.insert(entity);
        return R.ok(entity);
    }

    @PostMapping(Route.BUCKET_UPDATE)
    public R<?> update(@RequestBody MdmFileosBucket entity) {
        if (entity.getDefaultFlag() != null && entity.getDefaultFlag() == 1) {
            mdmFileosBucketService.setDefaultFlag(entity);
        }
        if ("******".equals(entity.getSecretKey()) || StringUtils.isBlank(entity.getSecretKey())) {
            entity.setSecretKey(null);
        }
        mdmFileosBucketService.updateByIdSelective(entity);
        mdmFileosBucketService.clearCache();
        return R.ok(entity);
    }

    @PostMapping(Route.BUCKET_REMOVE)
    public R<?> remove(@RequestBody MdmFileosBucket entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        MdmFileosBucket bucket = mdmFileosBucketService.selectById(entity.getId());
        if (bucket == null) {
            return R.error(ResultCode.RECORD_NOT_EXIST.getMessage());
        }
        if (bucket.getDefaultFlag() == 1) {
            return R.error("默认Bucket不能删除");
        }
        mdmFileosBucketService.deleteById(entity.getId());
        mdmFileosBucketService.clearCache();
        return R.ok();
    }

    @GetMapping(Route.BUCKET_OPTIONS)
    public R<List<MdmFileosBucket>> options(MdmFileosBucket entity) {
        List<MdmFileosBucket> list = mdmFileosBucketService.getBucketOptions(entity);
        return R.ok(list);
    }

}
