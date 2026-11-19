package jobth.web;

import java.lang.reflect.RecordComponent;
import java.util.Map;

// เขียน JSON
public final class Json {

    private Json() {
    }

    // จุดเริ่ม
    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        append(sb, value);
        return sb.toString();
    }

    // เลือกตามชนิดข้อมูล
    private static void append(StringBuilder sb, Object value) {
        switch (value) {
            case null -> sb.append("null");
            case String s -> appendString(sb, s);
            case Number n -> sb.append(n);
            case Boolean b -> sb.append(b);
            case Map<?, ?> map -> appendMap(sb, map);
            case Iterable<?> items -> appendList(sb, items);
            case Record r -> appendRecord(sb, r);
            default -> appendString(sb, value.toString());
        }
    }

    // Map
    private static void appendMap(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            appendString(sb, String.valueOf(e.getKey()));
            sb.append(':');
            append(sb, e.getValue());
        }
        sb.append('}');
    }

    // List
    private static void appendList(StringBuilder sb, Iterable<?> items) {
        sb.append('[');
        boolean first = true;
        for (Object item : items) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            append(sb, item);
        }
        sb.append(']');
    }

    // record
    private static void appendRecord(StringBuilder sb, Record record) {
        sb.append('{');
        RecordComponent[] parts = record.getClass().getRecordComponents();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            appendString(sb, parts[i].getName());
            sb.append(':');
            try {
                var accessor = parts[i].getAccessor();
                accessor.setAccessible(true);
                append(sb, accessor.invoke(record));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot read " + parts[i].getName(), e);
            }
        }
        sb.append('}');
    }

    // ข้อความ
    private static void appendString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<', '>', '&' -> sb.append(String.format("\\u%04x", (int) c));
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
