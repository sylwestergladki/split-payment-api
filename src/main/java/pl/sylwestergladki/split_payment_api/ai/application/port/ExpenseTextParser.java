package pl.sylwestergladki.split_payment_api.ai.application.port;

import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;

public interface ExpenseTextParser {

    ParsedExpense parse(
            String text,
            ExpenseParsingContext context
    );
}
