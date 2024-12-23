package com.example;

import com.brekelov.entity.MCPRequest;
import com.brekelov.entity.MCPResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp")
public class MCPController {

  private final InitializationHandler initializationHandler;

  public MCPController(InitializationHandler initializationHandler) {
    this.initializationHandler = initializationHandler;
  }

  @PostMapping(value = "/init", consumes = "application/json", produces = "application/json")
  public MCPResponse init(@RequestBody MCPRequest mcpRequest) {
    if (mcpRequest.getMethod().equals("initialize")) {
      return initializationHandler.handleInitializationRequest(mcpRequest);
    }
    //todo: implement other methods
    return new MCPResponse();
  }
}

