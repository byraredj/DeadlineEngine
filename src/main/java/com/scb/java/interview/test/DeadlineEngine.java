package com.scb.java.interview.test;

import java.util.function.Consumer;

public interface DeadlineEngine {

    long schedule(long deadlineMs);

    boolean cancel(long requestId);

    int poll(long nowMs, Consumer<Long> handler, int maxPoll);

    int size();
}
