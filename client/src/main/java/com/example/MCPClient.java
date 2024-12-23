package com.example;

import com.brekelov.entity.Schema.ClientCapabilities;
import com.brekelov.entity.Schema.ClientCapabilities.RootCapabilities;
import com.brekelov.entity.Schema.ClientCapabilities.Sampling;
import com.brekelov.entity.Schema.Implementation;
import com.brekelov.entity.Schema.InitializeRequest;
import com.brekelov.entity.Schema.JSONRPCRequest;
import com.brekelov.entity.Schema.JSONRPCResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

public class MCPClient {

  private final HttpClient client;
  private final ObjectMapper objectMapper;
  private final String serviceUrl;

  public MCPClient(HttpClient client, String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.objectMapper = new ObjectMapper();
    this.client = client;
  }

  public JSONRPCResponse init(JSONRPCRequest mcpMessage)  {

    String jsonRequest;
    try {
      jsonRequest = objectMapper.writeValueAsString(mcpMessage);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(String.format("%s/mcp/init", this.serviceUrl)))
        .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
        .header("Content-Type", "application/json")
        .build();

    HttpResponse<String> response;
    try {
      response = client.send(request, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    try {
      return objectMapper.readValue(response.body(), JSONRPCResponse.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {

    HttpClient client = HttpClient.newHttpClient();

    var mcpClient = new MCPClient(client, "http://localhost:8080");

    var impl = new Implementation("name", "version");

    var cap = new ClientCapabilities(new HashMap<>(), new RootCapabilities(true), new Sampling());
    JSONRPCRequest mcpMessage = new JSONRPCRequest(
        "jsonrpc",
        "method",
        "id",
        new InitializeRequest("1", cap, impl)
    );
    JSONRPCResponse response = mcpClient.init(mcpMessage);

    System.out.println(response);
  }

}
