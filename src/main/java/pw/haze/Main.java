package pw.haze;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pw.ddong.option.OptionParser;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Haze
 * @since 9/24/2015
 */
public class Main {

    private static OptionParser parser;
    private static List<SteamAccount> accountList = new CopyOnWriteArrayList<>();
    private static Queue<Long> ids = new ArrayDeque<>();
    private static long startTime;

    public static void main(String... args) {
        System.out.println("Steam Account Generator");
        System.out.println("Made by Haze and Apteryx");
        System.out.println();

        parser = new OptionParser();
        parser.register("--digits", true, "<int> Length of SteamIDs to find.");
        parser.register("--accounts", true, "<int>Amount of accounts to generate.");
        parser.register("--verbose", false, "[true] Display the accounts and such.");
        parser.register("--setupcheck", false, "Checks if the user has setup their community profile.");
        parser.register("-oav", false, "Only Accept Valid accounts");
        parser.register("--time", false, "Tells you the time it took to generate the accounts.");
        parser.parse(args);

        if (parser.isPresent("--help", Optional.empty())) {
            parser.getAvailable().forEach(option -> System.out.println(String.format("%s - %s", option.getLabel(), option.getDescription())));
            System.exit(0);
        }

        if (parser.isPresent("--digits", Optional.empty()) && parser.isPresent("--accounts", Optional.empty())) {
            int digits = Integer.parseInt(parser.getFollowingParam("--digits"));
            int accounts = Integer.parseInt(parser.getFollowingParam("--accounts"));

            System.out.printf("SteamID length: %s\n", digits);
            System.out.printf("Accounts to be generated: %s\n", accounts);
            System.out.println(String.format("Generating %s accounts with %s digit Steam IDs...", accounts, digits));
            if(parser.isPresent("--time", Optional.empty()))
                startTime = System.currentTimeMillis();
            do {
                generateAccounts(generateBuilder());
            } while (accountList.size() < accounts);
            if(parser.isPresent("--time", Optional.empty())){
                long endTime = System.currentTimeMillis();
                long diff = endTime - startTime;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                System.out.printf("Completed Task (%s accounts) in %sm, %ss.\n", accounts, minutes, seconds);
            }
            saveAccounts();

        } else {
            System.out.println("Required arguments are missing.");
            parser.getAvailable().forEach(option -> System.out.println(String.format("%s - %s", option.getLabel(), option.getDescription())));
        }
    }

    private static void saveAccounts() {
        if (parser.isPresent("-oav", Optional.empty())) {
            fixAccounts();
        }
        String indexStr = "Index: URL - Name - Email - Setup";
        StringBuilder accountsToWrite = new StringBuilder();
        File accountsFile = new File("accounts.txt");
        accountsToWrite.append(String.format("%s\n", indexStr));
        accountList.forEach(acc -> accountsToWrite.append(String.format("http://steamcommunity.com/profiles/%s - %s - %s - %s", acc.getId(), acc.getProfileName(), acc.guessEmail(), (parser.isPresent("--setupcheck", Optional.empty()) ? isSetup(String.format("http://steamcommunity.com/profiles/%s", acc.getId())) : ""))).append("\r\n"));
        try {
            Files.write(accountsFile.toPath(), accountsToWrite.toString().getBytes());
            System.out.println(String.format("A list of accounts (%s) was saved to \"%s\"", accountList.size(), accountsFile.getAbsolutePath()));
        } catch (IOException e) {
            System.out.println("Accounts file failed to save, a stacktrace has been printed.");
            e.printStackTrace();
        }
    }

    private static void fixAccounts() {
        accountList = accountList.stream().filter(SteamAccount::isValid).collect(Collectors.toList());
    }

    private static StringBuilder generateBuilder() {
        int digits = Integer.parseInt(parser.getFollowingParam("--digits"));
        String accs = generateID(100, digits);
        StringBuilder builder = new StringBuilder();
        try {
            HttpURLConnection client = (HttpURLConnection) new URL(String.format("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=41BC745487A1D96139B556002BF3A150&steamids=%s&format=json", accs)).openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Accept-Charset", "UTF-8");
            client.setDoOutput(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String ln = "";
            while (ln != null) {
                builder.append(ln);
                ln = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to read from Steam Community, a stacktrace will been printed.");
            e.printStackTrace();
        }
        return builder;
    }

    private static void generateAccounts(StringBuilder builder) {
        JsonElement element = new JsonParser().parse(builder.toString());
        JsonObject object = element.getAsJsonObject();
        JsonObject resp = object.get("response").getAsJsonObject();
        JsonArray array = resp.get("players").getAsJsonArray();
        String[] paramsToCheck = new String[]{
                "lastlogoff", "steamid", "profileurl", "communityvisibilitystate", "personaname"
        };

        int index = 0;
        for (JsonElement e : array) {
            int accountsNeeded = Integer.parseInt(parser.getFollowingParam("--accounts"));
            if (accountList.size() > accountsNeeded)
                return;
            JsonObject obj = e.getAsJsonObject();
            if (obj == null)
                continue;

            int paramsNull = 0;
            for (String str : paramsToCheck) {
                if (obj.get(str) == null)
                    paramsNull++;
            }

            if (paramsNull > 0)
                continue;

            long time = obj.get("lastlogoff").getAsLong();
            long id = obj.get("steamid").getAsLong();
            String url = obj.get("profileurl").getAsString();
            int vis = obj.get("communityvisibilitystate").getAsInt();
            String pName = obj.get("personaname").getAsString();
            SteamAccount acc = new SteamAccount(Long.toString(id), url, Integer.toString(vis), pName, time);
            if (accountList.contains(acc))
                continue;
            if (parser.isPresent("--verbose", Optional.empty())) {
                System.out.println(String.format("#%s / #%s / %s - http://steamcommunity.com/profiles/%s - %s - %s - %s", index, accountList.size(), accountsNeeded, acc.getId(), acc.getProfileName(), acc.guessEmail(), (parser.isPresent("--setupcheck", Optional.empty()) ? isSetup(String.format("http://steamcommunity.com/profiles/%s", acc.getId())) : "...")));
            }
            accountList.add(acc);
            index++;
        }
    }

    private static String generateID(int accounts, int digits) {
        Random rand = new Random();
        StringBuilder digitBuilder = new StringBuilder();
        StringJoiner joiner = new StringJoiner(",");
        for (int accountIndex = 0; accountIndex < accounts; accountIndex++) {
            for (int digitIndex = 0; digitIndex < digits; digitIndex++) {
                digitBuilder.append(rand.nextInt(10));
            }
            String steam = digitBuilder.toString();
            digitBuilder.replace(0, digitBuilder.length(), "");
            long hashedID = toCommunityID(String.format("STEAM_0:%s:%s", rand.nextInt(1), steam));
            String idStr = Long.toString(hashedID);
            if (!ids.contains(hashedID)) {
                ids.add(hashedID);
                joiner.add(idStr);
            } else {
                accountIndex--;
            }
        }
        return joiner.toString();
    }

    private static long toCommunityID(String id) {
        String[] args = id.split(":");
        return Math.addExact(((Long.parseLong(args[1]) * 2) + Long.parseLong(args[2])), Long.parseLong("76561197960265728"));
    }

    private static boolean isSetup(String url) {
        //not going to remove, but builder is never used.
        StringBuilder builder = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setDoOutput(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String ln;
            while ((ln = reader.readLine()) != null) {
                builder.append(ln);
                if (ln.contains("This user has not yet set up their Steam Community profile.")) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
