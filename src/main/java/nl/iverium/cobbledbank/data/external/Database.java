package nl.iverium.cobbledbank.data.external;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import nl.iverium.cobbledbank.CobbledBank;
import nl.iverium.cobbledbank.data.adapters.GSON;
import nl.iverium.cobbledbank.util.Logger;
import nl.iverium.cobbledbank.data.MongoCodecStringArray;
import nl.iverium.cobbledbank.data.bank.PokeBank;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Optional;
import java.util.UUID;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Database
{

    public MongoClientSettings mongoClientSettings;
    public MongoClient mongoClient;
    public MongoDatabase database;
    public MongoCollection<Document> collection;

    public Database()
    {
        if (CobbledBank.instance.dbConfig.enabled)
        {
            if (this.init())
            {
                Logger.log("Connected to the database successfully");
            } else Logger.log("Failed connecting to the database- please check the error for more info or contact the developer");
        } else Logger.log("Database not enabled, fusions will *not* be stored and this mod may not properly work or crash as a result.");
    }

    public void shutdown()
    {
        //close connection
        if (mongoClient != null) {

            Logger.log("Shutting down database connection");
            mongoClient.close();
        } else Logger.log("Something went wrong while shutting down the mongo db, was it ever set up to begin with?");
    }

    public boolean addPokeBank(PokeBank pokeBank, boolean overwrite) {
        Document document = pokeBank.toDocument();
        if (collection.find(Filters.eq("uuid", pokeBank.uuid.toString())).first() != null)
        {
            if (!overwrite) {
                Logger.log("PokeBank already exists in the database.");
                return false;
            } else {
                Logger.log("PokeBank already exists in the database, overwriting it.");
                return overwritePokeBank(pokeBank);
            }
        }
        collection.replaceOne(Filters.eq("uuid", pokeBank.uuid.toString()), document, new ReplaceOptions().upsert(true));
        Logger.log("Added PokeBank for %uuid% to the database".replace("%uuid%", pokeBank.uuid.toString()));
        return true;
    }

    public boolean overwritePokeBank(PokeBank pokeBank) {
        Document document = pokeBank.toDocument();
        collection.replaceOne(Filters.eq("uuid", pokeBank.uuid.toString()), document, new ReplaceOptions().upsert(true));
        Logger.log("Overwrote PokeBank for %uuid% in the database".replace("%uuid%", pokeBank.uuid.toString()));
        return true;
    }

    public boolean removePokeBank(PokeBank pokeBank) {
        Logger.log("Removing PokeBank for %uuid% from the database".replace("%uuid%", pokeBank.uuid.toString()));
        collection.deleteOne(Filters.eq(pokeBank.uuid.toString()));
        return true;
    }

    public boolean lockGlobalBank() {
        Logger.log("Locking the global bank");
        collection.insertOne(new Document("global_lock", true));
        return true;
    }

    public boolean unlockGlobalBank() {
        Logger.log("Unlocking the global bank");
        collection.deleteOne(Filters.eq("global_lock", true));
        return true;
    }

    public boolean isGloballyLocked() {
        return collection.find(Filters.eq("global_lock", true)).first() != null;
    }

    public boolean shouldKeepHeldItems() {
        Document document = collection.find(Filters.eq("keep_held_items", true)).first();
        return document != null;
    }

    public boolean setKeepHeldItems(boolean keepHeldItems) {
        Logger.log("Setting the keep held items to %keepHeldItems%".replace("%keepHeldItems%", String.valueOf(keepHeldItems)));
        Document document = collection.find(Filters.exists("keep_held_items")).first();
        if (document != null) {
            collection.updateOne(Filters.exists("keep_held_items"), new Document("$set", new Document("keep_held_items", keepHeldItems)));
        } else {
            collection.insertOne(new Document("keep_held_items", keepHeldItems));
        }
        return true;
    }

    public boolean setShouldKeepHeldItems(boolean keepHeldItems) {
        return setKeepHeldItems(keepHeldItems);
    }

    public boolean getShouldKeepHeldItems() {
        return shouldKeepHeldItems();
    }

    public Optional<PokeBank> getOrCreatePokeBank(UUID playerUUID)
    {
        PokeBank pokeBank = getPokeBank(playerUUID);
        if (pokeBank == null)
        {
            pokeBank = new PokeBank(playerUUID);
            addPokeBank(pokeBank, false);
        }
        return Optional.of(pokeBank);
    }

    public PokeBank getPokeBank(UUID playerUUID) {
        Document document = collection.find(Filters.eq("uuid", playerUUID.toString())).first();
        if (document == null)
            return null;
        return fromDocument(document);
    }

    public boolean setMaxBankSize(int size, boolean overwrite) {
        Logger.log("Setting the max bank size to %size%".replace("%size%", String.valueOf(size)));
        //check if bank size is already set
        if (collection.find(Filters.exists("max_bank_size")).first() != null) {
            if (!overwrite) {
                try {
                    //check if size is equal to the current size then update the error message
                    if (collection.find(Filters.eq("max_bank_size", size)).first().getInteger("max_bank_size") == size) {
                        Logger.log("Max bank size is already set to %size%".replace("%size%", String.valueOf(size)));
                    } else {
                        Logger.log("Max bank size is already set to %size%, to change it, use the appropriate command to overwrite.".replace("%size%", String.valueOf(size)));
                    }
                    return false;
                } catch (NullPointerException e) {
                    Logger.log(e);
                    return false;
                }
            } else {
                Logger.log("Max bank size is already set to %size%, overwriting it".replace("%size%", String.valueOf(size)));
                collection.replaceOne(Filters.exists("max_bank_size"), new Document("max_bank_size", size));
                return true;
            }
        } else {
            Logger.log("Max bank size is not set, setting it to %size%".replace("%size%", String.valueOf(size)));
            collection.insertOne(new Document("max_bank_size", size));
            return true;
        }
    }

    public int getMaxBankSize() {
        Document document = collection.find(Filters.exists("max_bank_size")).first();
        if (document == null) {
            Logger.log("Max bank size is not set, returning 0. Please adjust this using the command if this is incorrect");
            return 0;
        }
        return document.getInteger("max_bank_size");
    }

    public PokeBank fromDocument(Document document) {
        Gson gson = GSON.PRETTY_MAIN_GSON;
        return gson.fromJson(document.toJson(), PokeBank.class);
    }

    public boolean init()
    {
        try {
            ConnectionString connectionString = new ConnectionString(CobbledBank.instance.dbConfig.mongoDBURI);
            CodecRegistry codecRegistry = fromRegistries(
                    CodecRegistries.fromCodecs(new MongoCodecStringArray()), // <---- this is the custom codec
                    MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );
            mongoClientSettings = MongoClientSettings.builder().codecRegistry(codecRegistry).applyConnectionString(connectionString).retryWrites(true).build();
            mongoClient = MongoClients.create(mongoClientSettings);
            database = mongoClient.getDatabase(CobbledBank.instance.dbConfig.database);
            collection = database.getCollection(CobbledBank.instance.dbConfig.tableName);
            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
