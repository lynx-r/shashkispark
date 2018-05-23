package com.workingbit.share.util;

import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class RequestLogFactory {

    private Logger logger;

    public RequestLogFactory(Logger logger) {
        this.logger = logger;
    }

    @NotNull AbstractNCSARequestLog create() {
        return new AbstractNCSARequestLog() {
            @Override
            protected boolean isEnabled() {
                return true;
            }

            @Override
            public void write(String s) {
              logger.info(s);
            }
        };
    }
}