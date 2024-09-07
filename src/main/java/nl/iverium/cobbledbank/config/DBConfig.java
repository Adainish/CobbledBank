package nl.iverium.cobbledbank.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import nl.iverium.cobbledbank.CobbledBank;
import nl.iverium.cobbledbank.data.adapters.GSON;

import java.io.*;

public class DBConfig
{
    public boolean enabled = false;
    public String mongoDBURI = "";
    public String database = "cobbledbank";
    public String tableName = "bank_data";

    public DBConfig()
    {

    }


    public static void writeConfig()
    {
        File dir = CobbledBank.getConfigDir();
        dir.mkdirs();
        Gson gson  = GSON.PRETTY_MAIN_GSON;
        DBConfig config = new DBConfig();
        try {
            File file = new File(dir, "db_settings.json");
            if (file.exists())
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

    public static DBConfig getConfig()
    {
        File dir = CobbledBank.getConfigDir();
        dir.mkdirs();
        Gson gson  = GSON.PRETTY_MAIN_GSON;
        File file = new File(dir, "db_settings.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            CobbledBank.getLog().error("Something went wrong attempting to read the Config");
            return null;
        }

        return gson.fromJson(reader, DBConfig.class);
    }
}
