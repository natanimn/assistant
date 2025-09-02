package io.github.natanimn.util;

import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.inline.InlineQueryResult;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;
import io.github.natanimn.telebof.types.media_and_service.LinkPreviewOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Docs {

    private static final String BASE_URL = "https://natanimn.github.io/telebof-api/io/github/natanimn/telebof/BotContext.html";

    public static List<InlineQueryResult> searchMethods(String query) throws IOException {
        var doc = Jsoup.connect(BASE_URL).get();
        var html = doc.html();

        var results = new ArrayList<InlineQueryResult>();
        if (html.isEmpty() || query == null || query.isEmpty()) {
            results.add(new InlineQueryResultArticle("1", "404 Not found", new InputTextMessageContent("Search not found")));
            return results;
        }

        String queryLower = query.toLowerCase();
        Elements methods = doc.select("section.detail");
        int i = 1;
        for (Element method : methods) {
            Element h3 = method.selectFirst("h3");
            Element block = method.selectFirst("div.block");

            if (h3 == null || block == null) continue;

            String methodName = h3.text();
            String description = block.text();
            String methodId = method.attr("id");

            if (methodName.toLowerCase().contains(queryLower)) {
                String url = BASE_URL + "#" + methodId;

                results.add(
                        new InlineQueryResultArticle(
                            Integer.toString(i),
                            "\uD83D\uDCD8 " + methodName,
                            new InputTextMessageContent(
                                    String.format("""
                                            \uD83D\uDCD8 <b>Telebof method</b>
                                            
                                            🔸 <a href='%s'>%s</a> — <i>%s</i>""", url, methodName, description)
                            )
                                    .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true))
                                    .parseMode(ParseMode.HTML)
                    )
                                .description(description)
                                .thumbnailUrl("https://emoji.beeimg.com/\uD83D\uDCD8")
                                .thumbnailHeight(50)
                );
                i++;
            }
        }
        
        return results.size() > 50? results.subList(0, 50): results;
    }
}
