package com.wkclz.micro.dbview.utils.schemadiff.model;

import lombok.Data;

/** 单条差异。property 仅 CHANGED 时有值，如 columnType/defaultValue/charset/definition */
@Data
public class DiffItem {
    private DiffScope scope;
    private DiffAction action;
    private String objectName;
    private String property;
    private String baseValue;
    private String otherValue;

    public static DiffItem of(DiffScope scope, DiffAction action, String objectName) {
        DiffItem item = new DiffItem();
        item.setScope(scope);
        item.setAction(action);
        item.setObjectName(objectName);
        return item;
    }

    public static DiffItem changed(DiffScope scope, String objectName, String property, String baseValue, String otherValue) {
        DiffItem item = of(scope, DiffAction.CHANGED, objectName);
        item.setProperty(property);
        item.setBaseValue(baseValue);
        item.setOtherValue(otherValue);
        return item;
    }
}
