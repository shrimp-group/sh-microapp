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

    ApplyPageResp selectByProcInsIdWithDesign(@Param("procInsId") String procInsId);

    List<ApplyPageResp> selectByProcInsIdsWithDesign(@Param("procInsIds") Collection<String> procInsIds);

    List<FlowableApply> selectByApplyCodes(@Param("applyCodes") Collection<String> applyCodes);

}
