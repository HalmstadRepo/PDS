package mapreduce;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        MapFunction<Integer, String, String, Integer> map =
                (key, value, emitter) -> {
                    StringTokenizer st = new StringTokenizer(value, " \t\n");
                    while (st.hasMoreTokens()) {
                        emitter.mapEmit(st.nextToken(), 1);
                    }
                };

        ReduceFunction<String, Integer, String, Integer> reduce =
                (key, values, emitter) -> {
                    Integer sum = 0;
                    for (Integer i : values) {
                        sum += i;
                    }
                    emitter.reduceEmit(key, sum);
                };

        MapReduce<Integer, String, String, Integer, String, Integer> mapReduce = new MapReduce<>(map, reduce);
        Map<Integer, String> input = new TreeMap<>();

        input.put(0, "most people ignore most poetry");
        input.put(1, "most poetry ignores most people");

        Map<String, Integer> output = mapReduce.mapReduce(input);
        System.out.println(output);
    }
}
