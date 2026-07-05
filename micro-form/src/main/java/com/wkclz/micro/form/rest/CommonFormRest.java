package com.wkclz.micro.form.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.form.bean.dto.MdmFormDto;
import com.wkclz.micro.form.bean.entity.MdmForm;
import com.wkclz.micro.form.bean.req.CommonFormInfoReq;
import com.wkclz.micro.form.bean.req.CommonFormListReq;
import com.wkclz.micro.form.bean.resp.CommonFormResp;
import com.wkclz.micro.form.bean.resp.MdmFormResp;
import com.wkclz.micro.form.service.MdmFormService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "5.通用表单", description = "通用表单查询接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class CommonFormRest {

    @Autowired
    private MdmFormService mdmFormService;

    @Operation(summary = "11.通用表单-列表", description = "获取表单选项列表，用于生成下拉选项")
    @GetMapping(Route.COMMON_FORM_LIST)
    public R<List<CommonFormResp>> commonFormList(@Valid CommonFormListReq req) {
        List<MdmForm> list = mdmFormService.getFormOptions();
        List<CommonFormResp> respList = BeanUtil.cp(list, CommonFormResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "12.通用表单-详情", description = "根据表单编码获取表单详情，用于构造输入表单")
    @GetMapping(Route.COMMON_FORM_DETAIL)
    public R<CommonFormResp> commonFormInfo(@Valid CommonFormInfoReq req) {
        MdmForm entity = new MdmForm();
        entity.setFormCode(req.getFormCode());
        MdmFormDto dto = mdmFormService.getCustomFormDetail(entity);
        CommonFormResp resp = BeanUtil.cp(dto, CommonFormResp.class);
        if (dto.getItems() != null) {
            List<MdmFormResp.MdmFormItemResp> itemResps = BeanUtil.cp(dto.getItems(), MdmFormResp.MdmFormItemResp.class);
            resp.setItems(itemResps);
        }
        return R.ok(resp);
    }

}
