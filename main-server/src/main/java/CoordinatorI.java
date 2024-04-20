import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zeroc.Ice.Current;

import General.AMISortCallbackPrx;
import General.MergeCoordinator;
import General.MergeResult;
import General.MergeWorkerPrx;

public class CoordinatorI implements MergeCoordinator {
    private final List<MergeWorkerPrx> workers = new ArrayList<>();
    private int[] results;
    private int resultIndex;
    AMISortCallbackPrx cb;


    @Override
    public synchronized void registerWorker(MergeWorkerPrx worker, Current current) {
        System.out.println("Worker registered");
        workers.add(worker);
    }

    @Override
    public synchronized void startMergeSort(int[] data, AMISortCallbackPrx cb, Current current) {
        this.cb = cb;
        int numWorkers = workers.size();
        int chunkSize = data.length / numWorkers;

        for (int i = 0; i < numWorkers; i++) {
            int start = i * chunkSize;
            int end = (i == numWorkers - 1) ? data.length : (i + 1) * chunkSize;
            int[] chunk = Arrays.copyOfRange(data, start, end);
            workers.get(i).sort(chunk);
            System.out.println("Sent partial data to worker");
        }

        results = new int[data.length];
        resultIndex = 0;

        if (workers.isEmpty()) {
            cb.sortResult(new MergeResult(data));
        }
    }

    @Override
    public synchronized void receiveResult(MergeResult result, Current current) {
        System.out.println("Received partial result");
        int[] partialResult = result.data;
        System.arraycopy(partialResult, 0, results, resultIndex, partialResult.length);
        resultIndex += partialResult.length;


        if (resultIndex == results.length) {
            mergeResults();
        }
    }

    private synchronized void mergeResults() {
        Arrays.parallelSort(results);
        MergeResult finalResult = new MergeResult(results);
        cb.sortResult(finalResult);
    }
}