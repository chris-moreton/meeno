package live.raw;

import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.HttpExchangeOperations;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.types.BetStatus;
import snowmonkey.meeno.types.ClearedOrderSummary;
import snowmonkey.meeno.types.ClearedOrderSummaryReport;

import static java.time.ZonedDateTime.now;
import static live.raw.GenerateTestData.LIST_CLEARED_ORDERS_FILE;
import static live.raw.GenerateTestData.fileWriter;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.types.TimeRange.between;

public class ListClearedOrdersTest extends AbstractLiveTestCase {
    @Test
    public void canGetSettledOrders() throws Exception {

        httpAccess.listClearedOrders(fileWriter(LIST_CLEARED_ORDERS_FILE),
                BetStatus.SETTLED,
                between(now().minusMonths(3), now()), 0
        );

        ClearedOrderSummary clearedOrderSummaryReport = JsonSerialization.parse(readFileToString(LIST_CLEARED_ORDERS_FILE.toFile()), ClearedOrderSummary.class);
        for (ClearedOrderSummaryReport orderSummaryReport : clearedOrderSummaryReport.clearedOrders) {
            System.out.println("clearedOrderSummaryReport = " + orderSummaryReport);
        }
    }

    @Test
    public void canGetSettledOrders2() throws Exception {

        httpAccess.listClearedOrders(fileWriter(LIST_CLEARED_ORDERS_FILE),
                BetStatus.SETTLED,
                between(now().minusMonths(3), now()),
                0
        );

        HttpExchangeOperations httpExchangeOperations = new HttpExchangeOperations(httpAccess);
        ClearedOrderSummary clearedOrderSummaryReport = httpExchangeOperations.listClearedOrders(
                BetStatus.SETTLED,
                between(now().minusMonths(3), now()),
                0
        );

        for (ClearedOrderSummaryReport orderSummaryReport : clearedOrderSummaryReport.clearedOrders) {
            System.out.println("clearedOrderSummaryReport = " + orderSummaryReport);
        }
    }

    @Test
    public void canGetCancelledOrders() throws Exception {

        httpAccess.listClearedOrders(fileWriter(LIST_CLEARED_ORDERS_FILE),
                BetStatus.LAPSED,
                between(now().minusMonths(3), now()), 0
        );

        ClearedOrderSummary clearedOrderSummaryReport = JsonSerialization.parse(readFileToString(LIST_CLEARED_ORDERS_FILE.toFile()), ClearedOrderSummary.class);
        for (ClearedOrderSummaryReport orderSummaryReport : clearedOrderSummaryReport.clearedOrders) {
            System.out.println("clearedOrderSummaryReport = " + orderSummaryReport);
        }
    }
}
