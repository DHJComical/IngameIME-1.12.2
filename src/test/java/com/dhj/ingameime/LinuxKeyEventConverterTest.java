package com.dhj.ingameime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LinuxKeyEventConverterTest {
    @Test
    void convertsX11HardwareKeycodeToIbusKeycode() {
        assertEquals(0, LinuxKeyEventConverter.toIbusKeycode(8));
        assertEquals(30, LinuxKeyEventConverter.toIbusKeycode(38));
        assertThrows(IllegalArgumentException.class, () -> LinuxKeyEventConverter.toIbusKeycode(7));
    }

    @Test
    void rejectsValuesThatCannotCrossTheJniBoundary() {
        assertEquals(0xFF0D, LinuxKeyEventConverter.toJniKeyval(0xFF0DL));
        assertThrows(IllegalArgumentException.class, () -> LinuxKeyEventConverter.toJniKeyval(-1));
        assertThrows(
            IllegalArgumentException.class,
            () -> LinuxKeyEventConverter.toJniKeyval((long) Integer.MAX_VALUE + 1));
        assertThrows(IllegalArgumentException.class, () -> LinuxKeyEventConverter.validateState(-1));
    }

    @Test
    void mapsX11PressAndReleaseTypes() {
        assertFalse(LinuxKeyEventConverter.isRelease(2));
        assertTrue(LinuxKeyEventConverter.isRelease(3));
        assertThrows(IllegalArgumentException.class, () -> LinuxKeyEventConverter.isRelease(4));
    }

    @Test
    void onlyCancelsHandledKeyPresses() {
        assertFalse(LinuxKeyEventConverter.shouldCancelLwjglHandler(false, false));
        assertTrue(LinuxKeyEventConverter.shouldCancelLwjglHandler(false, true));
        assertFalse(LinuxKeyEventConverter.shouldCancelLwjglHandler(true, false));
        assertFalse(LinuxKeyEventConverter.shouldCancelLwjglHandler(true, true));
    }
}
