package replayable.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import replayable.api.Replayable;
import replayable.api.ReplayableProcessScheduler;
import replayable.api.ReplayableProcessStatus;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestReplayableProcessScheduler implements ReplayableProcessScheduler {
    private final static Set<ReplayableProcessStatus> EXECUTABLE_STATUSES =
            ImmutableSet.of(ReplayableProcessStatus.PENDING, ReplayableProcessStatus.RUNNING);

    private class ProcessInfo {
        final String ref;
        final Class<? extends Replayable> replayableProcessType;
        final Map<String, String> params;
        final Map<Pair<Class<?>, String>, Serializable> results;
        volatile ReplayableProcessStatus status = ReplayableProcessStatus.PENDING;

        public ProcessInfo(String ref, Class<? extends Replayable> replayableProcessType, Map<String, String> params) {
            this.ref = ref;
            this.replayableProcessType = replayableProcessType;
            this.params = ImmutableMap.copyOf(params);
            this.results = Maps.newHashMap();
        }
    }

    final Map<String, ProcessInfo> processes = Maps.newConcurrentMap();
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public TestReplayableProcessScheduler() {
        executor.scheduleAtFixedRate(() -> execute(), 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public String submit(String ref, Class<? extends Replayable> processType, Map<String, String> params) {
        String id = UUID.randomUUID().toString();
        processes.put(id, new ProcessInfo(ref, processType, params));
        return id;
    }

    @Override
    public ReplayableProcessStatus getStatus(String id) {
        ProcessInfo processInfo = processes.get(id);
        return processInfo == null ? null : processInfo.status;
    }

    private void execute() {
        processes.entrySet().forEach(p -> execute(p.getValue()));
    }

    private void execute(ProcessInfo processInfo) {
        if (EXECUTABLE_STATUSES.contains(processInfo.status)) {
            // begin transaction here
            try {
                processInfo.status = ReplayableProcessStatus.RUNNING;
                final Replayable o = processInfo.replayableProcessType.newInstance();
                o.execute(processInfo.params, new TestReplayableProcessRuntime(processInfo.results));
                processInfo.status = ReplayableProcessStatus.COMPLETED;
            } catch (Exception e) {
                // user exception, swallow it and schedule a retry or abort
            } finally {
                // end transaction here
            }
        }
    }
}
