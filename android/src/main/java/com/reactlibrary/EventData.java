package com.reactlibrary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EventData {
    private String eventType;
    private int channel;
    private int noteByte1;
    private int noteByte2;
    private long timestamp;

    public EventData() {
    }

    public EventData(String eventType, int channel, int noteByte1, int noteByte2, long timestamp) {
        this.eventType = eventType;
        this.channel = channel;
        this.noteByte1 = noteByte1;
        this.noteByte2 = noteByte2;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getNoteByte1() {
        return noteByte1;
    }

    public void setNoteByte1(int noteByte1) {
        this.noteByte1 = noteByte1;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getNoteByte2() {
        return noteByte2;
    }

    public void setNoteByte2(int noteByte2) {
        this.noteByte2 = noteByte2;
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
