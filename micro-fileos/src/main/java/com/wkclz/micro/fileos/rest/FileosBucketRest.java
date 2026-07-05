package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.req.*;
import com.wkclz.micro.fileos.bean.resp.BucketResp;
import com.wkclz.micro.fileos.service.MdmFileosBucketService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Route.PREFIX)
@Tag(name = "Bucket管理")
@Validated
public class FileosBucketRest {

    @Autowired
    private MdmFileosBucketService mdmFileosBucketService;

    @GetMapping(Route.BUCKET_PAGE)
    @Operation(summary = "Bucket分页查询")
    public R<PageData<BucketResp>> page(@Valid BucketPageReq req) {
        MdmFileosBucket entity = BeanUtil.cp(req, MdmFileosBucket.class);
        PageData<MdmFileosBucket> page = mdmFileosBucketService.getBucketPage(entity);
        PageData<BucketResp> convert = page.convert(BucketResp.class);
        for (BucketResp record : convert.getRecords()) {
            if (StringUtils.isNotBlank(record.getSecretKey())) {
                record.setSecretKey("******");
            }
        }
        return R.ok(convert);
    }

    @GetMapping(Route.BUCKET_INFO)
    @Operation(summary = "Bucket详情")
    public R<BucketResp> info(@Valid BucketInfoReq req) {
        MdmFileosBucket entity = mdmFileosBucketService.selectById(req.getId());
        if (entity == null) {
            return R.ok();
        }
        if (StringUtils.isNotBlank(entity.getSecretKey())) {
            entity.setSecretKey("******");
        }
        BucketResp resp = BeanUtil.cp(entity, BucketResp.class);
        return R.ok(resp);
    }

    @PostMapping(Route.BUCKET_CREATE)
    @Operation(summary = "Bucket创建")
    public R<BucketResp> create(@Valid @RequestBody BucketCreateReq req) {
        MdmFileosBucket entity = BeanUtil.cp(req, MdmFileosBucket.class);
        if (entity.getDefaultFlag() != null && entity.getDefaultFlag() == 1) {
            mdmFileosBucketService.setDefaultFlag(entity);
        }
        mdmFileosBucketService.insert(entity);
        BucketResp resp = BeanUtil.cp(entity, BucketResp.class);
        if (StringUtils.isNotBlank(entity.getSecretKey())) {
            entity.setSecretKey("******");
        }
        return R.ok(resp);
    }

    @PostMapping(Route.BUCKET_UPDATE)
    @Operation(summary = "Bucket修改")
    public R<BucketResp> update(@Valid @RequestBody BucketUpdateReq req) {
        MdmFileosBucket entity = BeanUtil.cp(req, MdmFileosBucket.class);
        if ("******".equals(entity.getSecretKey()) || StringUtils.isBlank(entity.getSecretKey())) {
            entity.setSecretKey(null);
        }
        if (entity.getDefaultFlag() != null && entity.getDefaultFlag() == 1) {
            mdmFileosBucketService.setDefaultFlag(entity);
        }
        mdmFileosBucketService.updateByIdSelective(entity);
        mdmFileosBucketService.clearCache();
        BucketResp resp = BeanUtil.cp(entity, BucketResp.class);
        return R.ok(resp);
    }

    @PostMapping(Route.BUCKET_REMOVE)
    @Operation(summary = "Bucket删除")
    public R<Void> remove(@Valid @RequestBody BucketRemoveReq req) {
        MdmFileosBucket bucket = mdmFileosBucketService.selectById(req.getId());
        if (bucket == null) {
            return R.error("记录不存在");
        }
        if (bucket.getDefaultFlag() != null && bucket.getDefaultFlag() == 1) {
            return R.error("默认Bucket不能删除");
        }
        mdmFileosBucketService.deleteById(bucket.getId());
        mdmFileosBucketService.clearCache();
        return R.ok();
    }

    @GetMapping(Route.BUCKET_OPTIONS)
    @Operation(summary = "Bucket选项")
    public R<List<BucketResp>> options(@Valid BucketOptionsReq req) {
        MdmFileosBucket entity = BeanUtil.cp(req, MdmFileosBucket.class);
        List<MdmFileosBucket> list = mdmFileosBucketService.getBucketOptions(entity);
        List<BucketResp> cp = BeanUtil.cp(list, BucketResp.class);
        return R.ok(cp);
    }

}
