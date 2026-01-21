package com.finance.dart.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * transaction_log_file MDC 키가 있는 경우에만 로그를 허용하는 필터
 */
public class TransactionLogFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMDCPropertyMap().containsKey("transaction_log_file")) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}
