package replayable.api;

import java.util.Map;

public interface ReplayableProcessScheduler {
    String submit(String ref, Class<? extends Replayable> processType, Map<String, String> params);
    ReplayableProcessStatus getStatus(String id);
}
