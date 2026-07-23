package com.wkclz.micro.wxmp.rest.cust;

import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.wxmp.bean.entity.WxmpUser;
import com.wkclz.micro.wxmp.bean.resp.WxUserResp;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpUserService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4.H5用户", description = "H5端用户信息")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxUserRest {

    @Resource
    private FileosSignApi fileosSignApi;
    @Resource
    private WxmpUserService wxmpUserService;

    @Operation(summary = "1.H5用户-基本信息", description = "获取当前登录用户基本信息")
    @GetMapping(Route.H5_MINE_USERINFO)
    public R<WxUserResp> h5MineUserinfo() {
        String userCode = IdentityContext.getUserCode();
        WxmpUser user = wxmpUserService.getUserByUserCode(userCode);
        WxUserResp resp = BeanUtil.cp(user, WxUserResp.class);
        resp.setAvatar(fileosSignApi.sign(user.getAvatar()));
        return R.ok(resp);
    }
}
