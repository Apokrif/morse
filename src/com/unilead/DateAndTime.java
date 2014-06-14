package com.unilead;

import java.util.*;

public class DateAndTime {
    protected static DateAndTime instance = new DateAndTime();

    @Deprecated // for testing
    public static void setInstance(DateAndTime newInstance) {
        instance = newInstance;
    }

    public static TimeZone localTimeZone() {
        return instance.internalLocalTimeZone();
    }

    public static Date now() {
        return instance.internalNow();
    }

    public TimeZone internalLocalTimeZone() {
        return TimeZone.getDefault();
    }

    public Date internalNow() {
        return new Date();
    }
}
