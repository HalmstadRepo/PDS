package mapreduce;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {
        new Main().run();
    }

    private void mapIntegers() {
        MapFunction<String, Integer, Integer, Integer> map =
                (key, value, emitter) -> {
                    HashMap<Integer, Integer> hashMap = new HashMap<>();

                    int count = hashMap.getOrDefault(value, 0);
                    hashMap.put(value, ++count);

                    hashMap.forEach(emitter::mapEmit);
                };

        ReduceFunction<Integer, Integer, Integer, Integer> reduce =
                (key, values, emitter) -> {
                    Integer sum = values.stream().mapToInt(i -> i).sum();
                    emitter.reduceEmit(key, sum);
                };

        MapReduce<String, Integer, Integer, Integer, Integer, Integer> mapReduce =
                new MapReduce<>(map, reduce);

        Map<String, Integer> input = new TreeMap<>();

        input.put("1", 0);
        input.put("2", 1);
        input.put("3", 2);
        input.put("4", 2);
        input.put("5", 3);
        input.put("6", 4);
        input.put("7", 0);
        input.put("8", 3);
        input.put("9", 3);

        Map<Integer, Integer> output = mapReduce.mapReduce(input);

        System.out.println(output);
    }

    private void mapStrings() {
        MapFunction<String, String, String, Integer> map =
                (key, value, emitter) -> {
                    HashMap<String, Integer> hashMap = new HashMap<>();
                    StringTokenizer st = new StringTokenizer(value, " \t\n");

                    while (st.hasMoreTokens()) {
                        String t = st.nextToken();

                        int count = hashMap.getOrDefault(t, 0);
                        hashMap.put(t, ++count);
                    }

                    hashMap.forEach(emitter::mapEmit);
                };

        ReduceFunction<String, Integer, String, Integer> reduce =
                (key, values, emitter) -> {
                    Integer sum = values.stream().mapToInt(i -> i).sum();
                    emitter.reduceEmit(key, sum);
                };

        MapReduce<String, String, String, Integer, String, Integer> mapReduce =
                new MapReduce<>(map, reduce);

        Map<String, String> input = new TreeMap<>();

        input.put("first", "most people ignore most poetry");
        input.put("second", "most poetry ignores most people");
        input.put("third", "most most most most most");

        Map<String, Integer> output = mapReduce.mapReduce(input);

        System.out.println(output);
    }

    private void run() {
        mapStrings();
        System.out.println("");
        mapIntegers();
    }
}
