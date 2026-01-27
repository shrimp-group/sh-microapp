package com.wkclz.micro.msg.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.msg.dao.MsgUserRecordMapper;
import com.wkclz.micro.msg.pojo.dto.MsgUserRecordDto;
import com.wkclz.micro.msg.pojo.entity.MsgUserRecord;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_record (用户消息记录) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 msg_user_record 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MsgUserRecordService extends BaseService<MsgUserRecord, MsgUserRecordMapper> {



    public List<MsgUserRecordDto> getPersonalRecordList(MsgUserRecordDto dto) {
        return mapper.getPersonalRecordList(dto);
    }

    public PageData<MsgUserRecordDto> getPersonalRecordPage(MsgUserRecordDto dto) {
        return PageQuery.page(dto, mapper::getPersonalRecordList4Page);
    }

    public MsgUserRecordDto getNotice(String noticeNo) {
        if (StringUtils.isBlank(noticeNo)) {
            return null;
        }
        MsgUserRecordDto param = new MsgUserRecordDto();
        param.setNoticeNo(noticeNo);
        param.setUserCode(SessionHelper.getUserCode());
        MsgUserRecordDto info = mapper.getNoticeInfo(param);
        if (info == null) {
            throw ValidationException.of("消息不存在！");
        }
        // 阅读 + 1
        mapper.updateShowTimes(info.getId());
        return info;
    }

    public Integer userMarkRecodeReaded(MsgUserRecord entity) {
        List<Long> ids = new ArrayList<>();
        if (entity.getId() != null) {
            ids.add(entity.getId());
        }
        if (CollectionUtils.isNotEmpty(entity.getIds())) {
            ids.addAll(entity.getIds());
        }
        if (CollectionUtils.isEmpty(ids)) {
            throw ValidationException.of("没有可以操作的数据！");
        }

        MsgUserRecord param = new MsgUserRecord();
        param.setIds(ids);
        param.setUserCode(SessionHelper.getUserCode());
        return mapper.markRecodeAsReaded(param);
    }


}

