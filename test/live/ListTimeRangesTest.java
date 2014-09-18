package live;

import org.junit.Test;
import snowmonkey.meeno.MarketFilterBuilder;
import snowmonkey.meeno.types.EventId;
import snowmonkey.meeno.types.EventTypeId;
import snowmonkey.meeno.types.Navigation;

import java.time.ZonedDateTime;

import static live.GenerateTestData.TimeRanges.*;
import static live.GenerateTestData.*;
import static snowmonkey.meeno.CountryLookup.*;
import static snowmonkey.meeno.types.EventTypeName.*;
import static snowmonkey.meeno.types.TimeGranularity.*;
import static snowmonkey.meeno.types.TimeRange.*;

public class ListTimeRangesTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        Navigation navigation = navigation().events(SOCCER).get(0);

        EventId eventId = navigation.eventId();
        EventTypeId eventTypeId = navigation.parent().eventTypeId();

        httpAccess.listTimeRanges(fileWriter(listTimeRangesFile()), MINUTES, new MarketFilterBuilder()
                .withEventTypeIds(eventTypeId)
                .withEventIds(eventId)
                .withMarketCountries(UnitedKingdom)
                .withMarketStartTime(between(ZonedDateTime.now(), ZonedDateTime.now().plusDays(1)))
                .build());
    }
}
