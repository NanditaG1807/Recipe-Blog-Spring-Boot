
package com.main.recipeblog.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OllamaService {

    private final ChatClient chatClient;

    public OllamaService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String ask(String question, String recipeData) {

        String systemPrompt = """
                You are Maja Recipe Blog's multilingual recipe assistant.

                Reply in the same language as the user's question.
                Support Marathi, Hindi and English.

                Use the supplied database recipe information when it
                contains a relevant recipe.

                If the database contains no relevant recipe, clearly
                tell the user that it was not found in the blog database.
                You may offer general cooking guidance, but do not
                pretend that it came from the database or internet.

                Explain ingredients and cooking steps clearly.
                Never invent database facts.
                """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user("""
                        User question:
                        %s

                        Recipe information from the blog database:
                        %s
                        """.formatted(question, recipeData))
                .call()
                .content();
    }
}
