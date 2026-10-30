package com.example.autotrade;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class AutoTradeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autotrade")
                .executes(context -> {
                    startTrading(1);
                    return 1;
                })
                .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 1000))
                    .executes(context -> {
                        int count = IntegerArgumentType.getInteger(context, "count");
                        startTrading(count);
                        return 1;
                    })
                )
            );
        });
    }

    private void startTrading(int times) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        if (client.player.currentScreenHandler instanceof MerchantScreenHandler handler) {
            client.player.sendMessage(Text.literal("§a[AutoTrade] Запуск автоматической торговли (" + times + " раз)..."), false);

            new Thread(() -> {
                try {
                    for (int i = 0; i < times; i++) {
                        if (!(client.player.currentScreenHandler instanceof MerchantScreenHandler)) {
                            break;
                        }

                        handler.setRecipeIndex(0);
                        
                        client.execute(() -> {
                            client.interactionManager.clickSlot(
                                handler.syncId, 
                                2, 
                                0, 
                                SlotActionType.QUICK_MOVE, 
                                client.player
                            );
                        });

                        Thread.sleep(100);
                    }
                    
                    client.player.sendMessage(Text.literal("§a[AutoTrade] Торговля завершена!"), false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } else {
            client.player.sendMessage(Text.literal("§c[AutoTrade] Ошибка: Откройте окно торговли с жителем!"), false);
        }
    }
}
