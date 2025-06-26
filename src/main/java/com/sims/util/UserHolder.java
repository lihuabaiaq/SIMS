package com.sims.util;


public class UserHolder {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();
    public static void saveId(Long id) {
        tl.set(id);
    }
    public static Long getId() {
        return tl.get();
    }
    public static void remove() {
        tl.remove();
    }
}
