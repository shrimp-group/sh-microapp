package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.resp.ApplyInfoResp;
import com.wkclz.micro.flowable.bean.resp.ApplyPageResp;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface FlowableApplyMapper extends BaseMapper<FlowableApply> {

    List<ApplyPageResp> getApplyPage(FlowableApply entity);

    ApplyInfoResp selectInfoById(@Param("id") Long id);

    /** 按申请单号查询申请（JOIN 设计表回填 designName），用于任务详情回填流程定义信息 */
    ApplyPageResp selectByApplyCodeWithDesign(@Param("applyCode") String applyCode);

    List<FlowableApply> selectByApplyCodes(@Param("applyCodes") Collection<String> applyCodes);

    /** 按申请单号批量查询（JOIN 设计表回填 designName），用于任务列表回填流程名称 */
    List<ApplyPageResp> selectByApplyCodesWithDesign(@Param("applyCodes") Collection<String> applyCodes);

}
