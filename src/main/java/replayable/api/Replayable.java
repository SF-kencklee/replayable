package replayable.api;

import java.util.Map;

public interface Replayable {
    void execute(Map<String, String> param, ReplayableProcessRuntime replayableProcessRuntime) throws Exception;
}
