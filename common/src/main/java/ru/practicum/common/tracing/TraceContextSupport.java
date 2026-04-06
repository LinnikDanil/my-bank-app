package ru.practicum.common.tracing;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class TraceContextSupport {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String TRACEPARENT = "traceparent";
    private static final String TRACEPARENT_VERSION = "00";
    private static final String TRACEPARENT_FLAGS = "01";

    private TraceContextSupport() {
    }

    public static void populateMdc(Tracer tracer, String traceparent) {
        if (populateMdcFromCurrentSpan(tracer)) {
            return;
        }
        populateMdcFromTraceparent(traceparent);
    }

    public static boolean populateMdcFromCurrentSpan(Tracer tracer) {
        if (tracer == null) {
            return false;
        }

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            populateMdc(currentSpan.context().traceId(), currentSpan.context().spanId());
            return true;
        }

        CurrentTraceContext currentTraceContext = tracer.currentTraceContext();
        TraceContext traceContext = currentTraceContext == null
                ? null
                : currentTraceContext.context();
        if (traceContext != null && hasText(traceContext.traceId()) && hasText(traceContext.spanId())) {
            populateMdc(traceContext.traceId(), traceContext.spanId());
            return true;
        }

        String traceId = MDC.get(TRACE_ID);
        String spanId = MDC.get(SPAN_ID);
        if (hasText(traceId) && hasText(spanId)) {
            populateMdc(traceId, spanId);
            return true;
        }

        return false;
    }

    public static boolean populateMdcFromTraceparent(String traceparent) {
        TraceHeaders traceHeaders = traceHeadersFromTraceparent(traceparent);
        if (!traceHeaders.isPresent()) {
            return false;
        }

        populateMdc(traceHeaders.traceId(), traceHeaders.spanId());
        return true;
    }

    public static void populateMdc(String traceId, String spanId) {
        if (hasText(traceId)) {
            ThreadContext.put(TRACE_ID, traceId);
        }
        if (hasText(spanId)) {
            ThreadContext.put(SPAN_ID, spanId);
        }
    }

    public static void clearMdc() {
        ThreadContext.remove(TRACE_ID);
        ThreadContext.remove(SPAN_ID);
    }

    public static Map<String, String> snapshotMdc() {
        return new LinkedHashMap<>(ThreadContext.getContext());
    }

    public static void withMdc(Map<String, String> context, Runnable action) {
        Map<String, String> previousContext = snapshotMdc();
        ThreadContext.clearMap();
        if (context != null && !context.isEmpty()) {
            ThreadContext.putAll(context);
        }

        try {
            action.run();
        } finally {
            ThreadContext.clearMap();
            if (!previousContext.isEmpty()) {
                ThreadContext.putAll(previousContext);
            }
        }
    }

    public static TraceHeaders currentTraceHeaders() {
        String traceId = ThreadContext.get(TRACE_ID);
        String spanId = ThreadContext.get(SPAN_ID);
        if (!hasText(traceId) || !hasText(spanId)) {
            return TraceHeaders.empty();
        }

        return new TraceHeaders(traceId, spanId, buildTraceparent(traceId, spanId));
    }

    public static TraceHeaders traceHeadersFromTraceparent(String traceparent) {
        if (!hasText(traceparent)) {
            return TraceHeaders.empty();
        }

        String[] parts = traceparent.split("-");
        if (parts.length < 4 || !hasText(parts[1]) || !hasText(parts[2])) {
            return TraceHeaders.empty();
        }

        return new TraceHeaders(parts[1], parts[2], normalizeTraceparent(traceparent));
    }

    private static String buildTraceparent(String traceId, String spanId) {
        return TRACEPARENT_VERSION + "-"
                + traceId.toLowerCase(Locale.ROOT) + "-"
                + spanId.toLowerCase(Locale.ROOT) + "-"
                + TRACEPARENT_FLAGS;
    }

    private static String normalizeTraceparent(String traceparent) {
        return traceparent.toLowerCase(Locale.ROOT);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record TraceHeaders(String traceId, String spanId, String traceparent) {
        public static TraceHeaders empty() {
            return new TraceHeaders(null, null, null);
        }

        public boolean isPresent() {
            return hasText(traceId) && hasText(spanId) && hasText(traceparent);
        }
    }
}
