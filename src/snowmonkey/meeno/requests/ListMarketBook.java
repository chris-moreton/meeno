package snowmonkey.meeno.requests;

import com.google.common.collect.ImmutableList;
import snowmonkey.meeno.types.ImmutbleType;
import snowmonkey.meeno.types.MarketId;
import snowmonkey.meeno.types.MatchProjection;
import snowmonkey.meeno.types.OrderProjection;
import snowmonkey.meeno.types.PriceProjection;

import java.util.List;

public class ListMarketBook extends ImmutbleType {

    public final List<MarketId> marketIds;
    public final PriceProjection priceProjection;
    public final OrderProjection orderProjection;
    public final MatchProjection matchProjection;
    public final String currencyCode;
    public final String locale;

    public ListMarketBook(Iterable<MarketId> marketIds, PriceProjection priceProjection, OrderProjection orderProjection, MatchProjection matchProjection, String currencyCode, String locale) {
        this.marketIds = ImmutableList.copyOf(marketIds);
        this.priceProjection = priceProjection;
        this.orderProjection = orderProjection;
        this.matchProjection = matchProjection;
        this.currencyCode = currencyCode;
        this.locale = locale;

        if (!marketIds.iterator().hasNext())
            throw new IllegalStateException("No market ids in request");
    }
}
