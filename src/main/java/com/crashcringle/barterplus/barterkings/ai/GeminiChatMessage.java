package com.crashcringle.barterplus.barterkings.ai;

import com.crashcringle.barterplus.BarterPlus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GeminiChatMessage {

    private String role;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tool_calls")
    private List<JsonNode> toolCalls;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tool_call_id")
    private String toolCallId;

    public GeminiChatMessage() {
    }

    public GeminiChatMessage(String role, String message, List<JsonNode> toolCalls, String toolCallId) {
        this.role = role;
        this.message = message;
        this.toolCalls = toolCalls;
        this.toolCallId = toolCallId;
    }

    public GeminiChatMessage(String role,List<JsonNode> toolCalls, String toolCallId) {
        this.role = role;
        this.message = "";
        this.toolCalls = toolCalls;
        this.toolCallId = toolCallId;
    }

    public GeminiChatMessage(String role, String message) {
        this.role = role;
        this.message = message;
        this.toolCalls = new ArrayList<>();
        this.toolCallId = "";
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<JsonNode> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<JsonNode> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public static GeminiChatMessage fromJson(JsonNode jsonNode) {
        GeminiChatMessage message = new GeminiChatMessage();
        message.setRole(jsonNode.get("role").asText());
        if (jsonNode.has("content")) {
            message.setMessage(jsonNode.get("message").asText());
        }
        if (jsonNode.has("tool_calls")) {
            message.setToolCalls((List<JsonNode>) jsonNode.get("tool_calls"));
        }
        if (jsonNode.has("tool_call_id")) {
            message.setToolCallId(jsonNode.get("tool_call_id").asText());
        }
        return message;
    }

    public JsonNode toObjectNode(ObjectMapper objectMapper) {
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();

        if (role.equalsIgnoreCase("tool")) {
            BarterPlus.inst().getLogger().info("Tool Message: " + message);
            ObjectNode functionResponse = new ObjectMapper().createObjectNode();
            functionResponse.set("functionResponse", convertToolResponse(toolCallId, message));
            role = "user";
        }
        content.put("role", role);
        if (toolCalls != null)
            for (JsonNode toolCall : toolCalls) {
                // Parts will need a functionResponse field
                parts.add(convertToolCall(toolCall));
            }
        if (message != null && !message.isEmpty()) {
            parts.add(objectMapper.createObjectNode().put("text", message));
        }
        content.set("parts", parts);
        return content;
    }

    /*
        Parts should look like this with tool calls
        "parts": [{
          "functionCall": {
            "name": "find_theaters",
            "args": {
              "location": "Mountain View, CA",
              "movie": "Barbie"
            }
          }
        }]
     */
    public JsonNode convertToolCall(JsonNode toolCall) {
        ObjectNode functionCall = new ObjectMapper().createObjectNode();
        functionCall.set("functionCall", toolCall);
        return functionCall;
    }

    /*
        Parts should look like this with text
    "parts": [{
      "functionResponse": {
        "name": "find_theaters", (name of tool)
        "response": {
          "name": "find_theaters", (Name of the tool)
          "content": {
            "movie": "Barbie",
            "theaters": [{
              "name": "AMC Mountain View 16",
              "address": "2000 W El Camino Real, Mountain View, CA 94040"
            }, {
              "name": "Regal Edwards 14",
              "address": "245 Castro St, Mountain View, CA 94040"
            }]
          }
        }
      }
    }]
     */
    public JsonNode convertToolResponse(String tool, String response) {
        ObjectNode functionResponse = new ObjectMapper().createObjectNode();
        functionResponse.put("name", tool);
        ObjectNode responseNode = new ObjectMapper().createObjectNode();
        responseNode.put("name", tool);
        responseNode.set("content", new ObjectMapper().createObjectNode().put("response", response));
        functionResponse.set("response", responseNode);
        return functionResponse;
    }
}
