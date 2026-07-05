package com.wkclz.micro.wxmp.rest.mamager;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.wxmp.bean.entity.WxmpKfMsg;
import com.wkclz.micro.wxmp.bean.req.WxmpKfMsgInfoReq;
import com.wkclz.micro.wxmp.bean.req.WxmpKfMsgPageReq;
import com.wkclz.micro.wxmp.bean.resp.WxmpKfMsgPageResp;
import com.wkclz.micro.wxmp.bean.resp.WxmpKfMsgResp;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpKfMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.wkclz.tool.utils.BeanUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "2.客服消息", description = "公众号客服消息管理")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxmpKfMsgRest {

    @Autowired
    private WxmpKfMsgService wxmpKfMsgService;

    @Operation(summary = "1.客服消息-分页查询", description = "分页查询客服消息列表")
    @GetMapping(Route.WXMP_KF_MSG_PAGE)
    public R<PageData<WxmpKfMsgPageResp>> wxmpKfMsgPage(@Valid WxmpKfMsgPageReq req) {
        WxmpKfMsg entity = BeanUtil.cp(req, WxmpKfMsg.class);
        PageData<WxmpKfMsgPageResp> page = wxmpKfMsgService.getKfMsgPage(entity);
        return R.ok(page);
    }

    @Operation(summary = "2.客服消息-详情", description = "根据ID查询客服消息详情")
    @GetMapping(Route.WXMP_KF_MSG_INFO)
    public R<WxmpKfMsgResp> wxmpKfMsgInfo(@Valid WxmpKfMsgInfoReq req) {
        WxmpKfMsg entity = wxmpKfMsgService.selectById(req.getId());
        WxmpKfMsgResp resp = BeanUtil.cp(entity, WxmpKfMsgResp.class);
        return R.ok(resp);
    }
}
