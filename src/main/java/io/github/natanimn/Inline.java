package io.github.natanimn;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.InlineHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.inline.InlineQueryResult;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;
import io.github.natanimn.telebof.types.updates.InlineQuery;
import io.github.natanimn.util.Docs;

import java.io.IOException;

public class Inline {

    @InlineHandler
    private void searchInline(BotContext context, InlineQuery query) {
        if (query.query.isEmpty()) {
            context.answerInlineQuery(query.id, new InlineQueryResult[]{
                            new InlineQueryResultArticle(
                                    "1",
                                    "\uD83D\uDCD6 Full documentation",
                                    new InputTextMessageContent("**\uD83D\uDCD6 Full documentation**\n\n__Full documentation is available @ https://natanimn.github.io/telebof")
                                            .parseMode(ParseMode.MARKDOWN)
                            )
                                    .description("Telebof is a modern Java wrapper for the Telegram Bot API, designed to make building bots fast, simple, and intuitive.")
                                    .thumbnailUrl("https://emoji.beeimg.com/\uD83D\uDCD6"),

                            new InlineQueryResultArticle(
                                    "2",
                                    "\uD83D\uDCD7 Examples",
                                    new InputTextMessageContent("**\uD83D\uDCD7 Examples**\n\nAvailable examples can be found @ https://natanimn.github.io/telebof/examples")
                                            .parseMode(ParseMode.MARKDOWN)
                            )
                                    .description("All examples are production-ready and can be run immediately after setting up your bot token credentials.")
                                    .thumbnailUrl("https://emoji.beeimg.com/\uD83D\uDCD7"),

                            new InlineQueryResultArticle(
                                    "3",
                                    "\uD83D\uDE07 Donate",
                                    new InputTextMessageContent("\uD83D\uDE07 Donate to the project. [Click here](t.me/telebof_bot?start=donate)").parseMode(ParseMode.MARKDOWN))
                                    .description("Support the development of Telebof.")
                                    .thumbnailUrl("https://emoji.beeimg.com/\uD83D\uDE07")

                    })
                    .cacheTime(10)
                    .exec();
        } else {
            try {
                var results = Docs.searchMethods(query.query);
                if (results.isEmpty())
                    results.add(new InlineQueryResultArticle(
                            "1",
                            "404 Not found",
                            new InputTextMessageContent("Search not found")
                    ));

                context.answerInlineQuery(query.id, results.toArray(InlineQueryResult[]::new))
                        .cacheTime(25)
                        .exec();


            } catch (IOException e) {
                var result = new InlineQueryResultArticle(
                        "1",
                        "Server error",
                        new InputTextMessageContent("Server error — Please try again later")
                );
                context.answerInlineQuery(query.id, new InlineQueryResult[]{result})
                        .cacheTime(25)
                        .exec();
            }
        }
    }

}
