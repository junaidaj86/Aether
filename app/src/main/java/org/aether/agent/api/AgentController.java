package org.aether.agent.api;

import org.aether.agent.service.AgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;


import jakarta.validation.Valid;

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
    public List<AgentResponse> getAgent(){
        return agentService.getAllAgents();
    }

    @GetMapping("/name")
    public AgentResponse getAgentByName(@RequestParam String name){
        return agentService.getAgentByName(name);
    }

    @PutMapping
    public AgentResponse updateAgent(@RequestParam UUID id, @Valid @RequestBody AgentRequest request){
        return agentService.updateAgent(id, request);
    }

    @DeleteMapping
    public void deleteAgent(@RequestParam UUID id){
        agentService.deleteAgent(id);
    }
}
