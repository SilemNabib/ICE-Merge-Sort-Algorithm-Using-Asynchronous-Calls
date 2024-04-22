import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import General.AMISortCallbackPrx;
import General.MergeResult;
import General.MergeWorkerPrx;
import com.zeroc.Ice.Current;

public class CoordinatorI implements General.MergeCoordinator{
    private final List<MergeWorkerPrx> workers = new ArrayList<>();
    private int[] results;
    private int resultIndex;
    AMISortCallbackPrx cb;


    @Override
    public void registerWorker(MergeWorkerPrx worker, Current current) {
        workers.add(worker);
    }

    @Override
    public void startMergeSort(int[] data, AMISortCallbackPrx cb, Current current) {
        this.cb = cb;
        int numWorkers = workers.size();
        int chunkSize = data.length / numWorkers;
        results = new int[data.length];
        resultIndex = 0;

        ExecutorService executor = Executors.newFixedThreadPool(numWorkers);

        for (int i = 0; i < numWorkers; i++) {
            int start = i * chunkSize;
            int end = (i == numWorkers - 1) ? data.length : (i + 1) * chunkSize;
            int[] chunk = Arrays.copyOfRange(data, start, end);
            int finalI = i;

            executor.submit(() -> {
                workers.get(finalI).sort(chunk);
                System.out.println("Sent partial data to worker:" + finalI + ".");
            });
        }

        executor.shutdown();
    }

    @Override
    public void receiveResult(MergeResult result, Current current) {
        System.out.println("Received partial result");
        int[] partialResult = result.data;
        System.arraycopy(partialResult, 0, results, resultIndex, partialResult.length);
        resultIndex += partialResult.length;

        if (resultIndex == results.length) {
            mergeResults();
        }
    }

    private void mergeResults() {
        Arrays.parallelSort(results);
        MergeResult finalResult = new MergeResult(results);
        cb.sortResult(finalResult);
    }
}