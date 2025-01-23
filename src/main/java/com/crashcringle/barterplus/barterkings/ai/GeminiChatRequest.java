package com.crashcringle.barterplus.barterkings.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class GeminiChatRequest {

    private final ObjectMapper objectMapper;
    private final ObjectNode request;
    private List<GeminiChatMessage> messages;
    private final ObjectNode tools;
    private final ArrayNode functionDeclarations;
    private final ObjectNode generationConfig;

    public GeminiChatRequest() {
        this.objectMapper = new ObjectMapper();
        this.request = objectMapper.createObjectNode();
        this.functionDeclarations = objectMapper.createArrayNode();
        this.generationConfig = objectMapper.createObjectNode();
        this.tools = objectMapper.createObjectNode();

        // Initialize default values
        generationConfig.put("temperature", 1);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 3192);
        generationConfig.put("responseMimeType", "text/plain");

        ObjectNode toolConfig = objectMapper.createObjectNode();
        ObjectNode functionCallingConfig = objectMapper.createObjectNode();

        //AUTO: The default model behavior. The model decides to predict either a function call or a natural language response.
        //ANY: The model is constrained to always predict a function call. If allowed_function_names is not provided, the model picks from all of the available function declarations. If allowed_function_names is provided, the model picks from the set of allowed functions.
        //NONE: The model won't predict a function call. In this case, the model behavior is the same as if you don't pass any function declarations.

        functionCallingConfig.put("mode", "AUTO");

        toolConfig.set("functionCallingConfig", functionCallingConfig);
        tools.set("functionDeclarations", functionDeclarations);
        request.set("toolConfig", toolConfig);
    }

    public GeminiChatRequest setModel(String model) {
        request.put("model", model);
        return this;
    }


    public GeminiChatRequest addTool(String name, String description, ObjectNode parameters) {
        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", name);
        function.put("description", description);
        if (parameters != null) {
            function.set("parameters", parameters);
        }
        functionDeclarations.add(function);
        return this;
    }

    public GeminiChatRequest setTemperature(double temperature) {
        generationConfig.put("temperature", temperature);
        return this;
    }

    public GeminiChatRequest setTopP(double topP) {
        generationConfig.put("topP", topP);
        return this;
    }


    public GeminiChatRequest setMessages(List<GeminiChatMessage> messages) {
        this.messages = messages;
        return this;
    }

    public List<GeminiChatMessage> getMessages() {
        return messages;
    }

    public GeminiChatRequest setTopK(int topK) {
        generationConfig.put("topK", topK);
        return this;
    }

    public GeminiChatRequest setMaxOutputTokens(int maxOutputTokens) {
        generationConfig.put("maxOutputTokens", maxOutputTokens);
        return this;
    }

    public ObjectNode build() {
        ArrayNode contentsArray = objectMapper.createArrayNode();
        for (GeminiChatMessage message : messages) {
            contentsArray.add(message.toObjectNode(objectMapper));
        }
        request.set("contents", contentsArray);
        request.set("tools", tools);
        request.set("generationConfig", generationConfig);
        return request;
    }
}
