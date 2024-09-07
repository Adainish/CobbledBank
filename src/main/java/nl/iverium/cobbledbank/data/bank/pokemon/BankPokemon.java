package nl.iverium.cobbledbank.data.bank.pokemon;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import net.minecraft.world.item.ItemStack;
import nl.iverium.cobbledbank.CobbledBank;
import nl.iverium.cobbledbank.data.adapters.GSON;
import org.bson.Document;

import java.util.Arrays;

public class BankPokemon
{
    public PokemonProperties properties;
    public StatStorage ivs;
    public StatStorage evs;
    public ItemStack heldItem;

    public BankPokemon(PokemonProperties properties, StatStorage ivs, StatStorage evs)
    {
        this.properties = properties;
        this.ivs = ivs;
        this.evs = evs;
    }

    public BankPokemon(PokemonProperties properties)
    {
        this(properties, new StatStorage(), new StatStorage());
    }

    public BankPokemon()
    {
        this(new PokemonProperties());
    }

    public PokemonProperties getProperties()
    {
        return properties;
    }

    public StatStorage getIvs()
    {
        return ivs;
    }

    public StatStorage getEvs()
    {
        return evs;
    }

    public void toStorablePokemon(Pokemon pokemon)
    {
        pokemon.clone(true, false).createPokemonProperties(PokemonPropertyExtractor.ALL).copy();
        this.ivs = new StatStorage();
        this.ivs.convertFromIVs(pokemon.getIvs());
        this.evs = new StatStorage();
        this.evs.convertFromEVs(pokemon.getEvs());
        this.heldItem = pokemon.heldItem().copy();
    }

    public Pokemon fromStorablePokemon(boolean locked)
    {
        Pokemon pokemon = this.properties.create();
        Arrays.stream(Stats.values()).filter(stat -> stat != Stats.ACCURACY && stat != Stats.EVASION).forEachOrdered(stat -> {
            String statId = stat.getIdentifier().toString();
            if (evs.hasStat(statId))
                pokemon.getEvs().set(stat, evs.getOrDefault(statId));
            if (ivs.hasStat(statId))
                pokemon.getIvs().set(stat, ivs.getOrDefault(statId));
        });
        if (locked)
        {
            //TODO: apply unbreedable
            pokemon.getPersistentData().putBoolean("breedable", false);
        }

        //check if held item should be retained
        //if it should not be retained, clear the held item, otherwise reapply
        if (CobbledBank.instance.database.shouldKeepHeldItems())
        {
            if (this.heldItem != null && !this.heldItem.isEmpty())
                pokemon.swapHeldItem(this.heldItem, false);
        } else pokemon.removeHeldItem();

        return pokemon;
    }

    public Document toDocument() {
        Gson gson  = GSON.PRETTY_MAIN_GSON;
        String json = gson.toJson(this);
        return Document.parse(json);
    }

}
