public class Client {
    public static void main(String[] args) {
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args)) {
            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("Merge:default -p 10000");
            General.MergeCoordinatorPrx cordinator = General.MergeCoordinatorPrx.checkedCast(base);
            if (cordinator == null) {
                throw new Error("Invalid proxy");
            }
            // Implement sorting view "interface"
            //System.out.println(cordinator.sort(new int[]{1, 2, 3, 4, 5}));
        }
    }
}
