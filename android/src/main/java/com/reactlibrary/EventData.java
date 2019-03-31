package com.reactlibrary;

public class EventData {
    private String eventType;
    private int noteNumber;
    private long timestamp;

    public EventData() {
    }

    public EventData(String eventType, int noteNumber, long timestamp) {
        this.eventType = eventType;
        this.noteNumber = noteNumber;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getNoteNumber() {
        return noteNumber;
    }

    public void setNoteNumber(int noteNumber) {
        this.noteNumber = noteNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
