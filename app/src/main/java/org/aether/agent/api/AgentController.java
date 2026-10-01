package org.aether.agent.api;

import org.aether.agent.service.AgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.aether.agent.domain.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public AgentResponse registerAgent( @Valid @RequestBody AgentRequest request){
        return agentService.registerAgent(request);
    }   

    @GetMapping
    public Page<AgentResponse> getAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return agentService.getAllAgents(pageable);
    }

    @GetMapping("/name")
    public List<AgentResponse> getAgentsByName(
            @RequestParam String name,
            @RequestParam(required = false) Environment environment) {
        return agentService.getAgentsByName(name, environment);
    }

    @GetMapping("/{id}")
    public AgentResponse getAgentById(@PathVariable UUID id) {
        return agentService.getAgentById(id);
    }

    @PutMapping
    public AgentResponse updateAgent(@RequestParam UUID id, @Valid @RequestBody AgentRequest request){
        return agentService.updateAgent(id, request);
    }

    @PatchMapping
    public AgentResponse patchAgent(@RequestParam UUID id, @Valid @RequestBody AgentPatchRequest request) {
        return agentService.patchAgent(id, request);
    }

    @PostMapping("/{id}/activate")
    public AgentResponse activateAgent(@PathVariable UUID id) {
        return agentService.activateAgent(id);
    }

    @PostMapping("/{id}/deactivate")
    public AgentResponse deactivateAgent(@PathVariable UUID id) {
        return agentService.deactivateAgent(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAgent(@RequestParam UUID id){
        agentService.deleteAgent(id);
    }
}
