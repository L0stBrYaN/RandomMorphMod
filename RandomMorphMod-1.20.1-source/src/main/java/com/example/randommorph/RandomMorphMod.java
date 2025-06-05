
package com.example.randommorph;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;

public class RandomMorphMod implements ModInitializer {
    private static final Set<String> usedMobs = new HashSet<>();
    private static final Random random = new Random();
    private static boolean isRunning = false;
    private static int tickCounter = 0;
    private static final int TICKS_PER_MORPH = 20 * 60;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("startmorph").executes(context -> {
                isRunning = true;
                tickCounter = 0;
                context.getSource().sendFeedback(() -> Text.literal("§aMob-Morph gestartet."), false);
                return 1;
            }));
            dispatcher.register(literal("stopmorph").executes(context -> {
                isRunning = false;
                context.getSource().sendFeedback(() -> Text.literal("§cMob-Morph gestoppt."), false);
                return 1;
            }));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!isRunning) return;
            tickCounter++;
            if (tickCounter >= TICKS_PER_MORPH) {
                tickCounter = 0;
                List<String> allMobs = Registry.ENTITY_TYPE.getIds().stream()
                        .map(Identifier::getPath)
                        .filter(id -> !usedMobs.contains(id) && !id.equals("player"))
                        .toList();

                if (allMobs.isEmpty()) return;

                String selected = allMobs.get(random.nextInt(allMobs.size()));
                usedMobs.add(selected);

                server.getPlayerManager().getPlayerList().forEach(player -> {
                    server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/identity set " + selected + " " + player.getName().getString());
                    player.sendMessage(Text.literal("§bGemorpht in: §e" + selected), false);
                });
            }
        });
    }
}
