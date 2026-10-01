package org.common.exception;

import java.util.UUID;

public class AgentNotFoundException extends RuntimeException {

    public AgentNotFoundException(UUID id) {
        super("Agent not found: " + id);
    }

    public AgentNotFoundException(String name) {

        super("Agent not found with name: " + name);

    }
}
