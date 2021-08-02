package com.scb.java.interview.test;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DeadlineEngineImpl implements DeadlineEngine {

    private final ConcurrentMap<Long, Long> deadlines;
    private final AtomicLong requestId;

    private DeadlineEngineImpl(final AtomicLong requestId, final ConcurrentMap<Long, Long> deadlines) {
        this.deadlines = deadlines;
        this.requestId = requestId;
    }

    public static DeadlineEngineImpl create() {
        return new DeadlineEngineImpl(new AtomicLong(0), new ConcurrentHashMap<>());
    }

    @Override
    public long schedule(long deadlineMs) {
        if (deadlineMs <= 0)
            return -1;

        var id = requestId.incrementAndGet();
        deadlines.putIfAbsent(id, deadlineMs);
        return id;
    }

    @Override
    public boolean cancel(long requestId) {
        var value = deadlines.get(requestId);
        return Objects.nonNull(value) && deadlines.remove(requestId, value);
    }

    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        var count = 0;

        for (var requestId : deadlines.keySet()) {
            if (count < maxPoll && nowMs >= deadlines.get(requestId)) {
                count++;
                deadlines.remove(requestId);
                handler.accept(requestId);
            } else {
                break;
            }
        }
        return count;
    }

    @Override
    public int size() {
        return deadlines.size();
    }

}
