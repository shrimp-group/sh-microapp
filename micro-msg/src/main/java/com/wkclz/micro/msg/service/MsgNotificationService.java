package com.wkclz.micro.msg.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.msg.bean.dto.MsgNotificationDto;
import com.wkclz.micro.msg.bean.entity.MsgNotification;
import com.wkclz.micro.msg.bean.entity.MsgUserRecord;
import com.wkclz.micro.msg.mapper.MsgNotificationMapper;
import com.wkclz.micro.msg.mapper.MsgUserRecordMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_notification (消息通知) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 msg_notification 的逻辑. 其他逻辑放 custom 中
 */

@Slf4j
@Service
public class MsgNotificationService extends BaseService<MsgNotification, MsgNotificationMapper> {

    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private MsgUserRecordMapper msgUserRecordMapper;

    public PageData<MsgNotificationDto> getNotificationPage(MsgNotificationDto dto) {
        return PageQuery.page(dto, mapper::getNotificationList);
    }


    /**
     * 创建并发布消息通知：生成通知单号落库后，为去重后的目标用户批量生成未读消息记录
     */
    public Integer createNotification(MsgNotificationDto dto) {
        log.info("消息通知发布, dto={}", dto);
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
        log.info("消息通知记录生成, noticeNo={}, userCount={}", notice.getNoticeNo(), records.size());
        msgUserRecordMapper.insertBatch(records);
        return 1;
    }

    public MsgNotification update(MsgNotification entity) {
        log.info("消息通知修改, entity={}", entity);
        MsgNotification oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MsgNotification.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

}

