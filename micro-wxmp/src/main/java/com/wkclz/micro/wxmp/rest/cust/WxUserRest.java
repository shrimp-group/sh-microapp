package com.wkclz.micro.wxmp.rest.cust;

import com.wkclz.core.base.R;
import com.wkclz.core.base.UserInfo;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.api.FsApi;
import com.wkclz.micro.wxmp.pojo.entity.WxmpUser;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WxUserRest {

    @Resource
    private FsApi fsApi;
    @Resource
    private WxmpUserService wxmpUserService;



    /**
     * @api {get} /h5/mine/userinfo 1. H5-用户-基本信息
     * @apiGroup H5
     *
     * @apiVersion 0.0.1
     * @apiDescription 1. H5-用户-基本信息
     *
     *
     *
     * @apiSuccess {String} [nickname] 昵称
     * @apiSuccess {String} [avatar] 头像
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "nickname": "nickname",
     *          "avatar": "avatar"
     *     }
     * }
     *
     */
    @GetMapping(Route.H5_MINE_USERINFO)
    public R h5MineUserinfo() {
        String userCode = SessionHelper.getUserCode();
        WxmpUser user = wxmpUserService.getUserByUserCode(userCode);
        UserInfo ui = new UserInfo();
        ui.setUserCode(user.getUserCode());
        ui.setNickname(user.getNickname());
        ui.setAvatar(user.getAvatar());
        ui.setAvatar(fsApi.sign(user.getAvatar()));
        ui.setOpenId(user.getOpenId());
        return R.ok(ui);
    }


}
