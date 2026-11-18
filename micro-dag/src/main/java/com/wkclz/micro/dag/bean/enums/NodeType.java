package com.wkclz.micro.dag.bean.enums;

/** DAG 节点类型：NORMAL 缺省；START/END/BRANCH 为特殊节点，仅作编排锚点 */
public enum NodeType {

    NORMAL("NORMAL"),
    START("START"),
    END("END"),
    BRANCH("BRANCH"),
    ;

    /** 落库与前后端约定的编码 */
    private final String code;

    NodeType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /** 按编码解析，空或未知返回 null */
    public static NodeType ofCode(String code) {
        if (code == null) {
            return null;
        }
        for (NodeType t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        return null;
    }

    /** 返回特殊节点类型（START/END/BRANCH）；NORMAL 或未知返回 null */
    public static NodeType specialOf(String code) {
        NodeType t = ofCode(code);
        return t == null || t == NORMAL ? null : t;
    }
}
