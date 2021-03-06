package snowmonkey.meeno.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.List;

public final class MarketCatalogue extends ImmutbleType {

    public final MarketId marketId;
    public final String marketName;
    public final ZonedDateTime marketStartTime;
    @Nullable
    public final MarketDescription description;
    public final ImmutableList<RunnerCatalog> runners;
    @Nullable
    public final EventType eventType;
    @Nullable
    public final Competition competition;
    public final Event event;

    public MarketCatalogue(MarketId marketId, String marketName, ZonedDateTime marketStartTime, @Nullable MarketDescription description, @Nullable List<RunnerCatalog> runners, @Nullable EventType eventType, @Nullable Competition competition, Event event) {
        this.marketId = marketId;
        this.marketName = marketName;
        this.marketStartTime = marketStartTime;
        this.description = description;
        this.runners = runners == null ? ImmutableList.of() : ImmutableList.copyOf(runners);
        this.eventType = eventType;
        this.competition = competition;
        this.event = event;
    }

    public ImmutableMap<SelectionId, RunnerCatalog> runners() {
        ImmutableMap.Builder<SelectionId, RunnerCatalog> builder = ImmutableMap.builder();
        for (RunnerCatalog runner : runners) {
            builder.put(runner.selectionId, runner);
        }
        return builder.build();
    }
}
