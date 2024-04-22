import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import General.AMISortCallbackPrx;
import General.MergeResult;
import General.MergeWorkerPrx;
import com.zeroc.Ice.Current;

/**
 * CoordinatorI class implements the MergeCoordinator interface.<br>
 * This class is responsible for coordinating the parallel sorting process using multiple workers.<br>
 * <br>
 * The coordinator divides the data to be sorted into chunks and sends each chunk to a worker.<br>
 * @author Andres Parra
 * @author Alejandro Mantilla
 * @author Silem Nabib (Documentation and Corrections)
 */
public class CoordinatorI implements General.MergeCoordinator{
    private final List<MergeWorkerPrx> workers = new ArrayList<>();
    private int[] results;
    private int resultIndex;
    AMISortCallbackPrx cb;

    /**
     * Registers a worker in the list of available workers.
     *
     * @param worker worker to register
     * @param current additional information provided by Ice runtime
     */
    @Override
    public void registerWorker(MergeWorkerPrx worker, Current current) {
        workers.add(worker);
    }

    /**
     * Starts the parallel sorting process.<br>
     * Divides the data between the available workers and sends each portion to a worker to sort.
     *
     * @param data data to sort
     * @param cb callback to send the result
     * @param current additional information provided by Ice runtime
     */
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

    /**
     * Receives the result of a worker and stores it in the results array.<br>
     * When all results have been received, it starts the merge process.
     *
     * @param result result of the worker server
     * @param current additional information provided by Ice runtime
     */
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

    /**
     * Merges the results of the workers and sends the final result to the callback.
     */
    private void mergeResults() {
        Arrays.parallelSort(results);
        MergeResult finalResult = new MergeResult(results);
        cb.sortResult(finalResult);
    }
}