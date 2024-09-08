package nl.iverium.cobbledbank.commands;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import nl.iverium.cobbledbank.CobbledBank;
import nl.iverium.cobbledbank.data.bank.PokeBank;
import nl.iverium.cobbledbank.util.Logger;
import nl.iverium.cobbledbank.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PokeBankCommand {
    public static int help(CommandSourceStack source) {
        List<Component> messages = new ArrayList<>();
        messages.add(Component.literal("PokeBank Commands:").withStyle(s -> s.withBold(true)).withStyle(ChatFormatting.AQUA));
        if (hasPermission(source, "pokebank.base.player"))
            messages.add(Component.literal("/pokebank - Opens your bank.").withStyle(ChatFormatting.GOLD));

        if (hasPermission(source, "pokebank.base.help")) {
            messages.add(Component.literal("/pokebank help - Displays this message.").withStyle(ChatFormatting.GOLD));
        }
        if (hasPermission(source, "pokebank.base.pc"))
            messages.add(Component.literal("/pokebank pc - Opens your bank directly into your pc, so you can store directly from there.").withStyle(ChatFormatting.GOLD));
        if (hasPermission(source, "pokebank.base.party"))
            messages.add(Component.literal("/pokebank party - Opens your bank directly into your pc, so you can store directly from there.").withStyle(ChatFormatting.GOLD));
        if (hasPermission(source, "pokebank.admin")) {
            messages.add(Component.literal("/pokebank <player> - Opens the pokebank of the specified player. Allowing administrative actions where needed. Works only on online players.").withStyle(ChatFormatting.GOLD));
            messages.add(Component.literal("/pokebank lock - Locks the global bank, preventing all players from accessing it.").withStyle(ChatFormatting.GOLD));
            messages.add(Component.literal("/pokebank unlock - Unlocks the global bank, allowing all players to access it.").withStyle(ChatFormatting.GOLD));
            messages.add(Component.literal("/pokebank keepHeldItems enable - Enables the ability to keep held items on pokemon when stored in the bank.").withStyle(ChatFormatting.GOLD));
            messages.add(Component.literal("/pokebank keepHeldItems disable - Disables the ability to keep held items on pokemon when stored in the bank.").withStyle(ChatFormatting.GOLD));
            messages.add(Component.literal("/pokebank setMaxPokemon <maxPokemon> - Verifies the maximum amount of pokemon a player can store in their bank.").withStyle(ChatFormatting.GOLD));
            messages.add(Component.literal("/pokebank setMaxPokemon <maxPokemon> overwrite - Sets the maximum amount of pokemon a player can store in their bank, overwriting the current value.").withStyle(ChatFormatting.GOLD));
            messages.add(Component.literal("/pokebank reload - Reloads the pokebank configuration.").withStyle(ChatFormatting.GOLD));
        }
        messages.forEach(source::sendSystemMessage);
        return 1;
    }

    public static boolean hasPermission(CommandSourceStack sourceStack, String permission) {
        if (sourceStack.isPlayer())
            try {
                return Util.hasPermission(sourceStack.getPlayerOrException().getUUID(), permission);
            } catch (CommandSyntaxException e) {
                Logger.log(e);
                return false;
            }
        else
            return true;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("pokebank")
                .requires(source -> hasPermission(source, "pokebank.base.player"))
                .executes(cc ->
                        {
                            try {
                                if (cc.getSource().isPlayer()) {
                                    ServerPlayer player = cc.getSource().getPlayer();
                                    if (player != null) {
                                        Optional<PokeBank> bank = CobbledBank.instance.database.getOrCreatePokeBank(cc.getSource().getPlayerOrException().getUUID());
                                        bank.ifPresent(pokeBank -> UIManager.openUIForcefully(player, pokeBank.getBankPage(player.getUUID(), false)));
                                    } else {
                                        Logger.log("Player is null, this should not happen.");
                                    }
                                } else {
                                    Logger.log("Command source is not a player, Please do not use this command otherwise.");
                                }
                            } catch (Exception e) {
                                Logger.log(e);
                            }
                            return 1;
                        }
                )
                .then(Commands.literal("help")
                        .requires(source -> hasPermission(source, "pokebank.base.help"))
                        .executes(cc -> help(cc.getSource()))
                )
                .then(Commands.literal("pc")
                        .requires(source -> hasPermission(source, "pokebank.base.pc"))
                        .executes(cc -> {
                            if (cc.getSource().isPlayer()) {
                                ServerPlayer player = cc.getSource().getPlayer();
                                if (player != null) {
                                    Optional<PokeBank> bank = CobbledBank.instance.database.getOrCreatePokeBank(cc.getSource().getPlayerOrException().getUUID());
                                    bank.ifPresent(pokeBank -> UIManager.openUIForcefully(player, pokeBank.getPcPage(player.getUUID())));
                                } else {
                                    Logger.log("Player is null, this should not happen.");
                                }
                            } else {
                                Logger.log("Command source is not a player, Please do not use this command otherwise.");
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("party")
                        .requires(source -> hasPermission(source, "pokebank.base.party"))
                        .executes(cc -> {
                            if (cc.getSource().isPlayer()) {
                                ServerPlayer player = cc.getSource().getPlayer();
                                if (player != null) {
                                    Optional<PokeBank> bank = CobbledBank.instance.database.getOrCreatePokeBank(cc.getSource().getPlayerOrException().getUUID());
                                    bank.ifPresent(pokeBank -> UIManager.openUIForcefully(player, pokeBank.getPartyPage(player.getUUID())));
                                } else {
                                    Logger.log("Player is null, this should not happen.");
                                }
                            } else {
                                Logger.log("Command source is not a player, Please do not use this command otherwise.");
                            }
                            return 1;
                        })
                )
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> hasPermission(source, "pokebank.admin"))
                        .executes(cc -> {
                            //opens the pokebank of an online player for an admin, allowing them to view and edit the bank
                            if (cc.getSource().isPlayer()) {
                                ServerPlayer player = cc.getSource().getPlayer();
                                if (player != null) {
                                    Optional<PokeBank> bank = CobbledBank.instance.database.getOrCreatePokeBank(cc.getSource().getPlayerOrException().getUUID());
                                    bank.ifPresent(pokeBank -> UIManager.openUIForcefully(player, pokeBank.getBankPage(player.getUUID(), true)));
                                } else {
                                    Logger.log("Player is null, this should not happen.");
                                }
                            } else {
                                Logger.log("Command source is not a player, Please do not use this command otherwise.");
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("lock")
                        .requires(source -> hasPermission(source, "pokebank.admin"))
                        .executes(cc -> {
                            if (!CobbledBank.instance.database.isGloballyLocked()) {
                                CobbledBank.instance.database.lockGlobalBank();
                                CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.lock.success");
                            } else {
                                CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.lock.failure");
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("unlock")
                        .requires(source -> hasPermission(source, "pokebank.admin"))
                        .executes(cc -> {
                            if (CobbledBank.instance.database.isGloballyLocked()) {
                                CobbledBank.instance.database.unlockGlobalBank();
                                CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.unlock.success");
                            } else {
                                CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.unlock.failure");
                            }
                            return 1;
                        })
                )
                //command for enabling held items
                .then(Commands.literal("keepHeldItems")
                        .requires(source -> hasPermission(source, "pokebank.admin"))
                        .then(Commands.literal("enable")
                                .requires(source -> hasPermission(source, "pokebank.admin"))
                                .executes(cc -> {
                                    if (!CobbledBank.instance.database.shouldKeepHeldItems()) {
                                        CobbledBank.instance.database.setKeepHeldItems(true);
                                        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.keepHeldItems.enable.success");
                                    } else {
                                        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.keepHeldItems.enable.failure");
                                    }
                                    return 1;
                                })
                        )
                        .then(Commands.literal("disable")
                                .requires(source -> hasPermission(source, "pokebank.admin"))
                                .executes(cc -> {
                                    if (CobbledBank.instance.database.shouldKeepHeldItems()) {
                                        CobbledBank.instance.database.setKeepHeldItems(false);
                                        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.keepHeldItems.disable.success");
                                    } else {
                                        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.keepHeldItems.disable.failure");
                                    }
                                    return 1;
                                })
                        )
                )
                //command for setting max pokemon
                .then(Commands.literal("setMaxPokemon")
                        .requires(source -> hasPermission(source, "pokebank.admin"))
                        .then(Commands.argument("maxPokemon", IntegerArgumentType.integer(1))
                                .requires(source -> hasPermission(source, "pokebank.admin"))
                                .executes(cc -> {
                                    int maxPokemon = IntegerArgumentType.getInteger(cc, "maxPokemon");
                                    CobbledBank.instance.database.setMaxBankSize(maxPokemon, false);
                                    String message = CobbledBank.instance.languageConfig.getStringFromKey("pokebank.setMaxPokemon.success").replace("%s", String.valueOf(maxPokemon));
                                    CobbledBank.instance.languageConfig.sendPrefixSuccess(cc.getSource().getPlayerOrException().getUUID(), message);
                                    return 1;
                                })
                                .then(Commands.literal("overwrite")
                                        .requires(source -> hasPermission(source, "pokebank.admin"))
                                        .executes(cc -> {
                                            int maxPokemon = IntegerArgumentType.getInteger(cc, "maxPokemon");
                                            CobbledBank.instance.database.setMaxBankSize(maxPokemon, true);
                                            String message = CobbledBank.instance.languageConfig.getStringFromKey("pokebank.setMaxPokemon.success").replace("%s", String.valueOf(maxPokemon));
                                            CobbledBank.instance.languageConfig.sendPrefixSuccess(cc.getSource().getPlayerOrException().getUUID(), message);
                                            return 1;
                                        })

                                )
                        )
                )
                .then(Commands.literal("reload")
                        .requires(source -> hasPermission(source, "pokebank.admin"))
                        .executes(cc -> {
                            CobbledBank.instance.load();
                            CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(cc.getSource().getPlayerOrException().getUUID(), "pokebank.reload.success");
                            return 1;
                        })
                )
                ;
    }
}
