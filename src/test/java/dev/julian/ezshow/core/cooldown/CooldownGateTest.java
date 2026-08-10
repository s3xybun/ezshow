package dev.julian.ezshow.core.cooldown;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CooldownGateTest {
    @Test
    public void firstUseAcquiresAndSecondUseIsRejected() {
        CooldownGate<String> gate = new CooldownGate<>();

        assertTrue(gate.tryAcquire("steve", 100L, 50L));
        assertFalse(gate.tryAcquire("steve", 120L, 50L));
    }

    @Test
    public void useAtDeadlineAcquiresAgain() {
        CooldownGate<String> gate = new CooldownGate<>();
        gate.tryAcquire("steve", 100L, 50L);

        assertTrue(gate.tryAcquire("steve", 150L, 50L));
    }

    @Test
    public void playersHaveIndependentCooldowns() {
        CooldownGate<String> gate = new CooldownGate<>();
        gate.tryAcquire("steve", 100L, 50L);

        assertTrue(gate.tryAcquire("alex", 120L, 50L));
        assertFalse(gate.tryAcquire("steve", 120L, 50L));
    }

    @Test
    public void zeroDurationDoesNotLeaveADeadline() {
        CooldownGate<String> gate = new CooldownGate<>();

        assertTrue(gate.tryAcquire("steve", 100L, 0L));
        assertTrue(gate.tryAcquire("steve", 100L, 0L));
    }

    @Test
    public void nanoTimeWrapAroundStillHonorsDeadline() {
        CooldownGate<String> gate = new CooldownGate<>();
        long start = Long.MAX_VALUE - 5L;
        gate.tryAcquire("steve", start, 10L);

        assertFalse(gate.tryAcquire("steve", Long.MIN_VALUE + 1L, 10L));
        assertTrue(gate.tryAcquire("steve", Long.MIN_VALUE + 4L, 10L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeDurationIsRejected() {
        new CooldownGate<String>().tryAcquire("steve", 0L, -1L);
    }
}
