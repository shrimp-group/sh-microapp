package com.wkclz.micro.seq.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.seq.bean.entity.MdmSequence;
import com.wkclz.micro.seq.bean.req.SequenceInfoReq;
import com.wkclz.micro.seq.bean.req.SequencePageReq;
import com.wkclz.micro.seq.bean.req.SequenceUpdateReq;
import com.wkclz.micro.seq.bean.resp.SequencePageResp;
import com.wkclz.micro.seq.bean.resp.SequenceResp;
import com.wkclz.micro.seq.service.MdmSequenceService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_sequence (序列生成) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.序列生成", description = "序列生成管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class SequenceRest {

    @Autowired
    private MdmSequenceService mdmSequenceService;

    @Operation(summary = "1.序列生成-分页查询", description = "根据条件分页查询序列生成列表")
    @GetMapping(Route.SEQUENCE_PAGE)
    public R<PageData<SequencePageResp>> mdmSequencePage(@Valid SequencePageReq req) {
        MdmSequence entity = BeanUtil.cp(req, MdmSequence.class);
        PageData<MdmSequence> page = mdmSequenceService.getSequencePage(entity);
        PageData<SequencePageResp> newPage = page.convert(SequencePageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.序列生成-详情", description = "根据ID查询序列生成详情")
    @GetMapping(Route.SEQUENCE_INFO)
    public R<SequenceResp> mdmSequenceInfo(@Valid SequenceInfoReq req) {
        MdmSequence entity = mdmSequenceService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        SequenceResp resp = BeanUtil.cp(entity, SequenceResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.序列生成-修改", description = "修改序列生成信息")
    @PostMapping(Route.SEQUENCE_UPDATE)
    public R<SequenceResp> mdmSequenceUpdate(@Valid @RequestBody SequenceUpdateReq req) {
        MdmSequence entity = BeanUtil.cp(req, MdmSequence.class);
        entity = mdmSequenceService.update(entity);
        SequenceResp resp = BeanUtil.cp(entity, SequenceResp.class);
        return R.ok(resp);
    }

}
