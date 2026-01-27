package com.wkclz.micro.wxmp.service;

import com.wkclz.micro.wxmp.dao.WxmpUserMapper;
import com.wkclz.micro.wxmp.pojo.entity.WxmpUser;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_user (微信用户) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 wxmp_user 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class WxmpUserService extends BaseService<WxmpUser, WxmpUserMapper> {
    @Autowired
    private RedisIdGenerator redisIdGenerator;

    public WxmpUser initUser(WxmpUser user) {
        String openId = user.getOpenId();

        WxmpUser u = mapper.getWxmpUserByOpenId(openId);
        if (u == null) {
            u = user;
            u.setUserCode(redisIdGenerator.generateIdWithPrefix("wxmp_"));
            u.setLoginTimes(1);
            insert(u);
        } else {
            if (user.getAppId() != null && !user.getAppId().equals(u.getAppId())) {
                u.setAppId(user.getAppId());
            }
            // user 与 不一样的地方，需要更新到 u。只有在出现不一样的值的时候，才更新到数据库
            if (user.getUnionId() != null && !user.getUnionId().equals(u.getUnionId())) {
                u.setUnionId(user.getUnionId());
            }
            if (user.getMobile() != null && !user.getMobile().equals(u.getMobile())) {
                u.setMobile(user.getMobile());
            }
            if (user.getNickname() != null && !user.getNickname().equals(u.getNickname())) {
                u.setNickname(user.getNickname());
            }
            if (user.getEmail() != null && !user.getEmail().equals(u.getEmail())) {
                u.setEmail(user.getEmail());
            }
            if (user.getGender() != null && !user.getGender().equals(u.getGender())) {
                u.setGender(user.getGender());
            }
            if (user.getAvatar() != null && !user.getAvatar().equals(u.getAvatar())) {
                u.setAvatar(user.getAvatar());
            }
            if (user.getCountry() != null && !user.getCountry().equals(u.getCountry())) {
                u.setCountry(user.getCountry());
            }
            if (user.getProvince() != null && !user.getProvince().equals(u.getProvince())) {
                u.setProvince(user.getProvince());
            }
            if (user.getCity() != null && !user.getCity().equals(u.getCity())) {
                u.setCity(user.getCity());
            }
            if (user.getPrivilegeList() != null && !user.getPrivilegeList().equals(u.getPrivilegeList())) {
                u.setPrivilegeList(user.getPrivilegeList());
            }

            u.setLoginTimes(u.getLoginTimes() + 1);
            updateByIdSelective(u);
        }
        return u;
    }


    public void userSubscribe(WxmpUser user) {
        WxmpUser u = mapper.getWxmpUserByOpenId(user.getOpenId());
        if (u == null) {
            u = user;
            u.setUserCode(redisIdGenerator.generateIdWithPrefix("wxmp_"));
            u.setLoginTimes(0);
            u.setSubscribeStatus(1);
            // 初始用户名
            if (StringUtils.isBlank(u.getNickname())) {
                u.setNickname(u.getUserCode());
            }
            insert(u);
        } else {
            u.setSubscribeStatus(1);
            updateByIdSelective(u);
        }
    }

    public void userUnSbscribe(String openId) {
        if (StringUtils.isBlank(openId)) {
            return;
        }
        WxmpUser u = mapper.getWxmpUserByOpenId(openId);
        if (u != null) {
            u.setSubscribeStatus(0);
            updateByIdSelective(u);
        }
    }


    public WxmpUser getUserByUserCode(String userCode) {
        return mapper.getWxmpUserByUserCode(userCode);
    }


}

