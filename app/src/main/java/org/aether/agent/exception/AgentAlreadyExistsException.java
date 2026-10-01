package org.aether.agent.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;

public class AgentAlreadyExistsException extends AetherException {
    public AgentAlreadyExistsException(String identity) {
        super(ErrorCode.AGENT_ALREADY_EXISTS, "Agent already exists: " + identity);
    }
}
