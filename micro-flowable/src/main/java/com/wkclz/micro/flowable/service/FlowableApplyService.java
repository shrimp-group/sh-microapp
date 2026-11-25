package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.resp.ApplyInfoResp;
import com.wkclz.micro.flowable.bean.resp.ApplyPageResp;
import com.wkclz.micro.flowable.mapper.FlowableApplyMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class FlowableApplyService extends BaseService<FlowableApply, FlowableApplyMapper> {

    public PageData<ApplyPageResp> getApplyPage(FlowableApply entity) {
        return PageQuery.page(entity, mapper::getApplyPage);
    }

    public ApplyInfoResp selectInfoById(Long id) {
        return mapper.selectInfoById(id);
    }

    public ApplyPageResp selectByProcInsIdWithDesign(String procInsId) {
        if (procInsId == null || procInsId.isEmpty()) {
            return null;
        }
        return mapper.selectByProcInsIdWithDesign(procInsId);
    }

    public List<ApplyPageResp> selectByProcInsIdsWithDesign(Collection<String> procInsIds) {
        if (procInsIds == null || procInsIds.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectByProcInsIdsWithDesign(procInsIds);
    }

    // 按申请单号批量查询，入参为空返回空列表
    public List<FlowableApply> selectByApplyCodes(Collection<String> applyCodes) {
        if (applyCodes == null || applyCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectByApplyCodes(applyCodes);
    }

    // 按流程实例 ID 查询申请单，procInsId 为空返回 null
    public FlowableApply selectByProcInsId(String procInsId) {
        if (procInsId == null || procInsId.isEmpty()) {
            return null;
        }
        FlowableApply param = new FlowableApply();
        param.setProcInsId(procInsId);
        return selectOneByEntity(param);
    }

    /**
     * 按流程实例 ID 更新申请单状态（乐观锁），返回更新前查到的申请单，未找到返回 null
     */
    public FlowableApply updateStatusByProcInsId(String procInsId, String status) {
        log.info("按流程实例更新申请单状态: procInsId={}, status={}", procInsId, status);
        FlowableApply apply = selectByProcInsId(procInsId);
        if (apply == null) {
            return null;
        }
        FlowableApply update = new FlowableApply();
        update.setId(apply.getId());
        update.setStatus(status);
        update.setVersion(apply.getVersion());
        updateByIdSelective(update);
        return apply;
    }
}
