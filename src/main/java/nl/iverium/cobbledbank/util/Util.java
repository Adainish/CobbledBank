package nl.iverium.cobbledbank.util;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.authlib.GameProfile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nl.iverium.cobbledbank.CobbledBank;

import java.util.*;

public class Util
{
    /**
     * Checks whether a player has a specific permission with luckperms, if luckperms is not loaded, return false
     * @param permission the permission to check
     * @return true if the player has the permission, false otherwise
     */
    public static boolean hasPermission(UUID uuid, String permission) {
        boolean hasPermission = false;
        try {
            Optional<ServerPlayer> optionalServerPlayer = getOptionalServerPlayer(uuid);
            if (optionalServerPlayer.isPresent()) {
                LuckPerms api = LuckPermsProvider.get();
                var lpPlayer = optionalServerPlayer.get();
                PlayerAdapter<ServerPlayer> adapter = api.getPlayerAdapter(ServerPlayer.class);
                User user = adapter.getUser(lpPlayer);
                hasPermission = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            }
        } catch (NoClassDefFoundError ignored)
        {
        }
        return hasPermission;
    }

    /**
     * Get the player's head as an itemstack
     * @param username the username of the player
     * @return the player's head as an itemstack
     */
    public static ItemStack getPlayerHead(String username) {
        CompoundTag nbt = new CompoundTag();
        ItemStack skullStack = new ItemStack(Items.SKELETON_SKULL);
        if (username != null) {
            Optional<GameProfile> gameprofile = CobbledBank.getServer().getProfileCache().get(username);

            if (gameprofile.isPresent()) {
                nbt.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameprofile.get()));
                skullStack = new ItemStack(Items.PLAYER_HEAD);
                skullStack.setTag(nbt);
            }
        }
        return skullStack;
    }

    /**
     * Get the server player object from the player's UUID
     *
     * @return the optional player object
     */
    public static Optional<ServerPlayer> getOptionalServerPlayer(UUID uuid) {
        return Optional.ofNullable(CobbledBank.getServer().getPlayerList().getPlayer(uuid));
    }

    public static ChestTemplate.Builder returnBasicTemplateBuilder() {
        ChestTemplate.Builder builder = ChestTemplate.builder(5);
        builder.fill(filler());

        PlaceholderButton placeHolderButton = new PlaceholderButton();
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(CobbledBank.instance.languageConfig.formattedString("Previous Page"))
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(CobbledBank.instance.languageConfig.formattedString("Next Page"))
                .linkType(LinkType.Next)
                .build();

        builder.set(0, 3, previous)
                .set(0, 5, next)
                .rectangle(1, 1, 3, 7, placeHolderButton);
        return builder;
    }

    public static ItemStack returnBackItem() {
        return new ItemStack(Items.ARROW);
    }

    public static GooeyButton filler() {
        return GooeyButton.builder()
                .display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE))
                .build();
    }

    public static ItemStack pokemonToItemStack(Pokemon pokemon)
    {
        return PokemonItem.from(pokemon, 1);
    }

    public static String prettyNature(Nature nature)
    {
        return switch (nature.getName().toString().replace("cobblemon:", "")) {
            case "adamant" -> "Adamant";
            case "bashful" -> "Bashful";
            case "bold" -> "Bold";
            case "brave" -> "Brave";
            case "calm" -> "Calm";
            case "careful" -> "Careful";
            case "docile" -> "Docile";
            case "gentle" -> "Gentle";
            case "hardy" -> "Hardy";
            case "hasty" -> "Hasty";
            case "impish" -> "Impish";
            case "jolly" -> "Jolly";
            case "lax" -> "Lax";
            case "lonely" -> "Lonely";
            case "mild" -> "Mild";
            case "modest" -> "Modest";
            case "naive" -> "Naive";
            case "naughty" -> "Naughty";
            case "quiet" -> "Quiet";
            case "quirky" -> "Quirky";
            case "rash" -> "Rash";
            case "relaxed" -> "Relaxed";
            case "sassy" -> "Sassy";
            case "serious" -> "Serious";
            case "timid" -> "Timid";
            default -> nature.getName().toString().replace("cobblemon:", "");
        };
    }
    public static String prettyStatDisplay(Stat stat)
    {
        return switch (stat.getShowdownId()) {
            case "atk" -> "Attack";
            case "def" -> "Defense";
            case "spa" -> "Special Attack";
            case "spd" -> "Special Defense";
            case "spe" -> "Speed";
            case "hp" -> "HP";
            default -> stat.getShowdownId();
        };
    }

    public static String prettyAbility(Ability ability)
    {
        //uppercase first letter
        return ability.getName().substring(0, 1).toUpperCase() + ability.getName().substring(1);
    }
    public static String prettyPokemonName(Pokemon pokemon)
    {
        String name = pokemon.getSpecies().resourceIdentifier.toString().replace("cobblemon:", "");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    public static List<String> loreFromPokemon(Pokemon pokemon)
    {
        List<String> lore = new ArrayList<>();
        lore.add("&bLevel: " + pokemon.getLevel());
        lore.add("&eExperience: " + pokemon.getExperience());
        lore.add("&fNature: " + prettyNature(pokemon.getNature()));
        lore.add("&dAbility: " + prettyAbility(pokemon.getAbility()));
        lore.add(pokemon.heldItem().isEmpty() ? "&7Held Item: &fNone" : "&7Held Item: &b" + pokemon.heldItem().getHoverName().getString());
        //add additional message that a held item will or will not be retained when stored in the bank based on the db setting
        lore.add(CobbledBank.instance.database.shouldKeepHeldItems() ? "&aHeld Item will be retained when stored in the bank" : "&4Held Item will not be retained when stored in the bank");
        lore.add("&6IVs: ");
        Arrays.stream(Stats.values()).filter(stat -> stat != Stats.ACCURACY && stat != Stats.EVASION).map(stat -> "&7" + prettyStatDisplay(stat) + ": &f" + pokemon.getIvs().getOrDefault(stat)).forEachOrdered(lore::add);
        lore.add("&aEVs: ");
        Arrays.stream(Stats.values()).filter(stat -> stat != Stats.ACCURACY && stat != Stats.EVASION).map(stat -> "&7" + prettyStatDisplay(stat) + ": &f" + pokemon.getEvs().getOrDefault(stat)).forEachOrdered(lore::add);
        return lore;
    }

    public static GooeyButton pokemonToButton(Pokemon pokemon)
    {
        return GooeyButton.builder()
                .title(pokemon.getDisplayName().plainCopy().toString())
                .display(pokemonToItemStack(pokemon))
                .lore(CobbledBank.instance.languageConfig.formattedArrayList(loreFromPokemon(pokemon)))
                .build();
    }

}
