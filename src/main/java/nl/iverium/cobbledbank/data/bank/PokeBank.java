package nl.iverium.cobbledbank.data.bank;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nl.iverium.cobbledbank.CobbledBank;
import nl.iverium.cobbledbank.data.adapters.GSON;
import nl.iverium.cobbledbank.data.bank.pokemon.BankPokemon;
import nl.iverium.cobbledbank.util.Logger;
import nl.iverium.cobbledbank.util.Util;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PokeBank
{
    public UUID uuid;
    public List<BankPokemon> pokemonList = new ArrayList<>();

    public PokeBank(UUID playerUUID)
    {
        this.uuid = playerUUID;
    }

    public void addPokemon(PlayerPartyStore pps, PCStore pcs, Pokemon pokemon)
    {
        if (pps != null) addPokemonFromParty(pps, pokemon);
        else if (pcs != null) addPokemonFromPC(pcs, pokemon);
    }

    public void addPokemonFromParty(PlayerPartyStore pps, Pokemon pokemon)
    {
        //check if bank is locked
        if (CobbledBank.instance.database.isGloballyLocked())
        {
            //if it is locked, send error message
            CobbledBank.instance.languageConfig.sendPrefixInfoFromKey(pps.getPlayerUUID(), "pokebank.addPokemon.locked");
            return;
        }
        //check if the pokemon bank is full
        if (pokemonList.size() >= CobbledBank.instance.database.getMaxBankSize())
        {
            //if it is full, send error message
            CobbledBank.instance.languageConfig.sendPrefixErrorFromKey(pps.getPlayerUUID(), "pokebank.addPokemon.full");
            return;
        }

        BankPokemon bankPokemon = new BankPokemon();
        bankPokemon.toStorablePokemon(pokemon);
        pokemonList.add(bankPokemon);
        pps.remove(pokemon);
        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(pps.getPlayerUUID(), "pokebank.addPokemon.success");
        //save the bank
        CobbledBank.instance.database.addPokeBank(this, true);
        ServerPlayer player = CobbledBank.getServer().getPlayerList().getPlayer(pps.getPlayerUUID());
        if (player != null)
            UIManager.openUIForcefully(player, getBankPage(pps.getPlayerUUID(), false));
        else Logger.log("Player not found. This is bad and shouldn't be happening.");
    }

    public void addPokemonFromPC(PCStore pcs, Pokemon pokemon)
    {
        //check if bank is locked
        if (CobbledBank.instance.database.isGloballyLocked())
        {
            //if it is locked, send error message
            CobbledBank.instance.languageConfig.sendPrefixInfoFromKey(pcs.getUuid(), "pokebank.addPokemon.locked");
            return;
        }
        //check if the pokemon bank is full
        if (pokemonList.size() >= CobbledBank.instance.database.getMaxBankSize())
        {
            //if it is full, send error message
            CobbledBank.instance.languageConfig.sendPrefixErrorFromKey(pcs.getUuid(), "pokebank.addPokemon.full");
            return;
        }

        BankPokemon bankPokemon = new BankPokemon();
        bankPokemon.toStorablePokemon(pokemon);
        pokemonList.add(bankPokemon);
        pcs.remove(pokemon);
        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(pcs.getUuid(), "pokebank.addpokemon.success");
        //save the bank
        CobbledBank.instance.database.addPokeBank(this, true);
        ServerPlayer player = CobbledBank.getServer().getPlayerList().getPlayer(pcs.getUuid());
        if (player != null)
            UIManager.openUIForcefully(player, getBankPage(pcs.getUuid(), false));
        else Logger.log("Player not found. This is bad and shouldn't be happening.");
    }

    public void removePokemon(PlayerPartyStore pps, BankPokemon bankPokemon)
    {
        //check if bank is locked
        if (CobbledBank.instance.database.isGloballyLocked())
        {
            //if it is locked, send error message
            CobbledBank.instance.languageConfig.sendPrefixInfoFromKey(pps.getPlayerUUID(), "pokebank.removePokemon.locked");
            return;
        }
        //check if the pokemon bank is empty
        if (pokemonList.isEmpty())
        {
            //if it is empty, send error message
            CobbledBank.instance.languageConfig.sendPrefixErrorFromKey(pps.getPlayerUUID(), "pokebank.removePokemon.empty");
            return;
        }
        pps.add(bankPokemon.fromStorablePokemon());
        pokemonList.remove(bankPokemon);
        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(pps.getPlayerUUID(), "pokebank.removePokemon.success");
        //save the bank
        CobbledBank.instance.database.addPokeBank(this, true);
        ServerPlayer player = CobbledBank.getServer().getPlayerList().getPlayer(pps.getPlayerUUID());
        if (player != null)
            UIManager.openUIForcefully(player, getBankPage(pps.getPlayerUUID(), false));
        else Logger.log("Player not found. This is bad and shouldn't be happening.");
    }

    public void clearBank(PlayerPartyStore pps)
    {
        //check if bank is locked
        if (CobbledBank.instance.database.isGloballyLocked())
        {
            //if it is locked, send error message
            CobbledBank.instance.languageConfig.sendPrefixInfoFromKey(pps.getPlayerUUID(), "pokebank.clearBank.locked");
            return;
        }
        //check if the pokemon bank is empty
        if (pokemonList.isEmpty())
        {
            //if it is empty, send error message
            CobbledBank.instance.languageConfig.sendPrefixErrorFromKey(pps.getPlayerUUID(), "pokebank.clearBank.empty");
            return;
        }

        pokemonList.forEach(bankPokemon -> pps.add(bankPokemon.fromStorablePokemon()));
        pokemonList.clear();
        CobbledBank.instance.languageConfig.sendPrefixSuccessFromKey(pps.getPlayerUUID(), "pokebank.clearBank.success");
        //save the bank
        CobbledBank.instance.database.addPokeBank(this, true);
        ServerPlayer player = CobbledBank.getServer().getPlayerList().getPlayer(pps.getPlayerUUID());
        if (player != null)
            UIManager.openUIForcefully(player, getBankPage(pps.getPlayerUUID(), false));
        else Logger.log("Player not found. This is bad and shouldn't be happening.");
    }

    public void movePokemon(PlayerPartyStore pps, PCStore pcs, BankPokemon bankPokemon)
    {
        if (pps != null) addPokemonFromParty(pps, bankPokemon.fromStorablePokemon());
        else if (pcs != null) addPokemonFromPC(pcs, bankPokemon.fromStorablePokemon());
        pokemonList.remove(bankPokemon);
        CobbledBank.instance.database.addPokeBank(this, true);
    }


    public Document toDocument() {
        Gson gson  = GSON.PRETTY_MAIN_GSON;
        String json = gson.toJson(this);
        return Document.parse(json);
    }

    public static PokeBank fromDocument(Document document)
    {
        Gson gson = GSON.PRETTY_MAIN_GSON;
        return gson.fromJson(document.toJson(), PokeBank.class);
    }

    public Button partyButtonFromPokemon(PlayerPartyStore pps, Pokemon pokemon)
    {
        return GooeyButton.builder()
                .title(CobbledBank.instance.languageConfig.formattedString(Util.prettyPokemonName(pokemon)))
                .display(Util.pokemonToItemStack(pokemon))
                .lore(CobbledBank.instance.languageConfig.formattedArrayList(Util.loreFromPokemon(pokemon)))
                .onClick(buttonAction -> addPokemon(pps, null, pokemon))
                .build();
    }

    public Button bankButtonFromPokemon(PlayerPartyStore pps, BankPokemon bankPokemon)
    {
        Pokemon pokemon = bankPokemon.fromStorablePokemon();
        return GooeyButton.builder()
                .title(CobbledBank.instance.languageConfig.formattedString(Util.prettyPokemonName(pokemon)))
                .display(Util.pokemonToItemStack(pokemon))
                .lore(CobbledBank.instance.languageConfig.formattedArrayList(Util.loreFromPokemon(pokemon)))
                .onClick(buttonAction -> removePokemon(pps, bankPokemon))
                .build();
    }

    public Button pcButtonFromPokemon(PCStore pcs, Pokemon pokemon)
    {
        return GooeyButton.builder()
                .title(CobbledBank.instance.languageConfig.formattedString(Util.prettyPokemonName(pokemon)))
                .display(Util.pokemonToItemStack(pokemon))
                .lore(CobbledBank.instance.languageConfig.formattedArrayList(Util.loreFromPokemon(pokemon)))
                .onClick(buttonAction -> addPokemon(null, pcs, pokemon))
                .build();
    }

    public List<Button> bankButtons(PlayerPartyStore pps)
    {
        List<Button> buttons = new ArrayList<>();
        for (BankPokemon bankPokemon : pokemonList) buttons.add(bankButtonFromPokemon(pps, bankPokemon));
        return buttons;
    }

    public List<Button> partyButtons(PlayerPartyStore pps)
    {
        List<Button> buttons = new ArrayList<>();
        for (Pokemon pokemon : pps) buttons.add(partyButtonFromPokemon(pps, pokemon));
        return buttons;
    }

    public List<Button> pcButtons(PCStore pcs)
    {
        List<Button> buttons = new ArrayList<>();
        for (Pokemon pokemon : pcs) buttons.add(pcButtonFromPokemon(pcs, pokemon));
        return buttons;
    }

    public LinkedPage getPartyPage(UUID uuid) {
        PlayerPartyStore pps;
        try {
            pps = Cobblemon.INSTANCE.getStorage().getParty(uuid);
        } catch (NoPokemonStoreException e) {
            throw new RuntimeException(e);
        }
        ChestTemplate.Builder template = Util.returnBasicTemplateBuilder();
        //add back button and party button
        GooeyButton.Builder builder = GooeyButton.builder();
        builder.display(new ItemStack(Items.ARROW));
        builder.title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.back"));
        builder.onClick(buttonAction -> UIManager.openUIForcefully(buttonAction.getPlayer(), getBankPage(uuid, false)));
        GooeyButton backButton = builder
                .build();

        buttonApplication(uuid, template, backButton, true);
        return PaginationHelper.createPagesFromPlaceholders(template.build(), partyButtons(pps),
                LinkedPage.builder().title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.party")).template(template.build()));
    }

    private void buttonApplication(UUID uuid, ChestTemplate.Builder template, GooeyButton backButton, boolean isParty) {
        template.set(4, 1, backButton);

        GooeyButton secondaryButton;
        if (!isParty) secondaryButton = GooeyButton.builder()
                .display(CobblemonItems.POKE_BALL.getPokeBall().stack(1))
                .title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.party"))
                .onClick(buttonAction -> UIManager.openUIForcefully(buttonAction.getPlayer(), getPartyPage(uuid)))
                .build();
        else secondaryButton = GooeyButton.builder()
                .display(CobblemonItems.PREMIER_BALL.getPokeBall().stack(1))
                .title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.pc"))
                .onClick(buttonAction -> UIManager.openUIForcefully(buttonAction.getPlayer(), getPcPage(uuid)))
                .build();
        template.set(4, 7, secondaryButton);
    }

    public LinkedPage getPcPage(UUID uuid) {
        PCStore pcs;
        try {
            pcs = Cobblemon.INSTANCE.getStorage().getPC(uuid);
        } catch (NoPokemonStoreException e) {
            throw new RuntimeException(e);
        }
        ChestTemplate.Builder template = Util.returnBasicTemplateBuilder();

        //add back button and party button
        GooeyButton backButton = GooeyButton.builder()
                .display(new ItemStack(Items.ARROW))
                .title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.back"))
                .onClick(buttonAction -> UIManager.openUIForcefully(buttonAction.getPlayer(), getBankPage(uuid, false)))
                .build();

        buttonApplication(uuid, template, backButton, false);

        return PaginationHelper.createPagesFromPlaceholders(template.build(), pcButtons(pcs),
                LinkedPage.builder().title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.pc")).template(template.build()));
    }

    public LinkedPage getBankPage(UUID uuid, boolean isAdmin) {
        ChestTemplate.Builder template = Util.returnBasicTemplateBuilder();

        PlayerPartyStore pps = null;
        try {
            if (isAdmin) {

                pps = Cobblemon.INSTANCE.getStorage().getParty(this.uuid);
            } else pps = Cobblemon.INSTANCE.getStorage().getParty(uuid);
        } catch (NoPokemonStoreException e) {
            Logger.log(e);
            if (isAdmin)
            {
                //send message to admin that there is no data to work with
                // return empty gui then close it
                template.fill(GooeyButton.builder().display(new ItemStack(Items.BARRIER)).build());

                return LinkedPage.builder().template(template.build()).title("Something went wrong. Check console").build();
            }
        }

        if (!isAdmin)
        {
            //add button for opening party view and pc view
            GooeyButton partyButton = GooeyButton.builder()
                    .display(CobblemonItems.POKE_BALL.getPokeBall().stack(1))
                    .title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.party"))
                    .onClick(buttonAction -> UIManager.openUIForcefully(buttonAction.getPlayer(), getPartyPage(uuid)))
                    .build();

            GooeyButton pcButton = GooeyButton.builder()
                    .display(CobblemonItems.PREMIER_BALL.getPokeBall().stack(1))
                    .title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank.pc"))
                    .onClick(buttonAction -> UIManager.openUIForcefully(buttonAction.getPlayer(), getPcPage(uuid)))
                    .build();

            template.set(4, 1, partyButton);
            template.set(4, 7, pcButton);
        } else {
            //TODO: Ask for client feedback, currently clicking a pokemon as an admin removes it from the bank to the party.
        }

        return PaginationHelper.createPagesFromPlaceholders(template.build(), bankButtons(pps),
                LinkedPage.builder().title(CobbledBank.instance.languageConfig.getFormattedStringFromKey("gui.title.pokebank")).template(template.build()));
    }

}
