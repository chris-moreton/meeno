package snowmonkey.meeno;

import live.GenerateTestData;
import org.junit.Test;
import snowmonkey.meeno.types.Event;
import snowmonkey.meeno.types.Events;

public class EventsTest {
    @Test
    public void test() throws Exception {
        Events events = Events.parse(GenerateTestData.Events.listEventsJson());

        Event next = events.iterator().next();
        System.out.println("next = " + next);
    }
}
