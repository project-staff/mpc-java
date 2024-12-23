package com.example.entity;

import java.util.HashMap;
import java.util.Map;

public class InitializationHandler {

  private static final String SUPPORTED_PROTOCOL_VERSION = "2024-11-05";

  public MCPMessage handleInitializationRequest(MCPMessage message) {
    Map<String, Object> params = message.getParams();

    String protocol = (String) params.get("protocolVersion");
    if (!SUPPORTED_PROTOCOL_VERSION.equals(protocol)) {
      return new MCPMessage(message.getId(), "Unsupported protocol version");
    }

    HashMap<String, Object> result = new HashMap<>();
    return new MCPMessage(message.getId(), result);
  }
}
