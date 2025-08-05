package com.example.configcenter.context;

import com.example.configcenter.exception.SystemException;

public class ContextManager {

    // 每个请求只会对应一个线程
    private static final ThreadLocal<Context> Context = new ThreadLocal<>();

    public static void setContext(Context context) {
        Context.set(context);
    }

    public static Context getContext()  {
        Context context = Context.get();
        if (context == null) {
            throw new SystemException("context is null");
        }
        return context;
    }

    public static void deleteContext() {
        Context.remove();
    }
}
