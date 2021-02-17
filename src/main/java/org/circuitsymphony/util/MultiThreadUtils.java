package org.circuitsymphony.util;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MultiThreadUtils {
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREADS,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("mt-utils-pool-%d").build());

    public static <T> void runInParallel(Iterable<T> data, Consumer<Iterable<T>> func) {
        try {
            executorService.invokeAll(
                    StreamSupport.stream(Iterables.partition(data, Math.max(1, Iterables.size(data) / THREADS + 1)).spliterator(), true)
                            .map(subData -> (Callable<Void>) () -> {
                                try {
                                    func.accept(subData);
                                } catch (Exception e) {
                                    Logger.getLogger(MultiThreadUtils.class.getSimpleName()).log(Level.SEVERE, "Couldn't process commands", e);
                                }
                                return null;
                            })
                            .collect(Collectors.toList()));
        } catch (InterruptedException e) {
            Logger.getLogger(MultiThreadUtils.class.getSimpleName()).log(Level.SEVERE, "Couldn't run commands", e);
        }
    }
}
