package nl.iverium.cobbledbank.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import nl.iverium.cobbledbank.CobbledBank;
import nl.iverium.cobbledbank.data.adapters.GSON;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.iverium.cobbledbank.util.Util.getOptionalServerPlayer;

public class LanguageConfig
{
    public String prefix = "&8[&6CobbledBank&8] &7";
    public HashMap<String, String> messages = new HashMap<>();
    public LanguageConfig()
    {
        this.initialSetup();
    }

    public void initialSetup() {
        if (messages.isEmpty()) {
            messages.put("pokebank.addPokemon.locked", "The bank is currently locked. Please try again later.");
            messages.put("pokebank.addPokemon.full", "The bank is full. Please remove some Pokemon before adding more.");
            messages.put("pokebank.removePokemon.locked", "The bank is currently locked. Please try again later.");
            messages.put("pokebank.removePokemon.empty", "The bank is empty. There are no Pokemon to remove.");
            messages.put("pokebank.clearBank.locked", "The bank is currently locked. Please try again later.");
            messages.put("pokebank.clearBank.empty", "The bank is empty. There are no Pokemon to clear.");
            messages.put("pokebank.clearBank.success", "The bank has been cleared.");
            messages.put("pokebank.addpokemon.success", "The Pokemon has been added to the bank.");
            messages.put("pokebank.removePokemon.success", "The Pokemon has been removed from the bank.");
            messages.put("pokebank.removePokemon.notfound", "The Pokemon was not found in the bank.");

            messages.put("pokebank.unlock.success", "The bank has been unlocked.");
            messages.put("pokebank.unlock.failure", "The bank is already unlocked.");
            messages.put("pokebank.lock.success", "The bank has been locked.");
            messages.put("pokebank.lock.failure", "The bank is already locked.");

            messages.put("pokebank.reload.success", "PokeBank has been reloaded.");

            messages.put("pokebank.setMaxPokemon.success", "The maximum number of Pokemon in the bank has been set to %s.");
            messages.put("pokebank.setMaxPokemon.overwrite.success", "The maximum number of Pokemon in the bank has been set to %s.");

            messages.put("pokebank.keepHeldItems.disable.success", "Held items will no longer be kept when removing Pokemon from the bank.");
            messages.put("pokebank.keepHeldItems.enable.success", "Held items will now be kept when removing Pokemon from the bank.");


            messages.put("pokebank.keepHeldItems.disable.failure", "This setting is already disabled.");
            messages.put("pokebank.keepHeldItems.enable.failure", "This setting is already enabled.");

            messages.put("gui.title.pokebank", "PokeBank");
            messages.put("gui.title.pokebank.party", "Party Pokemon");
            messages.put("gui.title.pokebank.pc", "PC Pokemon");
            messages.put("gui.title.pokebank.back", "Back");
            messages.put("gui.title.pokebank.addPokemon", "Add Pokemon");
            messages.put("gui.title.pokebank.removePokemon", "Remove Pokemon");
            messages.put("gui.title.pokebank.clearBank", "Clear Bank");
            messages.put("gui.title.pokebank.clearBank.confirm", "Are you sure?");
            messages.put("gui.title.pokebank.addPokemon.confirm", "Are you sure?");
            messages.put("gui.title.pokebank.removePokemon.confirm", "Are you sure?");
        }
    }

    /**
     * Get a string from a key
     * @param key the key to get the string from
     * @return the string from the key if it exists, otherwise an empty string
     */
    public String getStringFromKey(String key)
    {
        if (messages.containsKey(key))
            return messages.get(key);
        return "";
    }

    /**
     * Get a formatted string from a key
     * @param key the key to get the string from
     * @return the formatted string from the key if it exists, otherwise an empty string
     */
    public String getFormattedStringFromKey(String key)
    {
        return formattedString(getStringFromKey(key));
    }

    /**
     * Add a message to the config
     *
     * @param key the key of the message
     * @param message the message
     */
    public void addMessage(String key, String message)
    {
        messages.put(key, message);
    }

    /**
     * Remove a message from the config
     *
     * @param key the key of the message
     */
    public void removeMessage(String key)
    {
        messages.remove(key);
    }

    /**
     * Update a message in the config
     *
     * @param key     the key of the message
     * @param message the new message
     */
    public void updateMessage(String key, String message)
    {
        messages.put(key, message);
    }

    /**
     * Clear all messages from the config
     */
    public void clearMessages()
    {
        messages.clear();
    }

    /**
     * Get the prefix
     *
     * @return the prefix
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Get all messages in the config
     * @return the messages
     */
    public HashMap<String, String> getMessages()
    {
        return messages;
    }


    /**
     * Set all messages in the config
     * @param messages the messages to set
     */
    public void setMessages(HashMap<String, String> messages)
    {
        this.messages = messages;
    }


    /**
     * Write the config to a file
     */
    public static void writeConfig()
    {
        File dir = CobbledBank.getConfigDir();
        dir.mkdirs();
        Gson gson  = GSON.PRETTY_MAIN_GSON;
        LanguageConfig config = new LanguageConfig();
        writeFile(config, dir, gson, false);
    }

    /**
     * Get the config from a file
     *
     * @return the config
     */
    public static LanguageConfig getConfig()
    {
        File dir = CobbledBank.getConfigDir();
        dir.mkdirs();
        Gson gson  = GSON.PRETTY_MAIN_GSON;
        File file = new File(dir, "language.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            CobbledBank.getLog().error("Something went wrong attempting to read the Config");
            return null;
        }

        return gson.fromJson(reader, LanguageConfig.class);
    }

    /**
     * Save the config to a file
     *
     * @param config the config to save
     */
    public static void saveConfig(LanguageConfig config)
    {
        File dir = CobbledBank.getConfigDir();
        dir.mkdirs();
        Gson gson  = GSON.PRETTY_MAIN_GSON;
        writeFile(config, dir, gson, true);
    }

    /**
     * Write the config to a file
     *
     * @param config the config to write
     * @param dir the directory to write the file to
     * @param gson the gson object to use
     * @param overwrite whether to overwrite the file
     */
    private static void writeFile(LanguageConfig config, File dir, Gson gson, boolean overwrite) {
        try {
            File file = new File(dir, "language.json");
            if (file.exists() && !overwrite)
                return;
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            String json = gson.toJson(config);
            writer.write(json);
            writer.close();
        } catch (IOException e)
        {
            CobbledBank.getLog().warn(e);
        }
    }


    /**
     * Send a message to the player
     *
     * @param message the message to send
     */
    public void sendMessage(UUID uuid, String message) {
        getOptionalServerPlayer(uuid).ifPresent(player -> player.sendSystemMessage(Component.literal(formattedString(message)).withStyle(ChatFormatting.GREEN)));
    }

    /**
     * Send a message to the player with a color
     *
     * @param message the message to send
     * @param color   the color of the message
     */
    public void sendMessage(UUID uuid, String message, ChatFormatting color) {
        getOptionalServerPlayer(uuid).ifPresent(player -> player.sendSystemMessage(Component.literal(formattedString(message)).withStyle(color)));
    }

    /**
     * Send a message to the player from a key
     * @param uuid the player's UUID
     * @param key the key to get the message from
     */
    public void sendMessageFromKey(UUID uuid, String key) {
        sendMessage(uuid, getStringFromKey(key));
    }

    /**
     * Send an error message to the player from a key
     * @param uuid the player's UUID
     * @param key the key to get the message from
     */
    public void sendErrorFromKey(UUID uuid, String key) {
        sendMessage(uuid, getStringFromKey(key), ChatFormatting.RED);
    }

    /**
     * Send a warning message to the player from a key
     * @param uuid the player's UUID
     * @param key the key to get the message from
     */
    public void sendWarningFromKey(UUID uuid, String key) {
        sendMessage(uuid, getStringFromKey(key), ChatFormatting.YELLOW);
    }

    /**
     * Send a success message to the player from a key
     * @param uuid the player's UUID
     * @param key the key to get the message from
     */
    public void sendSuccessFromKey(UUID uuid, String key) {
        sendMessage(uuid, getStringFromKey(key), ChatFormatting.GREEN);
    }

    /**
     * Send an info message to the player from a key
     * @param uuid the player's UUID
     * @param key the key to get the message from
     */
    public void sendInfoFromKey(UUID uuid, String key) {
        sendMessage(uuid, getStringFromKey(key), ChatFormatting.AQUA);
    }

    /**
     * Send a message to all players
     * @param message the message to send
     */
    public void sendPrefixMessage(UUID uuid, String message) {
        sendMessage(uuid, prefix + message);
    }

    /**
     * Send a message to all players with a color
     * @param message the message to send
     * @param color the color of the message
     */
    public void sendPrefixMessage(UUID uuid, String message, ChatFormatting color) {
        sendMessage(uuid, prefix + message, color);
    }

    /**
     * Send a message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixMessageFromKey(UUID uuid, String key) {
        sendPrefixMessage(uuid, getStringFromKey(key));
    }

    /**
     * Send an error message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixErrorFromKey(UUID uuid, String key) {
        sendPrefixMessage(uuid, getStringFromKey(key), ChatFormatting.RED);
    }

    /**
     * Send a warning message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixWarningFromKey(UUID uuid, String key) {
        sendPrefixMessage(uuid, getStringFromKey(key), ChatFormatting.YELLOW);
    }

    /**
     * Send a success message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixSuccessFromKey(UUID uuid, String key) {
        sendPrefixMessage(uuid, getStringFromKey(key), ChatFormatting.GREEN);
    }

    /**
     * Send an info message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixInfoFromKey(UUID uuid, String key) {
        sendPrefixMessage(uuid, getStringFromKey(key), ChatFormatting.AQUA);
    }

    /**
     * Send a message to all players
     * @param message the message to send
     */
    public void sendPrefixMessageToAll(String message) {
        CobbledBank.getServer().getPlayerList().getPlayers().forEach(player -> sendPrefixMessage(player.getUUID(), message));
    }

    /**
     * Send a message to all players with a color
     * @param message the message to send
     * @param color the color of the message
     */
    public void sendPrefixMessageToAll(String message, ChatFormatting color) {
        CobbledBank.getServer().getPlayerList().getPlayers().forEach(player -> sendPrefixMessage(player.getUUID(), message, color));
    }

    /**
     * Send a message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixMessageFromKeyToAll(String key) {
        CobbledBank.getServer().getPlayerList().getPlayers().forEach(player -> sendPrefixMessageFromKey(player.getUUID(), key));
    }

    /**
     * Send an error message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixErrorFromKeyToAll(String key) {
        CobbledBank.getServer().getPlayerList().getPlayers().forEach(player -> sendPrefixErrorFromKey(player.getUUID(), key));
    }

    /**
     * Send a warning message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixWarningFromKeyToAll(String key) {
        CobbledBank.getServer().getPlayerList().getPlayers().forEach(player -> sendPrefixWarningFromKey(player.getUUID(), key));
    }

    /**
     * Send a success message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixSuccessFromKeyToAll(String key) {
        CobbledBank.getServer().getPlayerList().getPlayers().forEach(player -> sendPrefixSuccessFromKey(player.getUUID(), key));
    }

    /**
     * Send an info message to all players from a key
     * @param key the key to get the message from
     */
    public void sendPrefixInfoFromKeyToAll(String key) {
        CobbledBank.getServer().getPlayerList().getPlayers().forEach(player -> sendPrefixInfoFromKey(player.getUUID(), key));
    }

    /**
     * Format a string with Minecraft formatting codes
     * @param s the string to format
     * @return the formatted string
     */
    public String formattedString(String s) {
        return s.replaceAll("&", "§");
    }

    /**
     * Format a list of strings with Minecraft formatting codes
     * @param list the list to format
     * @return the formatted list
     */
    public List<String> formattedArrayList(List<String> list) {

        return list.stream().map(this::formattedString).collect(Collectors.toList());
    }
}
