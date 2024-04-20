import java.util.Arrays;

import com.zeroc.Ice.Current;

import General.MergeResult;

public class WorkerI implements General.MergeWorker {

    @Override
    public MergeResult sort(int[] data, Current current) {
        Arrays.parallelSort(data);
        System.out.println("Worker sorted data: " + Arrays.toString(data));
        MergeResult result = new MergeResult(data);
        WorkerServer.coordinator.receiveResult(result);
        return result;
    }
}