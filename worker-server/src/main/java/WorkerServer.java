import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class WorkerServer {
    static General.MergeCoordinatorPrx coordinator;

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("MergeWorker", "default -p 0");
            WorkerI worker = new WorkerI();
            adapter.add(worker, Util.stringToIdentity("worker"));
            adapter.activate();

            com.zeroc.Ice.ObjectPrx main = communicator.stringToProxy("coordinator:default -p 27402");
            coordinator = General.MergeCoordinatorPrx.checkedCast(main);
            if (coordinator == null) {
                throw new Error("Invalid proxy");
            } else {
                coordinator.registerWorker(
                        General.MergeWorkerPrx.checkedCast(adapter.createProxy(Util.stringToIdentity("worker"))));
            }

            communicator.waitForShutdown();
        }
    }
}