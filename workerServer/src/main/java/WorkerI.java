import java.util.Arrays;
import com.zeroc.Ice.Current;
import General.MergeResult;

/**
 * WorkerI class implements the MergeWorker interface.<br>
 * This class is responsible for sorting an array of integers and sending the result to the coordinator.<br>
 *
 * @author Andres Parra
 * @author Alejandro Mantilla
 * @author Silem Nabib (Documentation)
 */
public class WorkerI implements General.MergeWorker {

    /**
     * Sorts an array of integers in parallel and sends the result to the coordinator server.
     *
     * @param data the array of integers to sort
     * @param current additional information provided by Ice runtime
     */
    @Override
    public void sort(int[] data, Current current) {
        Arrays.parallelSort(data);
        MergeResult result = new MergeResult(data);
        WorkerServer.coordinator.receiveResult(result);
    }
}