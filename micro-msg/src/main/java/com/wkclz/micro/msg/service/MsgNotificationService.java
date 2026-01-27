package com.wkclz.micro.msg.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.msg.dao.MsgNotificationMapper;
import com.wkclz.micro.msg.dao.MsgUserRecordMapper;
import com.wkclz.micro.msg.pojo.dto.MsgNotificationDto;
import com.wkclz.micro.msg.pojo.entity.MsgNotification;
import com.wkclz.micro.msg.pojo.entity.MsgUserRecord;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_notification (消息通知) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 msg_notification 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MsgNotificationService extends BaseService<MsgNotification, MsgNotificationMapper> {

    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private MsgUserRecordMapper msgUserRecordMapper;

    public PageData<MsgNotificationDto> getNotificationPage(MsgNotificationDto dto) {
        return PageQuery.page(dto, mapper::getNotificationList);
    }


    public Integer createNotification(MsgNotificationDto dto) {
        List<String> users = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getSentToUser())) {
            users.add(dto.getSentToUser());
        }
        if (CollectionUtils.isNotEmpty(dto.getSentToUsers())) {
            users.addAll(dto.getSentToUsers());
        }
        users = users.stream().distinct().toList();
        MsgNotification notice = new MsgNotification();

        notice.setNoticeNo(redisIdGenerator.generateIdWithPrefix("msg_"));
        notice.setUserCode(dto.getUserCode());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setExtUrl(dto.getExtUrl());
        mapper.insert(notice);

        List<MsgUserRecord> records = users.stream().map(t -> {
            MsgUserRecord record = new MsgUserRecord();
            record.setUserCode(t);
            record.setNoticeNo(notice.getNoticeNo());
            record.setReadStatus(0);
            record.setShowTimes(0);
            return record;
        }).toList();
        msgUserRecordMapper.insertBatch(records);
        return 1;
    }

    public MsgNotification update(MsgNotification entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MsgNotification oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MsgNotification.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

}

