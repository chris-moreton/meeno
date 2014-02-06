package snowmonkey.meeno;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.apache.http.StatusLine;
import org.joda.time.DateTime;
import snowmonkey.meeno.types.CustomerRef;
import snowmonkey.meeno.types.Markets;
import snowmonkey.meeno.types.SessionToken;
import snowmonkey.meeno.types.raw.*;

import java.io.*;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static snowmonkey.meeno.CountryLookup.Argentina;
import static snowmonkey.meeno.CountryLookup.UnitedKingdom;
import static snowmonkey.meeno.MarketFilterBuilder.TimeRange.between;
import static snowmonkey.meeno.types.TimeGranularity.MINUTES;

public class GenerateTestData {
    public static final File TEST_DATA_DIR = new File("test-data/generated");
    private MeenoConfig config;
    private HttpAccess httpAccess;
    private File loginFile;

    public GenerateTestData(MeenoConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws Exception {
//        FileUtils.cleanDirectory(TEST_DATA_DIR);
        MeenoConfig config = MeenoConfig.load();

        GenerateTestData generateTestData = new GenerateTestData(config);
        generateTestData.login();

//        generateTestData.listCountries();
//        generateTestData.listMarketCatalogue();
        generateTestData.placeOrders();
//        generateTestData.listCurrentOrders();
//        generateTestData.listCompetitions();
//        generateTestData.listEvents();
//        generateTestData.listEventTypes();
//        generateTestData.listMarketTypes();
//        generateTestData.listTimeRanges();
//        generateTestData.accountDetails();
//        generateTestData.accountFunds();

        generateTestData.cleanup();
    }

    private void cleanup() {
        loginFile.delete();
    }

    private void accountFunds() throws IOException {
        httpAccess.getAccountFunds(fileWriter(AccountFunds.getAccountFundsFile()));
    }

    private void accountDetails() throws IOException {
        httpAccess.getAccountDetails(fileWriter(AccountDetails.getAccountDetailsFile()));
    }

    private void listTimeRanges() throws IOException {
        httpAccess.listTimeRanges(fileWriter(TimeRanges.listTimeRangesFile()), MINUTES, new MarketFilterBuilder()
                .withEventTypeIds("1")
                .withEventIds(someEvent().id)
                .withMarketCountries(UnitedKingdom)
                .withMarketStartTime(between(new DateTime(), new DateTime().plusDays(1)))
                .build());
    }

    private void listMarketTypes() throws IOException {
        httpAccess.listMarketTypes(fileWriter(MarketTypes.listMarketTypesFile()));
    }

    private void listEventTypes() throws IOException {
        httpAccess.listEventTypes(fileWriter(EventTypes.listEventTypesFile()));
    }

    private void listEvents() throws IOException {
        httpAccess.listEvents(
                fileWriter(Events.listEventsFile()),
                new MarketFilterBuilder()
                        .withEventTypeIds("1")
                        .withMarketCountries(Argentina)
                        .withMarketStartTime(between(new DateTime(), new DateTime().plusDays(1)))
                        .build());
    }

    private void listCompetitions() throws IOException {
        httpAccess.listCompetitions(fileWriter(Competitions.listCompetitionsFile()),
                new MarketFilterBuilder()
                        .withEventTypeIds("1")
                        .withMarketCountries(Argentina)
                        .withMarketStartTime(between(new DateTime(), new DateTime().plusDays(1)))
                        .build());
    }

    private void listCurrentOrders() throws IOException {
        httpAccess.listCurrentOrders(fileWriter(listCurrentOrdersFile()));
    }

    private void placeOrders() throws IOException {
        snowmonkey.meeno.types.raw.MarketCatalogue marketCatalogue = aMarket();
        System.out.println(marketCatalogue);
        LimitOrder limitOrder = new LimitOrder(2.0D, 1000d, PersistenceType.LAPSE);
        PlaceInstruction placeLimitOrder = PlaceInstruction.createPlaceLimitOrder(marketCatalogue.runners.get(0).selectionId, Side.BACK, limitOrder);
        httpAccess.placeOrders(marketCatalogue.marketId, newArrayList(placeLimitOrder), CustomerRef.NONE, fileWriter(PlaceOrders.placeOrdersFile()));
    }

    private void listMarketCatalogue() throws IOException {
        httpAccess.listMarketCatalogue(fileWriter(MarketCatalogue.listMarketCatalogueFile()),
                MarketProjection.all(),
                MarketSort.FIRST_TO_START,
                5,
                new MarketFilterBuilder()
                        .withEventTypeIds("1")
                        .withMarketCountries(Argentina)
                        .withMarketStartTime(between(new DateTime(), new DateTime().plusDays(7)))
                        .build());
    }

    private void listCountries() throws IOException {
        httpAccess.listCountries(fileWriter(Countries.listCountriesFile()));
    }

    private void login() throws Exception {
        loginFile = Login.loginFile();

        HttpAccess.login(
                config.certificateFile(),
                config.certificatePassword(),
                config.username(),
                config.password(),
                config.delayedAppKey(),
                fileWriter(loginFile));

        SessionToken sessionToken = SessionToken.parseJson(FileUtils.readFileToString(loginFile));

        httpAccess = new HttpAccess(sessionToken, config.delayedAppKey(), Exchange.UK);

    }

    private static snowmonkey.meeno.types.raw.MarketCatalogue aMarket() throws IOException {
        Markets markets = Markets.parse(GenerateTestData.MarketCatalogue.listMarketCatalogueJson());
        return markets.iterator().next();
    }

    private static Event someEvent() throws IOException {
        return snowmonkey.meeno.types.Events.parse(Events.listEventsJson()).iterator().next();
    }

    public static HttpAccess.Processor fileWriter(final File file) {
        return new HttpAccess.Processor() {
            @Override
            public void process(StatusLine statusLine, InputStream in) throws IOException {
                if (file.exists())
                    return;

                try (Reader reader = new InputStreamReader(in, HttpAccess.UTF_8)) {
                    JsonElement parse = new JsonParser().parse(reader);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String json = gson.toJson(parse);
                    FileUtils.writeStringToFile(file, json);
                }
            }
        };
    }

    public static class MarketCatalogue {

        public static File listMarketCatalogueFile() {
            return new File(TEST_DATA_DIR, "listMarketCatalogue.json");
        }

        public static String listMarketCatalogueJson() throws IOException {
            return FileUtils.readFileToString(listMarketCatalogueFile());
        }
    }

    private static class PlaceOrders {
        private static File placeOrdersFile() {
            return new File(TEST_DATA_DIR, "placeOrders.json");
        }
    }

    private static File listCurrentOrdersFile() {
        return new File(TEST_DATA_DIR, "listCurrentOrders.json");
    }

    private static class CurrentOrders {
        public static String getCurrentOrderJson() throws IOException {
            JsonElement parse = new JsonParser().parse(listCurrentOrdersJson());
            JsonElement currentOrders = parse.getAsJsonObject().get("currentOrders");
            return array(currentOrders);
        }

        public static String listCurrentOrdersJson() throws IOException {
            return FileUtils.readFileToString(listCurrentOrdersFile());
        }
    }

    private static class Competitions {

        private static File listCompetitionsFile() {
            return new File(TEST_DATA_DIR, "listCompetitions.json");
        }

        public static String getCompetitionJson() throws IOException {
            return jsonForFirstElementInArray(listCompetitionsJson());
        }

        public static String listCompetitionsJson() throws IOException {
            return FileUtils.readFileToString(listCompetitionsFile());
        }
    }

    public static class Events {

        private static File listEventsFile() {
            return new File(TEST_DATA_DIR, "listEvents.json");
        }

        public static String getEventJson() throws IOException {
            return jsonForFirstElementInArray(listEventsJson());
        }

        public static String listEventsJson() throws IOException {
            return FileUtils.readFileToString(listEventsFile());
        }
    }

    public static class EventTypes {

        private static File listEventTypesFile() {
            return new File(TEST_DATA_DIR, "listEventTypes.json");
        }

        public static String getEventTypeJson() throws IOException {
            return jsonForFirstElementInArray(listEventTypesJson());
        }

        public static String listEventTypesJson() throws IOException {
            return FileUtils.readFileToString(listEventTypesFile());
        }
    }

    private static class MarketTypes {

        private static File listMarketTypesFile() {
            return new File(TEST_DATA_DIR, "listMarketTypes.json");
        }

        public static String getMarketTypeJson() throws IOException {
            return jsonForFirstElementInArray(listMarketTypesJson());
        }

        public static String listMarketTypesJson() throws IOException {
            return FileUtils.readFileToString(listMarketTypesFile());
        }
    }

    private static class TimeRanges {
        private static File listTimeRangesFile() {
            return new File(TEST_DATA_DIR, "listTimeRanges.json");
        }

        public static String getTimeRangeJson() throws IOException {
            return jsonForFirstElementInArray(listTimeRangesJson());
        }

        public static String listTimeRangesJson() throws IOException {
            return FileUtils.readFileToString(listTimeRangesFile());
        }
    }

    private static class AccountFunds {
        private static File getAccountFundsFile() {
            return new File(TEST_DATA_DIR, "getAccountFunds.json");
        }

        public static String getAccountFundsJson() throws IOException {
            return FileUtils.readFileToString(getAccountFundsFile());
        }
    }

    private static class AccountDetails {

        private static File getAccountDetailsFile() {
            return new File(TEST_DATA_DIR, "getAccountDetails.json");
        }

        public static String getAccountDetailsJson() throws IOException {
            return FileUtils.readFileToString(getAccountDetailsFile());
        }
    }

    public static class Login {

        public static String loginJson() throws IOException {
            return FileUtils.readFileToString(loginFile());
        }

        private static File loginFile() {
            return new File(new File("private"), UUID.randomUUID() + ".login.json");
        }
    }

    private static class Countries {
        private static File listCountriesFile() {
            return new File(TEST_DATA_DIR, "listCountries.json");
        }
    }

    private static String jsonForFirstElementInArray(String json) {
        JsonElement parse = new JsonParser().parse(json);
        return array(parse);
    }

    private static String array(JsonElement currentOrders) {
        JsonArray jsonArray = currentOrders.getAsJsonArray();
        JsonElement jsonElement = jsonArray.get(0);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }
}
