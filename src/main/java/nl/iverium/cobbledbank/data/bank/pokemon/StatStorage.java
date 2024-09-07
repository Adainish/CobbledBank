package nl.iverium.cobbledbank.data.bank.pokemon;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.IVs;

import java.util.HashMap;

public class StatStorage
{
    public HashMap<String, Integer> statStorage = new HashMap<>();

    public void setStat(String key, int value) {
        statStorage.put(key, value);
    }

    public int getStat(String key) {
        return statStorage.get(key);
    }

    public int getOrDefault(String key)
    {
        return statStorage.getOrDefault(key, 0);
    }

    public void removeStat(String key) {
        statStorage.remove(key);
    }

    public void clearStats() {
        statStorage.clear();
    }

    public boolean hasStat(String key) {
        return statStorage.containsKey(key);
    }

    public boolean hasStat(String key, int value) {
        return statStorage.containsKey(key) && statStorage.get(key) == value;
    }

    public boolean hasStat(String key, int value, boolean greater) {
        return statStorage.containsKey(key) && (greater ? statStorage.get(key) > value : statStorage.get(key) < value);
    }

    public EVs convertToEVs() {
        EVs evs = new EVs();
        statStorage.keySet().forEach(key -> {
            Stat stat = Stats.Companion.getStat(key);
            evs.set(stat, getOrDefault(key));
        });
        return evs;
    }

    public IVs convertToIVs() {
        IVs ivs = new IVs();
        statStorage.keySet().forEach(key -> {
            Stat stat = Stats.Companion.getStat(key);
            ivs.set(stat, getOrDefault(key));
        });
        return ivs;
    }

    public void convertFromEVs(EVs evs) {
        Stats.Companion.getPERMANENT().stream().filter(stat -> !stat.equals(Stats.EVASION) && !stat.equals(Stats.ACCURACY)).forEachOrdered(stat -> setStat(stat.getIdentifier().toString(), evs.getOrDefault(stat)));
    }

    public void convertFromIVs(IVs ivs) {
        Stats.Companion.getPERMANENT().stream().filter(stat -> !stat.equals(Stats.EVASION) && !stat.equals(Stats.ACCURACY)).forEachOrdered(stat -> setStat(stat.getIdentifier().toString(), ivs.getOrDefault(stat)));
    }
}
