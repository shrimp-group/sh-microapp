package com.wkclz.micro.dbview.utils.schemadiff.model;

/** 以基准侧(base)视角：ADDED=仅基准侧有，REMOVED=仅对比侧有，CHANGED=两侧都有但不同 */
public enum DiffAction {
    ADDED,
    REMOVED,
    CHANGED
}
