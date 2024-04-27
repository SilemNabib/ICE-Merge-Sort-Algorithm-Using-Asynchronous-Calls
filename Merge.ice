#pragma once

module General {
    sequence<int> IntSeq;

    class MergeResult {
        IntSeq data;
    };

    interface MergeWorker {
        void sort(IntSeq data);
    };

    interface AMISortCallback {
        IntSeq sortResult(MergeResult result);
    };
    
    interface MergeCoordinator {
        void registerWorker(MergeWorker* worker);
        void startMergeSort(IntSeq data, AMISortCallback* cb);
        void receiveResult(MergeResult result);
    };

};