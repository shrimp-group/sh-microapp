package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.resp.ApplyInfoResp;
import com.wkclz.micro.flowable.bean.resp.ApplyPageResp;
import com.wkclz.micro.flowable.mapper.FlowableApplyMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class FlowableApplyService extends BaseService<FlowableApply, FlowableApplyMapper> {

    public PageData<ApplyPageResp> getApplyPage(FlowableApply entity) {
        return PageQuery.page(entity, mapper::getApplyPage);
    }

    public ApplyInfoResp selectInfoById(Long id) {
        return mapper.selectInfoById(id);
    }

    /** 按申请单号查询申请（JOIN 设计表回填 designName） */
    public ApplyPageResp selectByApplyCodeWithDesign(String applyCode) {
        if (applyCode == null || applyCode.isEmpty()) {
            return null;
        }
        return mapper.selectByApplyCodeWithDesign(applyCode);
    }

    // 按申请单号批量查询，入参为空返回空列表
    public List<FlowableApply> selectByApplyCodes(Collection<String> applyCodes) {
        if (applyCodes == null || applyCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectByApplyCodes(applyCodes);
    }

    /** 按申请单号批量查询（JOIN 设计表回填 designName），入参为空返回空列表 */
    public List<ApplyPageResp> selectByApplyCodesWithDesign(Collection<String> applyCodes) {
        if (applyCodes == null || applyCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectByApplyCodesWithDesign(applyCodes);
    }
}
