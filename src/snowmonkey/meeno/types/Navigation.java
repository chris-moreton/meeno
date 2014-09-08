package snowmonkey.meeno.types;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.types.raw.TimeRange;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class Navigation {
    private final Navigation parent;
    private final Type type;
    private final String id;
    private final String name;
    private final JsonArray children;

    public Navigation(Navigation parent, Type type, String id, String name, JsonArray children) {
        this.parent = parent;
        this.type = type;
        this.id = id;
        this.name = name;
        this.children = children;
    }

    public EventTypeName eventTypeName() {
        if (this.type.equals(Type.EVENT_TYPE))
            return new EventTypeName(name);
        throw new IllegalStateException(this + " is not an event type name");
    }

    @Override
    public String toString() {
        return StringUtils.join(new String[]{id, name, type.name(), children.size() + " children"}, ",");
    }

    public static Navigation parse(String json) {
        JsonElement parsed = new JsonParser().parse(json);

        JsonObject childObj = parsed.getAsJsonObject();

        return makeRootNode(childObj);
    }

    public List<Navigation> getEventTypes() {
        return findImmediateChildren(Type.EVENT_TYPE);
    }

    private List<Navigation> findImmediateChildren(Type eventType) {
        return children().stream().filter(child -> child.type.equals(eventType)).collect(Collectors.toList());
    }

    public List<Navigation> events(EventTypeName eventTypeName) {
        for (Navigation topLevelEvent : getEventTypes()) {
            if (topLevelEvent.eventTypeName().equals(eventTypeName)) {
                return topLevelEvent.children();
            }
        }

        return Collections.EMPTY_LIST;
    }

    public String groupName() {
        if (type.equals(Type.GROUP))
            return name;

        throw new IllegalStateException(this + " is not a group");
    }

    private EventTypeId eventTypeId() {
        if (type.equals(Type.EVENT_TYPE))
            return new EventTypeId(id);

        throw new IllegalStateException(this + " is not an event type");
    }

    public String eventName() {
        if (type.equals(Type.EVENT))
            return name;

        throw new IllegalStateException(this + " is not an event");
    }

    public List<Navigation> children() {
        List<Navigation> results = new ArrayList<>();
        for (JsonElement child : children) {
            JsonObject asJsonObject = child.getAsJsonObject();
            Navigation childNode = makeChildNode(asJsonObject, this);
            if (childNode != null)
                results.add(childNode);
        }
        return results;
    }

    public String print(List<Navigation> eventTypes) {
        return StringUtils.join(eventTypes.stream().map(nav -> nav.name).iterator(), ",");
    }

    private static Navigation makeRootNode(JsonObject childObj) {
        return makeChildNode(childObj, null);
    }

    private static Navigation makeChildNode(JsonObject childObj, Navigation parent1) {
        Type type = Type.valueOf(childObj.get("type").getAsString());
        String id = childObj.get("id").getAsString();
        String name = childObj.get("name").getAsString();

        if (!type.equals(Type.MARKET)) {
            JsonArray children = childObj.get("children").getAsJsonArray();

            return new Navigation(parent1, type, id, name, children);
        }

        return null;
    }

    public List<Market> markets() {
        List<Market> results = new ArrayList<>();
        for (JsonElement child : children) {
            JsonObject childObj = child.getAsJsonObject();
            Type type = Type.valueOf(childObj.get("type").getAsString());

            if (type.equals(Type.MARKET)) {
                String id = childObj.get("id").getAsString();
                String name = childObj.get("name").getAsString();
                String exchangeId = childObj.get("exchangeId").getAsString();
                String marketStartTime = childObj.get("marketStartTime").getAsString();
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(marketStartTime, JsonSerialization.BETFAIR_DATE_TIME_FORMAT);
                results.add(new Market(
                        this,
                        new ExchangeId(exchangeId),
                        new MarketId(id),
                        zonedDateTime,
                        name,
                        type
                ));
            }
        }

        return results;
    }

    public Navigation parent() {
        return parent;
    }

    public static class Markets implements Iterable<Market> {
        private final Collection<Market> markets;

        public Markets(List<Market> markets) {
            this.markets = markets;
        }

        @Override
        public Iterator<Market> iterator() {
            return markets.iterator();
        }

        public Iterable<MarketId> marketsIds() {
            return markets.stream().map(m -> m.id).collect(Collectors.toList());
        }

        public Iterable<FootballMarket> asFootballMarkets() {
            return markets.stream().map(m -> new FootballMarket(m)).collect(Collectors.toList());
        }
    }

    public Markets findMarkets(EventTypeName eventTypeName, TimeRange timeRange, String marketNamePattern) {
        return findMarkets(eventTypeName, timeRange, Pattern.compile(marketNamePattern));
    }

    public Markets findMarkets(EventTypeName eventTypeName, TimeRange timeRange, Pattern pattern) {
        List<Market> markets = new ArrayList<>();

        for (Navigation event : events(eventTypeName)) {
            findMarkets(timeRange, markets, event.children(), pattern);
        }

        return new Markets(markets);
    }

    private void findMarkets(TimeRange timeRange, List<Market> markets, List<Navigation> children, Pattern marketNamePattern) {
        for (Navigation navigation : children) {
            for (Market market : navigation.markets()) {

                if (!market.startSDuring(timeRange))
                    continue;

                if (!marketNamePattern.matcher(market.name).matches())
                    continue;

                markets.add(market);
            }

            findMarkets(timeRange, markets, navigation.children(), marketNamePattern);
        }
    }

    public String printHierarchy() {
        List<String> names = newArrayList(name);

        Navigation node = parent;
        while (node.parent != null) {
            names.add(node.name);
            node = node.parent;
        }

        return StringUtils.join(Lists.reverse(names), " / ");
    }

    public enum Type {
        GROUP, EVENT_TYPE, EVENT, RACE, MARKET
    }

    public static class Market extends ImmutbleType {
        public final Navigation parent;
        public final ExchangeId exchangeId;
        public final MarketId id;
        public final ZonedDateTime marketStartTime;
        public final String name;
        public final Type type;

        public Market(Navigation parent, ExchangeId exchangeId, MarketId id, ZonedDateTime marketStartTime, String name, Type type) {
            this.parent = parent;
            this.exchangeId = exchangeId;
            this.id = id;
            this.marketStartTime = marketStartTime;
            this.name = name;
            this.type = type;
        }

        public boolean startSDuring(TimeRange timeRange) {
            return !marketStartTime.isBefore(timeRange.from) && marketStartTime.isBefore(timeRange.to);
        }

        public Navigation group() {
            Navigation node = parent;
            while (node.type.equals(Type.GROUP)) {
                node = node.parent;

                if (node == null)
                    throw new IllegalStateException(this + " does not have a group");
            }

            return node;
        }

        public EventTypeId eventTypeId() {
            Navigation node = parent;
            while (node.parent != null) {
                if (node.type.equals(Type.EVENT_TYPE))
                    return node.eventTypeId();
                node = node.parent;
            }

            throw new IllegalStateException("Could not find the event type");
        }
    }

}
