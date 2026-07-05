package com.paytm.framework.reporting.listenerDecorators;

import com.paytm.framework.reporting.listeners.NullListener;

/**
 * Created by deepakkumar on 17/3/18.
 */
public final class DefaultTestNGListener extends ListenerDecorator {

    public DefaultTestNGListener() {
        super(new DefaultListener(new NullListener()));
    }
}
