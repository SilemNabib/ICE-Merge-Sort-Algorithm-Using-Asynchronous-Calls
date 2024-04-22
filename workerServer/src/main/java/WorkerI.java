import java.util.Arrays;

import com.zeroc.Ice.Current;

import General.MergeResult;

public class WorkerI implements General.MergeWorker {

    @Override
    public void sort(int[] data, Current current) {
        Arrays.parallelSort(data);
        MergeResult result = new MergeResult(data);
        WorkerServer.coordinator.receiveResult(result);
    }
}