package com.wkclz.micro.msg.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table msg_template (消息模板) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MsgTemplate extends BaseEntity {

    /**
     * 模板编码
     */
    @FieldDesc(value = "模板编码", notNull = true)
    private String templateCode;

    /**
     * 模板名称
     */
    @FieldDesc(value = "模板名称", notNull = true)
    private String templateName;

    /**
     * 消息标题
     */
    @FieldDesc(value = "消息标题", notNull = true)
    private String title;

    /**
     * 消息内容
     */
    @FieldDesc(value = "消息内容")
    private String content;


    public static MsgTemplate copy(MsgTemplate source, MsgTemplate target) {
        if (target == null ) { target = new MsgTemplate();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTemplateCode(source.getTemplateCode());
        target.setTemplateName(source.getTemplateName());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MsgTemplate copyIfNotNull(MsgTemplate source, MsgTemplate target) {
        if (target == null ) { target = new MsgTemplate();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTemplateCode() != null) { target.setTemplateCode(source.getTemplateCode()); }
        if (source.getTemplateName() != null) { target.setTemplateName(source.getTemplateName()); }
        if (source.getTitle() != null) { target.setTitle(source.getTitle()); }
        if (source.getContent() != null) { target.setContent(source.getContent()); }
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

