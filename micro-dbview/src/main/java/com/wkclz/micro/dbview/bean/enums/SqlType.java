package com.wkclz.micro.dbview.bean.enums;

public enum SqlType {
    SELECT, INSERT, UPDATE, DELETE,
    ALTER, CREATE, DROP, RENAME,
    SHOW, DESC, EXPLAIN, OTHER;

    public static SqlType parse(String sql) {
        if (sql == null || sql.isBlank()) {
            return OTHER;
        }
        String trimmed = sql.trim().toUpperCase();
        if (trimmed.startsWith("SELECT")) return SELECT;
        if (trimmed.startsWith("INSERT")) return INSERT;
        if (trimmed.startsWith("UPDATE")) return UPDATE;
        if (trimmed.startsWith("DELETE")) return DELETE;
        if (trimmed.startsWith("ALTER")) return ALTER;
        if (trimmed.startsWith("CREATE")) return CREATE;
        if (trimmed.startsWith("DROP")) return DROP;
        if (trimmed.startsWith("RENAME")) return RENAME;
        if (trimmed.startsWith("SHOW")) return SHOW;
        if (trimmed.startsWith("DESC")) return DESC;
        if (trimmed.startsWith("EXPLAIN")) return EXPLAIN;
        return OTHER;
    }

    public boolean isRead() {
        return this == SELECT || this == SHOW || this == DESC || this == EXPLAIN;
    }

    public boolean isWrite() {
        return this == INSERT || this == UPDATE || this == DELETE;
    }

    public boolean isDdl() {
        return this == ALTER || this == CREATE || this == DROP || this == RENAME;
    }
}
