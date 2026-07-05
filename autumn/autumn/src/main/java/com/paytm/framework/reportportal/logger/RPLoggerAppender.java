package com.paytm.framework.reportportal.logger;

import com.paytm.framework.reportportal.api.LogItemRequest;
import com.paytm.framework.reportportal.service.dto.LaunchInfo;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Calendar;

//TODO: NEED TO CHECK THIS
public class RPLoggerAppender extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent event) {
        if (null != event.getMessage()) {
            String level = event.getLevel().toString();
            String className = event.categoryName;
            String message = event.getMessage().toString();
//            Reporter.REPORT.info("[" + level + "] - " + "[" + className + "]" + " - " + message);
            if (!LaunchInfo.getInstance().getItemUuid().isEmpty()) {
                LogItemRequest rq = new LogItemRequest();
                rq.setContext("itemUuid", LaunchInfo.getInstance().getItemUuid());
                rq.setContext("time", Calendar.getInstance().getTime());
                rq.setContext("level", level);
                rq.setContext("message", "[" + className + "]" + " - " + message);
                rq.execute();
            }
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
