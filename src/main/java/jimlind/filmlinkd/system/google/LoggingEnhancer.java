package jimlind.filmlinkd.system.google;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Payload;
import com.google.cloud.logging.logback.LoggingEventEnhancer;
import com.google.gson.Gson;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.event.KeyValuePair;

/**
 * Translates a variety of inputs to formats that Google logging supports and can easily filter on
 * when generating reports.
 */
public class LoggingEnhancer implements LoggingEventEnhancer {

  private static final int MAX_LOG_ENTRY_LENGTH = 128000;

  private static LoggerContextVO extractLoggerContextVo(ILoggingEvent loggingEvent) {
    return loggingEvent.getLoggerContextVO();
  }

  @Override
  public void enhanceLogEntry(LogEntry.Builder logEntryBuilder, ILoggingEvent loggingEvent) {
    Map<String, Object> map = new HashMap<>();

    map.put("thread", loggingEvent.getThreadName());
    map.put("context", extractLoggerContextVo(loggingEvent).getName());
    map.put("logger", loggingEvent.getLoggerName());

    List<KeyValuePair> valueList = loggingEvent.getKeyValuePairs();
    Map<String, Object> metadata = new HashMap<>();
    if (valueList != null) {
      for (KeyValuePair pair : valueList) {
        metadata.put(pair.key, limitLength(parseValue(pair.value)));
      }
      map.put("metadata", metadata);
    }

    Payload.JsonPayload payload = logEntryBuilder.build().getPayload();
    map.putAll(payload.getDataAsMap());

    logEntryBuilder.setPayload(Payload.JsonPayload.of(map));
  }

  private Object parseValue(Object input) {
    if (input instanceof String
        || input instanceof Integer
        || input instanceof Long
        || input instanceof Float
        || input instanceof Double) {
      return input;
    }

    // TODO: Check what sort of exception I can actually get out of here.
    String result;
    try {
      result = new Gson().toJson(input);
    } catch (OverlappingFileLockException jsonException) {
      try {
        result = input.toString();
      } catch (OverlappingFileLockException toStringException) {
        result = toStringException.toString();
      }
    }

    return result;
  }

  private Object limitLength(Object input) {
    if (input instanceof String) {
      byte[] inputBytes = ((String) input).getBytes(StandardCharsets.UTF_8);
      if (inputBytes.length > MAX_LOG_ENTRY_LENGTH) {
        return new String(inputBytes, 0, MAX_LOG_ENTRY_LENGTH, StandardCharsets.UTF_8);
      }
    }
    return input;
  }
}
