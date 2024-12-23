package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class MCPClient {

  private final HttpClient client;
  private final ObjectMapper objectMapper;
  private final String serviceUrl;

  public MCPClient(HttpClient client, String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.client = client;
  }



  public void init(MCPMessage mcpMessage) {

    var jsonRequest =

        ObjectMapper objectMapper = new ObjectMapper();
    Car car = new Car("yellow", "renault");
    objectMapper.writeValue(new File("target/car.json"), car);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(this.serviceUrl))
        .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
        .build();
  }

  public static void main(String[] args) {

    HttpClient client = HttpClient.newHttpClient();

    var mcpClient = new MCPClient(client, "http://localhost:8081");

    mcpClient.init();
  }

}
