package edu.uw.cse.ifrcdemo.distplan.logic;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimpleTest {

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c", "d", "e", "f"})
    void simpleTest(String param) {
        assertNotNull(param);
    }

}
