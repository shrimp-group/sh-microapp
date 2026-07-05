package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosDeleteApi;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.bean.req.RecordInfoReq;
import com.wkclz.micro.fileos.bean.req.RecordPageReq;
import com.wkclz.micro.fileos.bean.req.RecordRemoveReq;
import com.wkclz.micro.fileos.bean.resp.RecordResp;
import com.wkclz.micro.fileos.service.MdmFileosRecordService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Tag(name = "文件记录")
@Validated
public class FileosRecordRest {

    @Autowired
    private FileosSignApi fileosSignApi;
    @Autowired
    private FileosDeleteApi fileosDeleteApi;
    @Autowired
    private MdmFileosRecordService mdmFileosRecordService;

    @GetMapping(Route.RECORD_PAGE)
    @Operation(summary = "文件记录分页查询")
    public R<PageData<RecordResp>> page(@Valid RecordPageReq req) {
        MdmFileosRecord entity = BeanUtil.cp(req, MdmFileosRecord.class);
        PageData<MdmFileosRecord> page = mdmFileosRecordService.getRecordPage(entity);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return R.ok(PageData.convert(page, List.of()));
        }
        PageData<RecordResp> convert = page.convert(RecordResp.class);
        fileosSignApi.sign(convert.getRecords(), RecordResp::getFileId, RecordResp::setPreviewUrl);
        return R.ok(convert);
    }

    @GetMapping(Route.RECORD_INFO)
    @Operation(summary = "文件记录详情")
    public R<RecordResp> info(@Valid RecordInfoReq req) {
        MdmFileosRecord entity = mdmFileosRecordService.selectById(req.getId());
        if (entity == null) {
            return R.error("记录不存在");
        }
        RecordResp resp = BeanUtil.cp(entity, RecordResp.class);
        if (StringUtils.isNotBlank(resp.getFileId())) {
            resp.setPreviewUrl(fileosSignApi.sign(resp.getFileId()));
        }
        return R.ok(resp);
    }

    @PostMapping(Route.RECORD_REMOVE)
    @Operation(summary = "文件记录删除")
    public R<Void> remove(@Valid @RequestBody RecordRemoveReq req) {
        MdmFileosRecord record = mdmFileosRecordService.selectById(req.getId());
        if (req.getId() == null) {
            return R.error("文件不存在！");
        }
        log.info("删除文件: fileId={}, fileName={}", record.getFileId(), record.getFileName());
        mdmFileosRecordService.deleteById(req.getId());
        fileosDeleteApi.delete(record.getFileId());
        return R.ok();
    }

}
