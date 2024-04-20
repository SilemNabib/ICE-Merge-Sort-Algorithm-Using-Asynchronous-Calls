import java.util.Arrays;

import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import General.AMISortCallbackPrx;
import General.MergeResult;

public class Client implements General.AMISortCallback {

    public static void main(String[] args) {
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("MergeClient", "default -p 0");
            Client client = new Client();
            adapter.add(client, Util.stringToIdentity("client"));
            adapter.activate();

            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("coordinator:default -p 27402");
            General.MergeCoordinatorPrx coordinator = General.MergeCoordinatorPrx.checkedCast(base);
            if (coordinator == null) {
                throw new Error("Invalid proxy");
            }
            System.out.println("Client connected to server");
            int[] data = { 3, 4, 5, 1, 2 };
            System.out.println("Sorting data: " + Arrays.toString(data));

            coordinator.startMergeSort(data, AMISortCallbackPrx.checkedCast(
                    adapter.createProxy(Util.stringToIdentity("client"))
            ));
        }
    }

    @Override
    public synchronized void sortResult(MergeResult result, com.zeroc.Ice.Current current) {
        System.out.println("Received sorted data"+ Arrays.toString(result.data));
    }
}