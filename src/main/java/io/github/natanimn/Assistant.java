package io.github.natanimn;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.CallbackHandler;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ChatMemberStatus;
import io.github.natanimn.telebof.enums.ChatType;
import io.github.natanimn.telebof.enums.MessageType;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.chat_and_user.ChatPermissions;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.telebof.types.ReplyParameters;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;

public class Assistant {
    private static final String ASK = """
    Sorry, your question is not well formulated. Please, be clear and try to follow the guidelines in the link below.

    [How do I ask a good question?](https://stackoverflow.com/help/how-to-ask)
    """;

    private static final String RES = """
    *Good Java resources for learning*

    • [Java Documentation](https://docs.oracle.com/en/java/) - Official Docs
    • [Telebof Documentation](https://natanimn.github.io/telebof) - Library Docs
    • [Learn Java](https://www.learnjavaonline.org/) - Interactive
    • [Java Video Tutorials](https://www.youtube.com/watch?v=bm0OyhwFDuY&list=PLsyeobzWxl7pe_IiTfNyr55kwJPWbgxB5&index=1) - Video
 
    """;

    private static final String LEARN = "Your issue is not related to Java programming. Please, learn more Java and try again.\n" + RES;

    private static final String RTD = "Please, read the docs: https://natanimn.github.io/telebof";
    private static final String FMT = """
    Please format your code with triple backticks to make it more readable.
    <code>```your code here```</code>
    """;

    private static final String LOCKED = "🔒 Chat has been locked. Send /unlock to unlock.";
    private static final String UNLOCKED = "🔓 Chat has been unlocked.";

    private static final String RULES = """
    \uD83D\uDCDC *Rules*
    
    1. Use English only
    2. Keep on topic
    3. Do not send any inappropriate content, and report to admin if you found it here.
    4. Read available resources before asking a question.
    5. Be nice to everyone
    6. Make your question clear
    """;

    static String START = """
            <b>Welcome to @telebof_bot</b>
            
            /donate - Donate to the project
            /rules  - Read chat rules
            
            <b>Github</b>: https://github.com/natanimn/telebof
            <b>Docs</b>: https://natanimn.github.io/telebof
            
            Write <code>@telebof_bot methodName</code> to get available telegram methods in the library, or use the bellow button.
            """;

    @MessageHandler(commands = "ask")
    private void ask(BotContext context, Message message) {
        replyAndDelete(context, message, ASK);
    }

    @MessageHandler(commands = "res")
    private void res(BotContext context, Message message) {
        replyAndDelete(context, message, RES);
    }

    @MessageHandler(commands = "learn")
    private void learn(BotContext context, Message message) {
        replyAndDelete(context, message, LEARN);
    }

    @MessageHandler(commands = "rules")
    private void rules(BotContext context, Message message) {
        replyAndDelete(context, message, RULES);
    }

    @MessageHandler(commands = "rtd")
    private void rtd(BotContext context, Message message) {
        replyAndDelete(context, message, RTD);
    }

    @MessageHandler(commands = "fmt")
    private void fmt(BotContext context, Message message) {
        if (!isAdmin(context, message)) return;
        context.deleteMessage(message.chat.id, message.message_id).exec();
        var reply_id = message.reply_to_message != null? message.reply_to_message.message_id: message.message_id;

        context.sendMessage(message.chat.id, FMT)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyParameters(new ReplyParameters(reply_id).allowSendingWithoutReply(true))
                .exec();
    }

    @MessageHandler(commands = "ban", chatType = {ChatType.SUPERGROUP, ChatType.GROUP})
    private void ban(BotContext context, Message message) {
        if (message.reply_to_message == null) return;
        if (!isAdmin(context, message)) return;
        var reply = message.reply_to_message;
        if (isAdmin(context, reply)) {
            var m = context.sendMessage(message.chat.id, "Sorry, I don't ban administrators").exec();
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    context.deleteMessage(message.chat.id, m.message_id).exec();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            return;
        }

        context.banChatMember(message.chat.id, reply.from.id).exec();

        context.sendMessage(message.chat.id, "<b>Banned user</b>" + reply.from.mention() )
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("✅ Unban", "unban:" + reply.from.id)
                }))
                .parseMode(ParseMode.HTML)
                .exec();
    }

    @MessageHandler(commands = "kick", chatType = {ChatType.SUPERGROUP, ChatType.GROUP})
    private void kick(BotContext context, Message message) {
        if (!isAdmin(context, message)) return;
        if (message.reply_to_message == null) return;

        var reply = message.reply_to_message;
        if (isAdmin(context, reply)) {
            var m = context.sendMessage(message.chat.id, "Sorry, I don't kick administrators").exec();
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    context.deleteMessage(message.chat.id, m.message_id).exec();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            return;
        }

        long untilTime = System.currentTimeMillis() / 1000 + 60;
        context.banChatMember(message.chat.id, reply.from.id).untilDate((int) untilTime).exec();

        context.sendMessage(message.chat.id, "<b>Kicked </b>" + reply.from.mention() + ". <i>They can rejoin</i>")
                .parseMode(ParseMode.HTML)
                .exec();

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                context.unbanChatMember(message.chat.id, reply.from.id).exec();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @MessageHandler(commands = "lock", chatType = {ChatType.SUPERGROUP, ChatType.GROUP})
    private void lock(BotContext context, Message message) {
        if (!isAdmin(context, message)) return;
        context.setChatPermissions(message.chat.id, new ChatPermissions().canSendMessages(false)).exec();
        replyAndDelete(context, message, LOCKED);
    }

    @MessageHandler(commands = "unlock", chatType = {ChatType.SUPERGROUP, ChatType.GROUP})
    private void unlock(BotContext context, Message message) {
        if (!isAdmin(context, message)) return;
        context.setChatPermissions(message.chat.id, new ChatPermissions()
                .canSendMessages(true)
                .canSendOtherMessages(true)
                .canAddWebPagePreviews(true)
                .canSendPolls(true)
                .canSendAudios(true)
                .canSendVideos(true)
                .canSendPhotos(true)
        ).exec();
        replyAndDelete(context, message, UNLOCKED);
    }

    @MessageHandler(commands = "help")
    private void help(BotContext context, Message message) {
        context.deleteMessage(message.chat.id, message.message_id).exec();
        var reply_id = message.reply_to_message != null? message.reply_to_message.message_id: message.message_id;
        context.sendMessage(message.chat.id, HELP)
                .replyParameters(new ReplyParameters(reply_id).allowSendingWithoutReply(true))
                .parseMode(ParseMode.HTML)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Remove Help", "remove:" + message.from.id)
                }))
                .exec();

    }

    @CallbackHandler(regex = "^(remove|unban):(\\d+)")
    private void cbQuery(BotContext context, CallbackQuery callbackQuery) {
        String[] parts = callbackQuery.data.split(":");
        String action = parts[0];
        long userId = Long.parseLong(parts[1]);

        if ("unban".equals(action)) {
            if (callbackQuery.from.id != 5213764043L) {
                context.answerCallbackQuery(callbackQuery.id, "Only @natiprado can unban users")
                        .showAlert(true)
                        .exec();
                return;
            }

            context.unbanChatMember(callbackQuery.message.chat.id, userId).exec();

            context.editMessageText(
                    "~~" + callbackQuery.message.text + "~~\n\n**Unbanned**",
                    callbackQuery.message.chat.id,
                    callbackQuery.message.message_id)
                    .parseMode(ParseMode.MARKDOWN)
                    .exec();
        }

        if ("remove".equals(action)) {
            callbackQuery.message.from.id = userId;
            if (callbackQuery.from.id == userId || isAdmin(context, callbackQuery.message)) {
                context.answerCallbackQuery(callbackQuery.id).exec();
                context.deleteMessage(callbackQuery.message.chat.id, callbackQuery.message.message_id).exec();
            } else {
                context.answerCallbackQuery(callbackQuery.id, "Only Admins can remove the help messages.")
                        .showAlert(true)
                        .exec();
            }
        }
    }

    @MessageHandler(type = MessageType.NEW_CHAT_MEMBER)
    private void welcome(BotContext context, Message message){
        context.deleteMessage(message.chat.id, message.message_id).exec();

        for (var user: message.new_chat_members){
            if (user.is_bot == true) {
                context.banChatMember(message.chat.id, user.id).exec();
                continue;
            }
            context.restrictChatMember(message.chat.id, user.id, new ChatPermissions()).exec();
            context.sendMessage(message.chat.id, "\uD83D\uDC4B Hi %s, Welcome to telebof chat.\n\nYou have been muted until you read the rules.".formatted(user.mention()))
                    .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton("Read Rules", "read_rules:%d".formatted(user.id))}))
                    .parseMode(ParseMode.HTML)
                    .exec();

        }
    }

    @CallbackHandler(regex = "read_rules:\\d+")
    private void readRules(BotContext context, CallbackQuery callback) {
        var id = Long.parseLong(callback.data.split(":")[1]);

        if (id != callback.from.id)
            context.answerCallbackQuery(callback.id, "This message is not for you")
                    .showAlert(true)
                    .exec();
        else
            context.editMessageText(RULES, callback.message.chat.id, callback.message.message_id)
                    .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                            new InlineKeyboardButton("I have read", "i_read:%d".formatted(id))}))
                    .parseMode(ParseMode.MARKDOWN)
                    .exec();
    }

    @CallbackHandler(regex = "i_read:\\d+")
    private void iReadRules(BotContext context, CallbackQuery callback){
        var id = Long.parseLong(callback.data.split(":")[1]);

        if (id != callback.from.id)
            context.answerCallbackQuery(callback.id, "This message is not for you")
                    .showAlert(true)
                    .exec();
        else {
            context.deleteMessage(callback.message.chat.id, callback.message.message_id).exec();
            context.restrictChatMember(
                    callback.message.chat.id,
                    callback.from.id,
                    new ChatPermissions()
                            .canSendMessages(true)
                            .canSendOtherMessages(true)
                            .canAddWebPagePreviews(true)
                            .canSendPolls(true)
                            .canInviteUsers(true)
                            .canPinMessages(true)
            ).exec();
        }
    }

    @MessageHandler(type = MessageType.LEFT_CHAT_MEMBER)
    private void delete(BotContext context, Message message){
        context.deleteMessage(message.chat.id, message.message_id).exec();
    }

    private void replyAndDelete(BotContext context, Message message, String text) {
        context.deleteMessage(message.chat.id, message.message_id).exec();
        var reply_id = message.reply_to_message != null ? message.reply_to_message.message_id : message.message_id;
        context.sendMessage(message.chat.id, text)
                .disableWebPagePreview(true)
                .parseMode(ParseMode.MARKDOWN)
                .replyParameters(new ReplyParameters(reply_id).allowSendingWithoutReply(true))
                .exec();
    }

    private boolean isAdmin(BotContext context, Message message) {
        try {
            var member = context.getChatMember(message.chat.id, message.from.id).exec();
            return member.status == ChatMemberStatus.ADMINISTRATOR ||
                    member.status == ChatMemberStatus.CREATOR;
        } catch (Exception e) {
            return false;
        }
    }

    private static final String HELP = """
    <b>🕹 List of available commands</b>

    • /ask - How to ask questions
    • /res - Good Java resources
    • /learn - Tell to learn Java
    • /rules - Show community rules
    • /rtd - Tell to RTD (gentle)
    • /fmt* - Tell to format code
    • /ban* - Ban a user in chat
    • /kick* - Kick (they can rejoin)
    • /lock* - Lock the Chat
    • /unlock* - Unlock the Chat
    • /help - Show this message

    `*` Administrators only
    """;

    @MessageHandler(commands = "start", chatType = ChatType.PRIVATE)
    void start(BotContext context, Message message){
        var split = message.text.split(" ");

        if (split.length == 2){
            if (split[1].equals("donate"))
                new Donation().donate(context, message);
        } else context.sendMessage(message.chat.id, START)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("\uD83D\uDD0E Search methods").switchInlineQueryCurrentChat("")
                }))
                .parseMode(ParseMode.HTML)
                .exec();
    }

    @MessageHandler(commands = "ping", chatType = ChatType.PRIVATE)
    void ping(BotContext context, Message message){
        long receivedAt = System.currentTimeMillis();
        var msg     = context.sendMessage(message.chat.id, "...").exec();
        long sentAt = System.currentTimeMillis();
        var latency = sentAt - receivedAt;
        context.editMessageText("<code>Pong! %d ms</code>".formatted(latency), message.chat.id, msg.message_id)
                .parseMode(ParseMode.HTML)
                .exec();
    }


    public static void main(String[] args){
        var bot = new BotClient(args[0]);
        bot.addHandler(new Assistant());
        bot.addHandler(new Inline());
        bot.addHandler(new Donation());

        System.out.println("Bot started..");
        bot.startPolling();
    }
}