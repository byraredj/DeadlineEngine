package com.scb.java.interview.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static com.scb.java.interview.test.Constants.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeadlineEngine Tests")
class DeadlineEngineImplTest {

    private DeadlineEngine deadlineEngine;

    private Consumer<Long> consumer = (l) -> System.out.println("Expired Deadline ID is " + l);

    @Mock
    private DeadlineEngine deadlineEngineMock;

    @BeforeEach
    private void init() {
        deadlineEngine = DeadlineEngineImpl.create();
    }

    @Test
    @DisplayName("Test Deadline Object Creation")
    public void testDeadlineCreation() {
        DeadlineEngine deadlineEngine = DeadlineEngineImpl.create();
        assertTrue(Objects.nonNull(deadlineEngine));
    }

    @Nested
    @DisplayName("DeadlineEngine Size Tests")
    class DeadlinesSizeTest {

        @Test
        @DisplayName("Size without any deadlines")
        public void testInitialSize() {
            assertThat(deadlineEngine.size(), is(ZERO));
        }

        @Test
        @DisplayName("Size with one deadline")
        public void testOneDeadlineSize() {
            deadlineEngine.schedule(1000);
            assertThat(deadlineEngine.size(), is(ONE));
        }

        @Test
        @DisplayName("Size with max Integer value")
        public void testSizeMaxIntValue() {
            Mockito.when(deadlineEngineMock.size()).thenReturn(Integer.MAX_VALUE);
            assertThat(deadlineEngineMock.size(), is(Integer.MAX_VALUE));
        }

        @Test
        @DisplayName("Size greater than max integer value")
        public void testSizeGreaterThanMaxIntValue() {
            Mockito.when(deadlineEngineMock.size()).thenReturn(Integer.MAX_VALUE + 1);
            assertThat(deadlineEngineMock.size(), not(Integer.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("Cancel Deadlines Tests")
    class CancelDeadlinesTest {

        @Test
        @DisplayName("Successful cancel of deadline")
        public void testSuccessfulCancel() {
            long requestId = deadlineEngine.schedule(1000);
            assertThat(deadlineEngine.cancel(requestId), is(SUCCESS));
        }

        @Test
        @DisplayName("Unsuccessful cancel of deadline")
        public void testInitialCancel() {
            assertThat(deadlineEngine.cancel(100), is(UNSUCCESS));
        }

        @Test
        @DisplayName("Unsuccessful cancel of Not Available deadline")
        public void testMissingDeadline() {
            var id = deadlineEngine.schedule(1000);
            assertThat(deadlineEngine.cancel(id + 1), is(UNSUCCESS));
        }

        @Test
        @DisplayName("Cancel the same deadline twice")
        public void testDuplicateCancel() {
            var id = deadlineEngine.schedule(1000);
            assertThat(deadlineEngine.cancel(id), is(SUCCESS));
            assertThat(deadlineEngine.cancel(id), is(UNSUCCESS));
        }
    }

    @Nested
    @DisplayName("Deadline Schedule Tests")
    class ScheduleTest {

        @Test
        @DisplayName("Valid schedule")
        public void testOneSchedule() {
            var id = deadlineEngine.schedule(1000);

            assertThat(deadlineEngine.size(), is(ONE));
            assertThat(id, is(1L));
        }

        @Test
        @DisplayName("Valid multiple schedules")
        public void testMultipleSchedules() {
            long[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            long[] actual = new long[10];
            for (int i = 1; i <= 10; i++) {
                actual[i - 1] = deadlineEngine.schedule(1000 * i);
            }
            var id = deadlineEngine.schedule(1000);
            assertArrayEquals(expected, actual);
        }

        @Test
        @DisplayName("Invalid negative or zero schedules")
        public void testInvalidSchedule() {
            var id = deadlineEngine.schedule(-1);
            assertThat(id, is(-1L));
            var zeroId = deadlineEngine.schedule(0);
            assertThat(id, is(-1L));
        }

    }

    @Nested
    @DisplayName("Deadline Poll Tests")
    class DeadlinePollTest {

        @Test
        @DisplayName("Initial Poll Without Any schedules")
        public void initPoll() {
            assertThat(deadlineEngine.poll(10, consumer, 1), is(ZERO));
        }

        @Test
        @DisplayName("Poll with only one matching deadline")
        public void testPollOneMatchingDeadline() {
            deadlineEngine.schedule(1000);
            assertThat(deadlineEngine.poll(2000, consumer, 1), is(ONE));
        }

        @Test
        @DisplayName("Poll with no matching deadline")
        public void testPollWithNoMatchingDeadline() {
            deadlineEngine.schedule(1000);
            assertThat(deadlineEngine.poll(500, consumer, 1), is(ZERO));
        }

        @Test
        @DisplayName("Poll with mixed matching deadlines but within Max Poll")
        public void testPollWithMixedMatchingDeadlineWithinMaxPoll() {
            generateSchedules(5);
            assertThat(deadlineEngine.poll(3000, consumer, 5), is(THREE));
        }

        @Test
        @DisplayName("Poll with mixed matching deadlines but greater than Max Poll")
        public void testPollWithMixedMatchingDeadlineGreaterThanMaxPoll() {
            generateSchedules(5);
            var MAX_POLL = TWO;
            assertThat(deadlineEngine.poll(3000, consumer, MAX_POLL), is(MAX_POLL));
        }

        @Test
        @DisplayName("Poll with mixed matching deadlines in multiple poll cycles")
        public void testPollWithMixedMatchingDeadlineGreaterThanMaxPollInMultipleCycles() {
            generateSchedules(5);
            var MAX_POLL = TWO;
            assertThat(deadlineEngine.poll(3000, consumer, MAX_POLL), is(MAX_POLL));
            MAX_POLL = ONE;
            assertThat(deadlineEngine.poll(3000, consumer, MAX_POLL), is(MAX_POLL));
        }

        private void generateSchedules(int count) {
            for(int i = 1; i <= count; i++)
                deadlineEngine.schedule(i * 1000);
        }

    }

}