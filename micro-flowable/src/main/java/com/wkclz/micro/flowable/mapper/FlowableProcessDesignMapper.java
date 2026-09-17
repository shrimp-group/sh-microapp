package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.bean.req.DesignPageReq;
import com.wkclz.micro.flowable.bean.resp.DesignPageResp;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlowableProcessDesignMapper extends BaseMapper<FlowableProcessDesign> {
    List<DesignPageResp> getDesignPage(DesignPageReq req);
}
