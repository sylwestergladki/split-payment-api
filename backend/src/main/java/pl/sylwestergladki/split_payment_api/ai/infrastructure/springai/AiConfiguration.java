package pl.sylwestergladki.split_payment_api.ai.infrastructure.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration {

    @Bean
    public ChatClient expenseChatClient(
            ChatClient.Builder builder
    ) {
        return builder.build();
    }
}
