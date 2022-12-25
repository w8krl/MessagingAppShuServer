package org.messaging;

import org.json.simple.*;  // required for JSON encoding and decoding

public class Message {
    // class name to be used as tag in JSON representation
    private static final String _class =
            Message.class.getSimpleName();

    private final String body;
    private final String author;
    private final String channel;
    private final long   timestamp;

    // Constructor; throws NullPointerException if arguments are null
    public Message(String body, String author, String channel, long timestamp) {
        if (body == null || author == null)
            throw new NullPointerException();
        this.body      = body;
        this.author    = author;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    public String getBody()      { return body; }
    public String getAuthor()    { return author; }
    public String getChannel()    { return channel; }
    public long   getTimestamp() { return timestamp; }

    public String toString() {
        return author + ": " + body + " (" + timestamp + ")";
    }

    //////////////////////////////////////////////////////////////////////////
    // JSON representation

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class",    _class);
        obj.put("body",      body);
        obj.put("author",    author);
        obj.put("channel",    channel);
        obj.put("timestamp", timestamp);
        return obj;
    }

    // Tries to deserialize a Message instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static Message fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize message fields (checking timestamp for null)
            String body      = (String)obj.get("body");
            String author    = (String)obj.get("author");
            String channel    = (String)obj.get("channel");
            long   timestamp = (long)obj.get("timestamp");
            // construct the object to return (checking for nulls)
            return new Message(body, author, channel, timestamp);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
