package snowmonkey.meeno;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import snowmonkey.meeno.types.EventType;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventTypes {
    private final Map<String, EventType> eventTypesByName = new LinkedHashMap<>();

    public static EventTypes parse(String json) {
        JsonElement parse = new JsonParser().parse(json);

        EventTypes eventTypes = new EventTypes();

        for (JsonElement jsonElement : parse.getAsJsonArray()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject eventTypeObj = jsonObject.get("eventType").getAsJsonObject();
            EventType eventType = new EventType(
                    eventTypeObj.getAsJsonPrimitive("id").getAsString(),
                    eventTypeObj.getAsJsonPrimitive("name").getAsString(),
                    jsonObject.getAsJsonPrimitive("marketCount").getAsInt()
            );
            eventTypes.add(eventType);
        }

        return eventTypes;
    }

    private void add(EventType eventType) {
        eventTypesByName.put(eventType.name, eventType);
    }

    public EventType lookup(String eventName) {
        EventType eventType = eventTypesByName.get(eventName);
        if (eventName == null)
            throw new Defect("There is no event named '" + eventName + "'");
        return eventType;
    }
}