package com.brekelov.entity;

/**
 * Response for the init request
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "result": {
 *     "protocolVersion": "2024-11-05",
 *     "capabilities": {
 *       "logging": {},
 *       "prompts": {
 *         "listChanged": true
 *       },
 *       "resources": {
 *         "subscribe": true,
 *         "listChanged": true
 *       },
 *       "tools": {
 *         "listChanged": true
 *       }
 *     },
 *     "serverInfo": {
 *       "name": "ExampleServer",
 *       "version": "1.0.0"
 *     }
 *   }
 * }
 */
public class MCPResponse {
}