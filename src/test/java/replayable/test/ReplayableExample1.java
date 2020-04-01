package replayable.test;

import replayable.api.Replayable;
import replayable.api.ReplayableProcessRuntime;

import java.util.Map;

public class ReplayableExample1 implements Replayable {
    @Override
    public void execute(Map<String, String> params, ReplayableProcessRuntime replayableProcessRuntime) throws Exception {
        final Helper h = replayableProcessRuntime.shadow(Helper.class, new HelperImpl());
        System.err.println("======");
        int n = h.task1();
        h.task2(n);
        h.task3();
    }

    interface Helper {
        int task1();
        void task2(int n) throws Exception;
        void task3();
    }

    class HelperImpl implements Helper {
        @Override
        public int task1() {
            System.err.println("task1 starts");
            final int n = (int)(Math.random() * 100);
            System.err.println("task1 ends with n=" + n);
            return n;
        }

        @Override
        public void task2(int n) throws Exception {
            System.err.println("task2 starts with n=" + n);
            if (Math.random() < 0.8) { // 80% chance of failure
                throw new Exception("task2 fake failure");
            }
            System.err.println("task2 ends");
        }

        @Override
        public void task3() {
            System.err.println("task3");
        }
    }
}
