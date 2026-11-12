package pl.sylwestergladki.split_payment_api.ai.infrastructure.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseTextParser;

@Component
public class SpringAiExpenseTextParser implements ExpenseTextParser {
    private final ChatClient chatClient;

    public SpringAiExpenseTextParser(
            ChatClient expenseChatClient
    ) {
        this.chatClient = expenseChatClient;
    }

    @Override
    public ParsedExpense parse(
            String text,
            ExpenseParsingContext context
    ) {
        String members = context.members().stream()
                .map(member -> "%s: %s".formatted(
                        member.userId().value(),
                        member.displayName()
                ))
                .collect(java.util.stream.Collectors.joining("\n"));

        String prompt = """
                You are an expense parsing assistant.

                Extract structured expense information from
                the user's message.

                Rules:
                - Return a short expense description.
                - Extract the total amount.
                - Currency must be PLN.
                - Use "me" when the payer or participant
                  refers to the requester.
                - Otherwise, use the exact display name
                  from the provided group members.
                - Never invent group members.
                - Never invent an amount.
                - If a required value is unknown,
                  return null or an empty list.
                - Do not calculate expense splits.

                Requester ID:
                %s

                Group members:
                %s

                Expense text:
                %s
                """.formatted(
                context.requesterId().value(),
                members,
                text
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(ParsedExpense.class);
    }
}