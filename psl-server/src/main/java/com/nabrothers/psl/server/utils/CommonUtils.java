package com.nabrothers.psl.server.utils;

import java.util.List;

public class CommonUtils {
    @SuppressWarnings("rawtypes")
    public static List subList(List list, int from, int to) {
        if (list.isEmpty()) {
            return list;
        }
        int _from = Math.min(from, to);
        int _to = Math.max(from, to);
        _from = Math.max(0, _from);
        _to = Math.max(0, _to);
        _to = Math.min(_to, list.size());
        _from = Math.min(_from, _to);
        return list.subList(_from, _to);
    }
}
