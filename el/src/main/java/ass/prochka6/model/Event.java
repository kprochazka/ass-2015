package ass.prochka6.model;

import java.util.Date;

/**
 * @author Kamil Prochazka
 */
public class Event extends IdEntity {

    private Event parent;

    private EventType eventType;

    private String title;

    private String description;

    private User organizer;

    private Location location;

    private Boolean shareable;

    private Date date;

    private int capacity;

    private Priority priority;

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Boolean getShareable() {
        return shareable;
    }

    public void setShareable(Boolean shareable) {
        this.shareable = shareable;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Event getParent() {
        return parent;
    }

    public void setParent(Event parent) {
        this.parent = parent;
    }

    public void printX(String x, Event e, Object o) {
        System.out.println("x=" + x + ", event=" + e + ", object=" + o);
    }

    public Event returnX() {
        return this;
    }
}
