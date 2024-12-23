package com.brekelov.entity;

import java.util.Map;

/**
 * Request from MCP spec
 * https://spec.modelcontextprotocol.io/specification/basic/lifecycle/
 * for the basic lifecycle to init session
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "method": "initialize",
 *   "params": {
 *     "protocolVersion": "2024-11-05",
 *     "capabilities": {
 *       "roots": {
 *         "listChanged": true
 *       },
 *       "sampling": {}
 *     },
 *     "clientInfo": {
 *       "name": "ExampleClient",
 *       "version": "1.0.0"
 *     }
 *   }
 * }
 */

public class MCPRequest {
  private String jsonrpc = "2.0"; // JSON-RPC version
  private Integer id;            // Request/response ID
  private String method;         // RPC method (e.g., "initialize")
  private Map<String, Object> params; // Request parameters
  private Map<String, Object> result; // Response result
  private String error;          // Error message (if applicable)

  // Constructors
  public MCPRequest(Integer id, String method, Map<String, Object> params) {
    this.id = id;
    this.method = method;
    this.params = params;
  }

  public MCPRequest(Integer id, Map<String, Object> result) {
    this.id = id;
    this.result = result;
  }

  public MCPRequest(Integer id, String error) {
    this.id = id;
    this.error = error;
  }

  // Getters and setters
  public String getJsonrpc() {
    return jsonrpc;
  }

  public Integer getId() {
    return id;
  }

  public String getMethod() {
    return method;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public Map<String, Object> getResult() {
    return result;
  }

  public String getError() {
    return error;
  }

}
