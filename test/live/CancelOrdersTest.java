package live;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;
import snowmonkey.meeno.DefaultProcessor;
import snowmonkey.meeno.HttpExchangeOperations;
import snowmonkey.meeno.requests.CancelInstruction;
import snowmonkey.meeno.types.CancelExecutionReport;
import snowmonkey.meeno.types.CurrentOrderSummary;
import snowmonkey.meeno.types.CurrentOrderSummaryReport;
import snowmonkey.meeno.types.MarketId;
import snowmonkey.meeno.types.OrderProjection;

import static java.time.ZonedDateTime.*;
import static live.GenerateTestData.*;
import static org.apache.commons.io.FileUtils.*;
import static snowmonkey.meeno.JsonSerialization.parse;
import static snowmonkey.meeno.requests.ListCurrentOrders.*;
import static snowmonkey.meeno.types.TimeRange.*;

public class CancelOrdersTest extends AbstractLiveTestCase {

    @Test
    public void test() throws Exception {
        HttpExchangeOperations httpExchangeOperations = new HttpExchangeOperations(httpAccess);

        CurrentOrderSummaryReport currentOrders = httpExchangeOperations.listCurrentOrders(new Builder()
                .withOrderProjection(OrderProjection.ALL)
                .withDateRange(between(now().minusDays(1), now().plusMonths(1)))
                .build());

        Iterable<CancelExecutionReport> cancelled = httpExchangeOperations.cancelAllOrders(currentOrders);
        for (CancelExecutionReport cancelExecutionReport : cancelled) {
            System.out.println("cancelExecutionReport = " + cancelExecutionReport);
        }
    }

    @Test
    public void cancelAllOrders() throws Exception {

        httpAccess.listCurrentOrders(fileWriter(LIST_CURRENT_ORDERS_FILE), new Builder().build());

        CurrentOrderSummaryReport currentOrders = parse(readFileToString(LIST_CURRENT_ORDERS_FILE.toFile()), CurrentOrderSummaryReport.class);

        Multimap<MarketId, CancelInstruction> cancelInstructions = ArrayListMultimap.create();
        for (CurrentOrderSummary currentOrder : currentOrders.currentOrders) {
            MarketId marketId = currentOrder.marketId;
            CancelInstruction cancel = CancelInstruction.cancel(currentOrder.betId);
            cancelInstructions.put(marketId, cancel);
        }

        for (MarketId marketId : cancelInstructions.keySet()) {
            httpAccess.cancelOrders(marketId, cancelInstructions.get(marketId), (statusLine, in) -> {
                String s = DefaultProcessor.processResponse(statusLine, in);
                System.out.println(s);
                return s;
            }, null);
        }
    }

}
