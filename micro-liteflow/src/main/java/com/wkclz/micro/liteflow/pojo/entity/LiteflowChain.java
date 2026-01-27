package com.wkclz.micro.liteflow.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_chain (liteflow-规则) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class LiteflowChain extends BaseEntity {

    /**
     * 规则名称
     */
    @Desc("规则名称")
    private String chainName;

    /**
     * 规则描述
     */
    @Desc("规则描述")
    private String chainDesc;

    /**
     * 规则数据
     */
    @Desc("规则数据")
    private String elData;

    /**
     * 路由
     */
    @Desc("路由")
    private String route;

    /**
     * 命名空间
     */
    @Desc("命名空间")
    private String namespace;

    /**
     * 状态
     */
    @Desc("状态")
    private Integer enable;


    public static LiteflowChain copy(LiteflowChain source, LiteflowChain target) {
        if (target == null ) { target = new LiteflowChain();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setChainName(source.getChainName());
        target.setChainDesc(source.getChainDesc());
        target.setElData(source.getElData());
        target.setRoute(source.getRoute());
        target.setNamespace(source.getNamespace());
        target.setEnable(source.getEnable());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static LiteflowChain copyIfNotNull(LiteflowChain source, LiteflowChain target) {
        if (target == null ) { target = new LiteflowChain();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getChainName() != null) { target.setChainName(source.getChainName()); }
        if (source.getChainDesc() != null) { target.setChainDesc(source.getChainDesc()); }
        if (source.getElData() != null) { target.setElData(source.getElData()); }
        if (source.getRoute() != null) { target.setRoute(source.getRoute()); }
        if (source.getNamespace() != null) { target.setNamespace(source.getNamespace()); }
        if (source.getEnable() != null) { target.setEnable(source.getEnable()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

