package org.aether.agent.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;
import java.util.UUID;

public class AgentNotFoundException extends AetherException {

    public AgentNotFoundException(UUID id) {
        super(ErrorCode.AGENT_NOT_FOUND, "Agent not found: " + id);
    }

    public AgentNotFoundException(String name) {

        super(ErrorCode.AGENT_NOT_FOUND, "Agent not found with name: " + name);

    }
}
