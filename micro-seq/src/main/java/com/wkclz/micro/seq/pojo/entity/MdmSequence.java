package com.wkclz.micro.seq.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_sequence (序列生成记录) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmSequence extends BaseEntity {

    /**
     * 名称
     */
    @Desc("名称")
    private String seqName;

    /**
     * 前缀
     */
    @Desc("前缀")
    private String prefix;

    /**
     * 当前序列
     */
    @Desc("当前序列")
    private Integer sequence;

    /**
     * 序列长度(不计前缀长度)
     */
    @Desc("序列长度(不计前缀长度)")
    private Integer codeLength;


    public static MdmSequence copy(MdmSequence source, MdmSequence target) {
        if (target == null ) { target = new MdmSequence();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setSeqName(source.getSeqName());
        target.setPrefix(source.getPrefix());
        target.setSequence(source.getSequence());
        target.setCodeLength(source.getCodeLength());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmSequence copyIfNotNull(MdmSequence source, MdmSequence target) {
        if (target == null ) { target = new MdmSequence();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getSeqName() != null) { target.setSeqName(source.getSeqName()); }
        if (source.getPrefix() != null) { target.setPrefix(source.getPrefix()); }
        if (source.getSequence() != null) { target.setSequence(source.getSequence()); }
        if (source.getCodeLength() != null) { target.setCodeLength(source.getCodeLength()); }
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

