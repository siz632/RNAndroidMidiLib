package com.reactlibrary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    /**
     * @return A JSON representation of this object.
     */
    public String toJsonString() {
        return EventData.createDefaultGson().toJson(this);
    }

    public static Gson createDefaultGson() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }
}
