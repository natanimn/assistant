package io.github.natanimn;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.CallbackHandler;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.annotations.PreCheckoutHandler;
import io.github.natanimn.telebof.enums.ChatType;
import io.github.natanimn.telebof.enums.MessageType;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.telebof.types.payments.LabeledPrice;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;
import io.github.natanimn.telebof.types.updates.PreCheckoutQuery;

public class Donation {
    static String DONATE = """
                <b>☕️ Welcome to donation section</b>
                
                You can use the following option to donate:
                
                💠 <b>USDT (Trc20)</b>
                <code>TQKEymFBYTDJx7NcZ4wuSpkP9TaVGBAtbJ</code>
                
                💠 <b>Ton</b>
                <code>UQBHMGr5wpngPCTenVtmTCBuaJqv-GI9HWsd_bM5NVpRLu8g</code>
               
                👇 Press the bellow button to donate with a telegram star. 
                """;

    private InlineKeyboardMarkup getStarsButton(boolean addBack){
        var stars = new int[]{10, 25, 50, 100, 300, 500, 1000, 5000, 10_000};
        var inline = new InlineKeyboardMarkup();
        for (int i=0; i < stars.length - 1 ; i += 2){
            var ls = new InlineKeyboardButton[2];
            for (int j=0; j < 2; j++)
                ls[j] = new InlineKeyboardButton("⭐ " + stars[i + j], "donate_"+stars[i + j]);
            inline.addKeyboard(ls);
        }
        if (addBack) inline.addKeyboard(new InlineKeyboardButton("\uD83D\uDD19 Back", "back_donation"));

        return inline;
    }

    @MessageHandler(commands = "donate", chatType = ChatType.PRIVATE)
    void donate(BotContext context, Message message){
        context.sendMessage(message.chat.id, DONATE)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("⭐ Star Donation", "star")
                }))
                .parseMode(ParseMode.HTML)
                .exec();
    }

    @MessageHandler(commands = "donate", chatType = {ChatType.SUPERGROUP, ChatType.GROUP})
    void gpDonate(BotContext context, Message message){
        context.sendMessage(message.chat.id, "You can donate in private chat.")
                .replyMarkup(new InlineKeyboardMarkup(
                        new InlineKeyboardButton[]{
                                new InlineKeyboardButton("Donate in private").url("t.me/telebof_bot?start=donate")
                        }
                ))
                .exec();
    }

    @CallbackHandler(data = "star")
    @MessageHandler(commands = "star", chatType = ChatType.PRIVATE)
    void star(BotContext context, Object object){
        if (object instanceof Message)
            context.sendMessage(((Message) object).chat.id,"How many stars would you like to donate?")
                    .replyMarkup(getStarsButton(false))
                    .exec();
        else {
            var callback = ((CallbackQuery)object);
            context.answerCallbackQuery(callback.id).exec();
            context.editMessageText("How many stars would you like to donate?", callback.from.id, callback.message.message_id)
                    .replyMarkup(getStarsButton(true))
                    .exec();
        }
    }

    @CallbackHandler(data = "back_donation")
    void backDonation(BotContext context, CallbackQuery query){
        context.answerCallbackQuery(query.id).exec();

        context.editMessageText(DONATE, query.from.id, query.message.message_id)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("⭐ Star Donation", "star")
                }))
                .parseMode(ParseMode.HTML)
                .exec();

    }

    @CallbackHandler(regex = "donate_\\d+")
    void donateStar(BotContext context, CallbackQuery query){
        context.answerCallbackQuery(query.id).exec();

        int amount = Integer.parseInt(query.data.split("_")[1]);
        context.sendInvoice(
                query.message.chat.id,
                "Donation",
                "☕ Donation to support the continued development and maintenance of the Telebof Java open source library.",
                "donate_" + amount,
                "XTR",
                new LabeledPrice[]{new LabeledPrice("Donate", amount)}
        )
                .exec();
    }

    @PreCheckoutHandler(regex = "donate_\\d+")
    void preCheckout(BotContext context, PreCheckoutQuery query){
        context.answerPreCheckoutQuery(query.id, true).exec();
    }

    @MessageHandler(type = MessageType.SUCCESSFUL_PAYMENT)
    void acceptStar(BotContext context, Message message){
        context.sendMessage(message.chat.id,
                                """
                                <b>✅ Donation Received - Thank You!</b>
                                
                                💡 Thank you for your donation to the Telebof project! Your support is essential to our work and is greatly appreciated by our entire team.
                                """
                )
                .parseMode(ParseMode.HTML)
                .exec();
        context.refundStarPayment(message.from.id, message.successful_payment.telegram_payment_charge_id).exec();
    }

}
