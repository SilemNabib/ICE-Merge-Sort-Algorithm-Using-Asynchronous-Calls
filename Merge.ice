#pragma once

module General {
    sequence<int> IntSeq;

    class MergeResult {
        IntSeq data;
    };

    interface MergeWorker {
        idempotent MergeResult sort(IntSeq data);
    };

    interface AMISortCallback {
        void sortResult(MergeResult result);
    };
    
    interface MergeCoordinator {
        void registerWorker(MergeWorker* worker);
        void startMergeSort(IntSeq data, AMISortCallback* cb);
        void receiveResult(MergeResult result);
    };

};