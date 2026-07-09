package com.wkclz.micro.dict.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dict.bean.dto.MdmDictDto;
import com.wkclz.micro.dict.bean.entity.MdmDict;
import com.wkclz.micro.dict.bean.entity.MdmDictItem;
import com.wkclz.micro.dict.bean.req.*;
import com.wkclz.micro.dict.bean.resp.*;
import com.wkclz.micro.dict.service.MdmDictService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: wangkaicun @ 2018-10-30 15:11:51
 */
@Tag(name = "1.字典类型", description = "字典类型管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class DictRest {

    @Autowired
    private MdmDictService mdmDictService;

    @Operation(summary = "1.字典类型-分页查询", description = "根据条件分页查询字典类型列表")
    @GetMapping(Route.DICT_PAGE)
    public R<PageData<DictPageResp>> dictPage(@Valid DictPageReq req) {
        MdmDict entity = BeanUtil.cp(req, MdmDict.class);
        PageData<MdmDict> page = mdmDictService.getDictPage(entity);
        PageData<DictPageResp> newPage = page.convert(DictPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.字典类型-详情", description = "根据ID查询字典类型详情")
    @GetMapping(Route.DICT_INFO)
    public R<DictResp> dictInfo(@Valid DictInfoReq req) {
        MdmDict entity = mdmDictService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        DictResp resp = BeanUtil.cp(entity, DictResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.字典类型-创建", description = "新增字典类型")
    @PostMapping(Route.DICT_CREATE)
    public R<DictResp> dictCreate(@RequestBody DictCreateReq req) {
        MdmDict entity = BeanUtil.cp(req, MdmDict.class);
        entity = mdmDictService.dictCreate(entity);
        DictResp resp = BeanUtil.cp(entity, DictResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.字典类型-修改", description = "修改字典类型")
    @PostMapping(Route.DICT_UPDATE)
    public R<DictResp> dictUpdate(@RequestBody DictUpdateReq req) {
        MdmDict entity = BeanUtil.cp(req, MdmDict.class);
        entity = mdmDictService.dictUpdate(entity);
        DictResp resp = BeanUtil.cp(entity, DictResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.字典类型-删除", description = "删除字典类型")
    @PostMapping(Route.DICT_REMOVE)
    public R<Integer> dictRemove(@RequestBody RemoveReq req) {
        Integer rt = mdmDictService.dictRemove(req.getId());
        return R.ok(rt);
    }

    @Operation(summary = "6.字典-复制", description = "复制字典数据为JSON")
    @GetMapping(Route.DICT_COPY)
    public R<List<DictCopyResp>> dictCopy(@Valid DictCopyReq req) {
        MdmDictDto dto = BeanUtil.cp(req, MdmDictDto.class);
        List<MdmDictDto> copyList = mdmDictService.copy(dto);
        List<DictCopyResp> respList = copyList.stream().map(d -> {
            DictCopyResp resp = BeanUtil.cp(d, DictCopyResp.class);
            if (d.getItems() != null) {
                List<DictItemResp> itemResps = BeanUtil.cp(d.getItems(), DictItemResp.class);
                resp.setItems(itemResps);
            }
            return resp;
        }).toList();
        return R.ok(respList);
    }

    @Operation(summary = "7.字典-粘贴", description = "粘贴导入字典数据")
    @PostMapping(Route.DICT_PASTE)
    public R<Integer> dictPaste(@Valid @RequestBody DictPasteReq req) {
        if (req.getList() == null || req.getList().isEmpty()) {
            return R.error("没有可制作的数据！");
        }
        List<MdmDictDto> dtoList = new ArrayList<>();
        for (DictPasteReq.DictPasteItem item : req.getList()) {
            MdmDictDto dto = BeanUtil.cp(item, MdmDictDto.class);
            if (item.getItems() != null) {
                List<MdmDictItem> items = item.getItems().stream().map(i -> BeanUtil.cp(i, MdmDictItem.class)).toList();
                dto.setItems(items);
            }
            dtoList.add(dto);
        }
        Integer paste = mdmDictService.paste(dtoList);
        return R.ok(paste);
    }

    @Operation(summary = "8.字典类型-选项", description = "获取所有字典类型的选项列表")
    @GetMapping(Route.DICT_OPTIONS)
    public R<List<DictOptionsResp>> dictOptions() {
        List<MdmDict> dicts = mdmDictService.dictOptions();
        List<DictOptionsResp> respList = BeanUtil.cp(dicts, DictOptionsResp.class);
        return R.ok(respList);
    }

}
