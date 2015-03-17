package ass.prochka6.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class EventModel {

    private Event[] eventArray;
    private List<Event> eventList;
    private Set<Event> eventSet;
    private Map<Long, Event> eventMap;


    public void init(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        eventArray = events.toArray(new Event[events.size()]);
        eventList = new ArrayList<>(events);
        eventSet = new HashSet<>(events);
        eventMap = events.stream().collect(Collectors.toMap(Event::getId, Function.identity()));
    }


    public Event[] getEventArray() {
        return eventArray;
    }

    public void setEventArray(Event[] eventArray) {
        this.eventArray = eventArray;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public Set<Event> getEventSet() {
        return eventSet;
    }

    public void setEventSet(Set<Event> eventSet) {
        this.eventSet = eventSet;
    }

    public Map<Long, Event> getEventMap() {
        return eventMap;
    }

    public void setEventMap(Map<Long, Event> eventMap) {
        this.eventMap = eventMap;
    }
}
