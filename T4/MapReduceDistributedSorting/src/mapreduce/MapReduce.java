package mapreduce;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

interface MapEmitter<K2, V2> {
    void mapEmit(K2 k2, V2 v2);
}

interface ReduceEmitter<K3, V3> {
    void reduceEmit(K3 k3, V3 v3);
}

interface MapFunction<K1, V1, K2, V2> {
    void map(K1 key, V1 value, MapEmitter<K2, V2> emitter);
}

interface ReduceFunction<K2, V2, K3, V3> {
    void reduce(K2 key, List<V2> values, ReduceEmitter<K3, V3> emitter);
}

public class MapReduce<K1, V1, K2, V2, K3, V3> implements MapEmitter<K2, V2>, ReduceEmitter<K3, V3> {

    private MapFunction<K1, V1, K2, V2> mapFunction;
    private ReduceFunction<K2, V2, K3, V3> reduceFunction;

    private Map<K2, List<V2>> interResult = new TreeMap<K2, List<V2>>();
    private Map<K3, V3> finalResult = new TreeMap<K3, V3>();

    public MapReduce(MapFunction<K1, V1, K2, V2> map, ReduceFunction<K2, V2, K3, V3> reduce) {
        this.mapFunction = map;
        this.reduceFunction = reduce;
    }

    public synchronized void mapEmit(K2 k2, V2 v2) {
        if (interResult.get(k2) == null) {
            List<V2> nl = new ArrayList<V2>();
            nl.add(v2);
            interResult.put(k2, nl);
        } else {
            interResult.get(k2).add(v2);
        }
    }

    public Map<K3, V3> mapReduce(Map<K1, V1> input) {
        List<Thread> mapWorkers = new ArrayList<>();
        for (K1 key1 : input.keySet()) {
            Thread t = new Thread(() -> mapFunction.map(key1, input.get(key1), MapReduce.this));
            t.start();
            mapWorkers.add(t);
        }

        for (Thread w : mapWorkers)
            try {
                w.join();
            } catch (InterruptedException ie) {
            }

        System.out.println(interResult);
        List<Thread> reduceWorkers = new ArrayList<Thread>();
        for (K2 key2 : interResult.keySet()) {
            Thread t = new Thread(() -> reduceFunction.reduce(key2, interResult.get(key2), MapReduce.this));
            t.start();
            reduceWorkers.add(t);
        }
        for (Thread w : reduceWorkers)
            try {
                w.join();
            } catch (InterruptedException ie) {
            }
        return finalResult;
    }

    public synchronized void reduceEmit(K3 k3, V3 v3) {
        finalResult.put(k3, v3);
    }
}

