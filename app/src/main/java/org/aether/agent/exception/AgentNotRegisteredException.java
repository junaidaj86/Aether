package org.aether.agent.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;

public class AgentNotRegisteredException extends AetherException {

    public AgentNotRegisteredException() {
        super(ErrorCode.AGENT_NOT_REGISTERED,
                "The authenticated identity is not registered as an agent");
    }
}
