package io.github.klee.sonar.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Kévin Buntrock
 */
public enum ObjectMapperHolder {
    INSTANCE;

    private final ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ObjectMapper get() {
        return om;
    }
}
