/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brekelov.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Based on the <a href="http://www.jsonrpc.org/specification">JSON-RPC 2.0
 * specification<a/> and the <a href=
 * "https://github.com/modelcontextprotocol/specification/blob/main/schema/schema.ts">Model
 * Context Protocol Schema</a>.
 *
 * @author Christian Tzolov
 */
public class Schema {

  public static final String LATEST_PROTOCOL_VERSION = "2024-11-05";

  public static final String JSONRPC_VERSION = "2.0";

  // ---------------------------
  // JSON-RPC Error Codes
  // ---------------------------
  public final class ErrorCodes {

    public static final int PARSE_ERROR = -32700;

    public static final int INVALID_REQUEST = -32600;

    public static final int METHOD_NOT_FOUND = -32601;

    public static final int INVALID_PARAMS = -32602;

    public static final int INTERNAL_ERROR = -32603;

  }

  public sealed interface Request
      permits InitializeRequest, CallToolRequest, CreateMessageRequest, CompleteRequest, GetPromptRequest {

  }

  private final static TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
  };

  /**
   * Deserializes a JSON string into a JSONRPCMessage object.
   * 
   * @param objectMapper The ObjectMapper instance to use for deserialization
   * @param jsonText     The JSON string to deserialize
   * @return A JSONRPCMessage instance using either the {@link JSONRPCRequest},
   *         {@link JSONRPCNotification}, or {@link JSONRPCResponse} classes.
   * @throws IOException              If there's an error during deserialization
   * @throws IllegalArgumentException If the JSON structure doesn't match any
   *                                  known
   *                                  message type
   */
  public static JSONRPCMessage deserializeJsonRpcMessage(ObjectMapper objectMapper, String jsonText)
      throws IOException {

    var map = objectMapper.readValue(jsonText, MAP_TYPE_REF);

    // Determine message type based on specific JSON structure
    if (map.containsKey("method") && map.containsKey("id")) {
      return objectMapper.convertValue(map, JSONRPCRequest.class);
    } else if (map.containsKey("method") && !map.containsKey("id")) {
      return objectMapper.convertValue(map, JSONRPCNotification.class);
    } else if (map.containsKey("result") || map.containsKey("error")) {
      return objectMapper.convertValue(map, JSONRPCResponse.class);
    }

    throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + jsonText);
  }

  // ---------------------------
  // JSON-RPC Message Types
  // ---------------------------
  public sealed interface JSONRPCMessage permits JSONRPCRequest, JSONRPCNotification, JSONRPCResponse {

    String jsonrpc();

  }

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record JSONRPCRequest( // @formatter:off
			@JsonProperty("jsonrpc") String jsonrpc,
			@JsonProperty("method") String method,
			@JsonProperty("id") Object id,
			@JsonProperty("params") Object params) implements JSONRPCMessage {
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record JSONRPCNotification( // @formatter:off
			@JsonProperty("jsonrpc") String jsonrpc,
			@JsonProperty("method") String method,
			@JsonProperty("params") Map<String, Object> params) implements JSONRPCMessage {
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record JSONRPCResponse( // @formatter:off
			@JsonProperty("jsonrpc") String jsonrpc,
			@JsonProperty("id") Object id,
			@JsonProperty("result") Object result,
			@JsonProperty("error") JSONRPCError error) implements JSONRPCMessage {

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		public record JSONRPCError(
			@JsonProperty("code") int code,
			@JsonProperty("message") String message,
			@JsonProperty("data") Object data) {
		}
	}// @formatter:on

  // ---------------------------
  // Initialization
  // ---------------------------
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record InitializeRequest( // @formatter:off
		@JsonProperty("protocolVersion") String protocolVersion,
		@JsonProperty("capabilities") ClientCapabilities capabilities,
		@JsonProperty("clientInfo") Implementation clientInfo) implements Request {		
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record InitializeResult( // @formatter:off
		@JsonProperty("protocolVersion") String protocolVersion,
		@JsonProperty("capabilities") ServerCapabilities capabilities,
		@JsonProperty("serverInfo") Implementation serverInfo,
		@JsonProperty("instructions") String instructions) {
	} // @formatter:on

  /**
   * Clients can implement additional features to enrich connected MCP servers
   * with
   * additional capabilities. These capabilities can be used to extend the
   * functionality
   * of the server, or to provide additional information to the server about the
   * client's capabilities.
   *
   * @param experimental WIP
   * @param roots        define the boundaries of where servers can operate within
   *                     the
   *                     filesystem, allowing them to understand which directories
   *                     and files they have
   *                     access to.
   * @param sampling     Provides a standardized way for servers to request LLM
   *                     sampling
   *                     (“completions” or “generations”) from language models via
   *                     clients.
   *
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ClientCapabilities( // @formatter:off
		@JsonProperty("experimental") Map<String, Object> experimental,
		@JsonProperty("roots") RootCapabilities roots,
		@JsonProperty("sampling") Sampling sampling) {

		/**
		 * Roots define the boundaries of where servers can operate within the filesystem,
		 * allowing them to understand which directories and files they have access to.
		 * Servers can request the list of roots from supporting clients and
		 * receive notifications when that list changes.
		 *
		 * @param listChanged Whether the client would send notification about roots
		 * 		  has changed since the last time the server checked.
		 */
		@JsonInclude(JsonInclude.Include.NON_ABSENT)			
		public record RootCapabilities(
			@JsonProperty("listChanged") Boolean listChanged) {
		}

		/**
		 * Provides a standardized way for servers to request LLM
	 	 * sampling (“completions” or “generations”) from language
		 * models via clients. This flow allows clients to maintain
		 * control over model access, selection, and permissions
		 * while enabling servers to leverage AI capabilities—with
		 * no server API keys necessary. Servers can request text or
		 * image-based interactions and optionally include context
		 * from MCP servers in their prompts.
		 */
		@JsonInclude(JsonInclude.Include.NON_ABSENT)			
		public record Sampling() {
		}
	}// @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ServerCapabilities( // @formatter:off
		@JsonProperty("experimental") Map<String, Object> experimental,
		@JsonProperty("logging") Object logging,
		@JsonProperty("prompts") PromptCapabilities prompts,
		@JsonProperty("resources") ResourceCapabilities resources,
		@JsonProperty("tools") ToolCapabilities tools) {
		
		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		public record PromptCapabilities(
			@JsonProperty("listChanged") Boolean listChanged) {
		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		public record ResourceCapabilities(
			@JsonProperty("subscribe") Boolean subscribe,
			@JsonProperty("listChanged") Boolean listChanged) {
		}

		@JsonInclude(JsonInclude.Include.NON_ABSENT)
		public record ToolCapabilities(
			@JsonProperty("listChanged") Boolean listChanged) {
		}
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record Implementation(// @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("version") String version) {
	} // @formatter:on

  // Existing Enums and Base Types (from previous implementation)
  public enum Role {// @formatter:off

		@JsonProperty("user") USER,
		@JsonProperty("assistant") ASSISTANT
	}// @formatter:on

  public enum LoggingLevel {// @formatter:off

		@JsonProperty("debug") DEBUG,
		@JsonProperty("info") INFO,
		@JsonProperty("notice") NOTICE,
		@JsonProperty("warning") WARNING,
		@JsonProperty("error") ERROR,
		@JsonProperty("critical") CRITICAL,
		@JsonProperty("alert") ALERT,
		@JsonProperty("emergency") EMERGENCY

	} // @formatter:on

  // ---------------------------
  // Resource Interfaces
  // ---------------------------
  /**
   * Base for objects that include optional annotations for the client. The client
   * can
   * use annotations to inform how objects are used or displayed
   */
  public interface Annotated {

    Annotations annotations();

  }

  /**
   * @param audience Describes who the intended customer of this object or data
   *                 is. It
   *                 can include multiple entries to indicate content useful for
   *                 multiple audiences
   *                 (e.g., `["user", "assistant"]`).
   * @param priority Describes how important this data is for operating the
   *                 server. A
   *                 value of 1 means "most important," and indicates that the
   *                 data is effectively
   *                 required, while 0 means "least important," and indicates that
   *                 the data is entirely
   *                 optional. It is a number between 0 and 1.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record Annotations( // @formatter:off
		@JsonProperty("audience") List<Role> audience,
		@JsonProperty("priority") Double priority) {
	} // @formatter:on

  /**
   * A known resource that the server is capable of reading.
   *
   * @param uri         the URI of the resource.
   * @param name        A human-readable name for this resource. This can be used
   *                    by clients to
   *                    populate UI elements.
   * @param description A description of what this resource represents. This can
   *                    be used
   *                    by clients to improve the LLM's understanding of available
   *                    resources. It can be
   *                    thought of like a "hint" to the model.
   * @param mimeType    The MIME type of this resource, if known.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Resource( // @formatter:off
		@JsonProperty("uri") String uri,
		@JsonProperty("name") String name,
		@JsonProperty("description") String description,
		@JsonProperty("mimeType") String mimeType,
		@JsonProperty("annotations") Annotations annotations) implements Annotated {
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ResourceTemplate( // @formatter:off
		@JsonProperty("uriTemplate") String uriTemplate,
		@JsonProperty("name") String name,
		@JsonProperty("description") String description,
		@JsonProperty("mimeType") String mimeType,
		@JsonProperty("annotations") Annotations annotations) implements Annotated {
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ListResourcesResult( // @formatter:off
		@JsonProperty("resources") List<Resource> resources,
		@JsonProperty("nextCursor") String nextCursor) {
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ListResourceTemplatesResult( // @formatter:off
		@JsonProperty("resourceTemplates") List<ResourceTemplate> resourceTemplates,
		@JsonProperty("nextCursor") String nextCursor) {
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ReadResourceRequest( // @formatter:off
		@JsonProperty("uri") String uri){
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ReadResourceResult( // @formatter:off
		@JsonProperty("contents") List<ResourceContents> contents){
	} // @formatter:on

  /**
   * Sent from the client to request resources/updated notifications from the
   * server
   * whenever a particular resource changes.
   *
   * @param uri the URI of the resource to subscribe to. The URI can use any
   *            protocol;
   *            it is up to the server how to interpret it.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record SubscribeRequest( // @formatter:off
		@JsonProperty("uri") String uri){
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record UnsubscribeRequest( // @formatter:off
		@JsonProperty("uri") String uri){
	} // @formatter:on

  /**
   * The contents of a specific resource or sub-resource.
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = As.PROPERTY)
  @JsonSubTypes({ @JsonSubTypes.Type(value = TextResourceContents.class, name = "text"),
      @JsonSubTypes.Type(value = BlobResourceContents.class, name = "blob") })
  public sealed interface ResourceContents permits TextResourceContents, BlobResourceContents {

    /**
     * The URI of this resource.
     * 
     * @return the URI of this resource.
     */
    String uri();

    /**
     * The MIME type of this resource.
     * 
     * @return the MIME type of this resource.
     */
    String mimeType();

  }

  /**
   * Text contents of a resource.
   *
   * @param uri      the URI of this resource.
   * @param mimeType the MIME type of this resource.
   * @param text     the text of the resource. This must only be set if the
   *                 resource can
   *                 actually be represented as text (not binary data).
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TextResourceContents( // @formatter:off
		@JsonProperty("uri") String uri,
		@JsonProperty("mimeType") String mimeType,
		@JsonProperty("text") String text) implements ResourceContents {
	} // @formatter:on

  /**
   * Binary contents of a resource.
   *
   * @param uri      the URI of this resource.
   * @param mimeType the MIME type of this resource.
   * @param blob     a base64-encoded string representing the binary data of the
   *                 resource.
   *                 This must only be set if the resource can actually be
   *                 represented as binary data
   *                 (not text).
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record BlobResourceContents( // @formatter:off
		@JsonProperty("uri") String uri,
		@JsonProperty("mimeType") String mimeType,
		@JsonProperty("blob") String blob) implements ResourceContents {
	} // @formatter:on

  // ---------------------------
  // Prompt Interfaces
  // ---------------------------
  /**
   * A prompt or prompt template that the server offers.
   *
   * @param name        The name of the prompt or prompt template.
   * @param description An optional description of what this prompt provides.
   * @param arguments   A list of arguments to use for templating the prompt.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record Prompt( // @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("description") String description,
		@JsonProperty("arguments") List<PromptArgument> arguments) {
	} // @formatter:on

  /**
   * Describes an argument that a prompt can accept.
   *
   * @param name        The name of the argument.
   * @param description A human-readable description of the argument.
   * @param required    Whether this argument must be provided.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record PromptArgument( // @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("description") String description,
		@JsonProperty("required") Boolean required) {
	}// @formatter:on

  /**
   * Describes a message returned as part of a prompt.
   *
   * This is similar to `SamplingMessage`, but also supports the embedding of
   * resources
   * from the MCP server.
   *
   * @param role    The sender or recipient of messages and data in a
   *                conversation.
   * @param content The content of the message of type {@link Content}.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record PromptMessage( // @formatter:off
		@JsonProperty("role") Role role,
		@JsonProperty("content") Content content) {
	} // @formatter:on

  /**
   * The server's response to a prompts/list request from the client.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ListPromptsResult( // @formatter:off
		@JsonProperty("prompts") List<Prompt> prompts,
		@JsonProperty("nextCursor") String nextCursor) {
	}// @formatter:on

  /**
   * Used by the client to get a prompt provided by the server.
   *
   * @param name      The name of the prompt or prompt template.
   * @param arguments Arguments to use for templating the prompt.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record GetPromptRequest(// @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("arguments") Map<String, Object> arguments) implements Request {
	}// @formatter:off

	/**
	 * The server's response to a prompts/get request from the client.
	 *
	 * @param description An optional description for the prompt.
	 * @param messages A list of messages to display as part of the prompt.
	 */
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	public record GetPromptResult( // @formatter:off
		@JsonProperty("description") String description,
		@JsonProperty("messages") List<PromptMessage> messages) {
	} // @formatter:on

  // ---------------------------
  // Tool Interfaces
  // ---------------------------
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ListToolsResult( // @formatter:off
		@JsonProperty("tools") List<Tool> tools,
		@JsonProperty("nextCursor") String nextCursor) {
	}// @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record Tool( // @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("description") String description,
		@JsonProperty("inputSchema") Map<String, Object> inputSchema) {
	} // @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record CallToolRequest(// @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("arguments") Map<String, Object> arguments) implements Request {
	}// @formatter:off

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	public record CallToolResult( // @formatter:off
		@JsonProperty("content") List<Content> content,
		@JsonProperty("isError") Boolean isError) {
	} // @formatter:on

  // ---------------------------
  // Sampling Interfaces
  // ---------------------------
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record SamplingMessage(// @formatter:off
		@JsonProperty("role") Role role,
		@JsonProperty("content") Content content) {
	} // @formatter:on

  // Sampling and Message Creation
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record CreateMessageRequest(// @formatter:off
		@JsonProperty("messages") List<SamplingMessage> messages,
		@JsonProperty("modelPreferences") ModelPreferences modelPreferences,
		@JsonProperty("systemPrompt") String systemPrompt,
		@JsonProperty("includeContext") ContextInclusionStrategy includeContext,
		@JsonProperty("temperature") Double temperature,
		@JsonProperty("maxTokens") int maxTokens,
		@JsonProperty("stopSequences") List<String> stopSequences, 			
		@JsonProperty("metadata") Map<String, Object> metadata) implements Request {

		public enum ContextInclusionStrategy {
			@JsonProperty("none") NONE,
			@JsonProperty("this_server") THIS_SERVER,
			@JsonProperty("all_server") ALL_SERVERS
		}
	}// @formatter:on

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record CreateMessageResult(Role role, Content content, String model, StopReason stopReason) {
    public enum StopReason {

      // @formatter:off
			@JsonProperty("end_turn") END_TURN,
			@JsonProperty("stop_sequence") STOP_SEQUENCE,
			@JsonProperty("max_tokens") MAX_TOKENS
		} // @formatter:on
  }

  // ---------------------------
  // Model Preferences
  // ---------------------------
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ModelPreferences(List<ModelHint> hints, Double costPriority, Double speedPriority,
      Double intelligencePriority) {
  }

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ModelHint(String name) {
  }

  // ---------------------------
  // Pagination Interfaces
  // ---------------------------
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record PaginatedRequest(@JsonProperty("cursor") String cursor) {
  }

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record PaginatedResult(@JsonProperty("nextCursor") String nextCursor) {
  }

  // ---------------------------
  // Progress and Logging
  // ---------------------------
  public record ProgressNotification(// @formatter:off
		@JsonProperty("progressToken") String progressToken,
		@JsonProperty("progress") double progress,
		@JsonProperty("total") Double total) {
	}// @formatter:on

  public record LoggingMessageNotification(// @formatter:off
		@JsonProperty("level") LoggingLevel level,
		@JsonProperty("logger") String logger,
		@JsonProperty("data") Object data) {
	}// @formatter:on

  // ---------------------------
  // Autocomplete
  // ---------------------------
  public record CompleteRequest(PromptOrResourceReference ref, CompleteArgument argument) implements Request {
    public sealed interface PromptOrResourceReference permits PromptReference, ResourceReference {

      String type();

    }

    public record PromptReference(// @formatter:off
			@JsonProperty("type") String type,
			@JsonProperty("name") String name) implements PromptOrResourceReference {
		}// @formatter:on

    public record ResourceReference(// @formatter:off
			@JsonProperty("type") String type,
			@JsonProperty("uri") String uri) implements PromptOrResourceReference {
		}// @formatter:on

    public record CompleteArgument(// @formatter:off
			@JsonProperty("name") String name,
			@JsonProperty("value") String value) {
		}// @formatter:on
  }

  public record CompleteResult(CompleteCompletion completion) {
    public record CompleteCompletion(// @formatter:off
			@JsonProperty("values") List<String> values,
			@JsonProperty("total") Integer total,
			@JsonProperty("hasMore") Boolean hasMore) {
		}// @formatter:on
  }

  // ---------------------------
  // Content Types
  // ---------------------------
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
  @JsonSubTypes({ @JsonSubTypes.Type(value = TextContent.class, name = "text"),
      @JsonSubTypes.Type(value = ImageContent.class, name = "image"),
      @JsonSubTypes.Type(value = EmbeddedResource.class, name = "resource") })
  public sealed interface Content permits TextContent, ImageContent, EmbeddedResource {

    String type();

  }

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record TextContent( // @formatter:off
		@JsonProperty("audience") List<Role> audience,
		@JsonProperty("priority") Double priority,
		@JsonProperty("type") String type,
		@JsonProperty("text") String text) implements Content { // @formatter:on

    public TextContent {
      type = "text";
    }

    public String type() {
      return type;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ImageContent( // @formatter:off
		@JsonProperty("audience") List<Role> audience,
		@JsonProperty("priority") Double priority,
		@JsonProperty("type") String type,
		@JsonProperty("data") String data,
		@JsonProperty("mimeType") String mimeType) implements Content { // @formatter:on

    public ImageContent {
      type = "image";
    }

    public String type() {
      return type;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record EmbeddedResource( // @formatter:off
		@JsonProperty("audience") List<Role> audience,
		@JsonProperty("priority") Double priority,
		@JsonProperty("type") String type,
		@JsonProperty("resource") ResourceContents resource) implements Content { // @formatter:on

    public EmbeddedResource {
      type = "resource";
    }

    public String type() {
      return type;
    }
  }

  // ---------------------------
  // Roots
  // ---------------------------
  /**
   * Represents a root directory or file that the server can operate on.
   *
   * @param uri  The URI identifying the root. This *must* start with file:// for
   *             now.
   *             This restriction may be relaxed in future versions of the
   *             protocol to allow other
   *             URI schemes.
   * @param name An optional name for the root. This can be used to provide a
   *             human-readable identifier for the root, which may be useful for
   *             display purposes or
   *             for referencing the root in other parts of the application.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record Root( // @formatter:off
		@JsonProperty("uri") String uri,
		@JsonProperty("name") String name) {
	} // @formatter:on

  /**
   * The client's response to a roots/list request from the server. This result
   * contains
   * an array of Root objects, each representing a root directory or file that the
   * server can operate on.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  public record ListRootsResult( // @formatter:off
		@JsonProperty("roots") List<Root> roots) {
	} // @formatter:on

}