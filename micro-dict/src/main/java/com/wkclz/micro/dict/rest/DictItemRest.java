package com.wkclz.micro.dict.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dict.bean.dto.MdmDictDto;
import com.wkclz.micro.dict.bean.entity.MdmDictItem;
import com.wkclz.micro.dict.bean.req.DictItemListReq;
import com.wkclz.micro.dict.bean.req.DictItemSaveReq;
import com.wkclz.micro.dict.bean.resp.DictItemResp;
import com.wkclz.micro.dict.service.MdmDictItemService;
import com.wkclz.tool.utils.BeanUtil;
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
@Tag(name = "2.字典项", description = "字典项管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class DictItemRest {

    @Autowired
    private MdmDictItemService mdmDictItemService;

    @Operation(summary = "1.字典项-列表", description = "根据字典类型查询字典项列表（不分页）")
    @GetMapping(Route.DICT_ITEM_LIST)
    public R<List<DictItemResp>> dictItemList(@Valid DictItemListReq req) {
        String dictType = req.getDictType();
        List<MdmDictItem> dictItemList = mdmDictItemService.getDictItemList(dictType);
        List<DictItemResp> respList = BeanUtil.cp(dictItemList, DictItemResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.字典项-保存", description = "批量保存字典项（diff模式）")
    @PostMapping(Route.DICT_ITEM_SAVE)
    public R<Integer> dictItemSave(@Valid @RequestBody DictItemSaveReq req) {
        MdmDictDto dto = new MdmDictDto();
        dto.setDictType(req.getDictType());
        if (req.getItems() != null) {
            List<MdmDictItem> items = BeanUtil.cp(req.getItems(), MdmDictItem.class);
            dto.setItems(items);
        } else {
            dto.setItems(new ArrayList<>());
        }
        Integer modifys = mdmDictItemService.dictItemSave(dto);
        return R.ok(modifys);
    }

}
