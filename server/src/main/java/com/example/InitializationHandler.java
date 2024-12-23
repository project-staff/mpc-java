package com.example;

import com.brekelov.entity.MCPRequest;
import com.brekelov.entity.MCPResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InitializationHandler {

  private static final String SUPPORTED_PROTOCOL_VERSION = "2024-11-05";

  public MCPResponse handleInitializationRequest(MCPRequest message) {
    Map<String, Object> params = message.getParams();

    String protocol = (String) params.get("protocolVersion");
    if (!SUPPORTED_PROTOCOL_VERSION.equals(protocol)) {
      return new MCPResponse();
    }

    HashMap<String, Object> result = new HashMap<>();
    return new MCPResponse();
  }
}
