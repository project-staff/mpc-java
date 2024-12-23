package com.example;

import com.brekelov.entity.MCPRequest;
import com.brekelov.entity.MCPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class MCPClient {

  private final HttpClient client;
  private final ObjectMapper objectMapper;
  private final String serviceUrl;

  public MCPClient(HttpClient client, String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.client = client;
  }

  public void init(MCPRequest mcpMessage) {

    String jsonRequest = null;
    try {
      jsonRequest = objectMapper.writeValueAsString(mcpMessage);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(this.serviceUrl))
        .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
        .build();

    HttpResponse<String> request = client.send(request, BodyHandlers.ofString());

    objectMapper.readValue(request, MCPResponse.class)
  }

  public static void main(String[] args) {

    HttpClient client = HttpClient.newHttpClient();

    var mcpClient = new MCPClient(client, "http://localhost:8081");

    mcpClient.init(new MCPRequest());
  }

}
