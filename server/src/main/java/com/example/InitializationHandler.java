package com.example;

import com.brekelov.entity.MCPRequest;
import com.brekelov.entity.MCPResponse;
import com.brekelov.entity.Schema.JSONRPCRequest;
import com.brekelov.entity.Schema.JSONRPCResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InitializationHandler {

  private static final String SUPPORTED_PROTOCOL_VERSION = "2024-11-05";

  public JSONRPCResponse handleInitializationRequest(JSONRPCRequest request) {
    System.out.println("handleInitializationRequest");
    System.out.println(request.params());

//    MCPRequest params = request.params();
//    String protocol = (String) params.get("protocolVersion");
//    if (!SUPPORTED_PROTOCOL_VERSION.equals(protocol)) {
//      return new MCPResponse();
//    }

    return new JSONRPCResponse(request.jsonrpc(), request.id(), null, null);
  }
}
