package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fileos.api.FileosDeleteApi;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.fileos.bean.dto.MdmFileosRecordDto;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.service.MdmFileosRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class FileosRecordRest {

    @Autowired
    private FileosSignApi fileosSignApi;
    @Autowired
    private FileosDeleteApi fileosDeleteApi;
    @Autowired
    private MdmFileosRecordService mdmFileosRecordService;

    @GetMapping(Route.RECORD_PAGE)
    public R<PageData<MdmFileosRecordDto>> page(MdmFileosRecord entity) {
        PageData<MdmFileosRecord> page = mdmFileosRecordService.getRecordPage(entity);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return R.ok(PageData.convert(page, List.of()));
        }
        List<MdmFileosRecordDto> dtos = page.getRecords().stream()
            .map(MdmFileosRecordDto::copy)
            .collect(Collectors.toList());
        fileosSignApi.sign(dtos, MdmFileosRecordDto::getFileId, MdmFileosRecordDto::setPreviewUrl);
        PageData<MdmFileosRecordDto> newPage = PageData.convert(page, dtos);
        return R.ok(newPage);
    }

    @GetMapping(Route.RECORD_INFO)
    public R<MdmFileosRecord> info(@RequestParam Long id) {
        MdmFileosRecord entity = new MdmFileosRecord();
        entity.setId(id);
        entity = mdmFileosRecordService.selectById(id);
        if (entity == null) {
            return R.error(ResultCode.NOT_FOUND.getMessage());
        }
        MdmFileosRecordDto dto = MdmFileosRecordDto.copy(entity);
        if (StringUtils.isNotBlank(dto.getFileId())) {
            dto.setPreviewUrl(fileosSignApi.sign(dto.getFileId()));
        }
        return R.ok(dto);
    }

    @PostMapping(Route.RECORD_REMOVE)
    public R<?> remove(@RequestBody MdmFileosRecord entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        MdmFileosRecord record = mdmFileosRecordService.selectById(entity.getId());
        if (record != null && record.getFileId() != null) {
            log.info("删除文件: fileId={}, fileName={}", record.getFileId(), record.getFileName());
            fileosDeleteApi.delete(record.getFileId());
        }
        mdmFileosRecordService.deleteById(entity.getId());
        return R.ok();
    }

}
