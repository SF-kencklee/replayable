package replayable.test;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import replayable.api.ReplayableProcessScheduler;
import replayable.api.ReplayableProcessStatus;
import replayable.impl.TestReplayableProcessScheduler;

public class ReplayableExample1Tests {
    @Test
    public void test() throws Exception {
        ReplayableProcessScheduler scheduler = new TestReplayableProcessScheduler();
        final String procId = scheduler.submit("", ReplayableExample1.class, ImmutableMap.of("userId", "xyz"));

        final long waitEndTime = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < waitEndTime && scheduler.getStatus(procId) != ReplayableProcessStatus.COMPLETED) {
            Thread.sleep(1000);
        }

        System.err.println(procId + " => " + scheduler.getStatus(procId));
    }
}
