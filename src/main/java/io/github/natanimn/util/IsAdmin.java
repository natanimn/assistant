package io.github.natanimn.util;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.enums.ChatMemberStatus;
import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.Update;

public class IsAdmin implements CustomFilter {
    private BotClient client;
    public IsAdmin(BotClient client){
        this.client = client;
    }
    @Override
    public boolean check(Update update) {
        if (update.message.from.id == 5213764043L) return true; // @natiprado

//        client.context.deleteMessage(update.message.chat.id, update.message.message_id).exec();
        var user = client.context.getChatMember(update.message.chat.id, update.message.from.id).exec();
        return  (user.status == ChatMemberStatus.ADMINISTRATOR || user.status == ChatMemberStatus.CREATOR);
    }
}
