package storage;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import org.json.JSONObject;

public class DataStore {

    private static void ensureDataFolderExists() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
    }

    public static JSONObject loadOldData() throws Exception {
        File oldDataFile = new File("data/daily_data.json");
        if (!oldDataFile.exists()) {
            return null;
        }
        String oldContent = new String(Files.readAllBytes(oldDataFile.toPath()));
        return new JSONObject(oldContent);
    }

    public static boolean shouldRunToday(JSONObject oldData, String[] symbols) {
        if (oldData == null) {
            return true;
        }
        if (!oldData.has(symbols[0])) {
            return true;
        }
        JSONObject firstStock = oldData.getJSONObject(symbols[0]);
        if (!firstStock.has("checked_at")) {
            return true;
        }
        long oldCheckedAt = firstStock.getLong("checked_at");
        long now = System.currentTimeMillis();
        long hoursSinceLastCheck = (now - oldCheckedAt) / (1000 * 60 * 60);

        if (hoursSinceLastCheck < 20) {
            System.out.println("Less than 20 hours since last check — this project is designed to run once per day via a scheduler. Skipping this run.");
            return false;
        }
        return true;
    }

    public static void appendToLog(JSONObject logEntry) throws Exception {
        ensureDataFolderExists();
        FileWriter logFile = new FileWriter("data/accuracy_log.jsonl", true);
        logFile.write(logEntry.toString() + "\n");
        logFile.close();
    }

    public static void saveDailyData(JSONObject allData) throws Exception {
        ensureDataFolderExists();
        FileWriter file = new FileWriter("data/daily_data.json");
        file.write(allData.toString());
        file.close();
    }
}