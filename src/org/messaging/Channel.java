package org.messaging;

import org.json.simple.*;  // required for JSON encoding and decoding

public class Channel {
    // class name to be used as tag in JSON representation
    private static final String _class =
            Channel.class.getSimpleName();

    private final String name;
    private final String author;
    private final long   timestamp;

    // Constructor; throws NullPointerException if arguments are null
    public Channel(String body, String author, long timestamp) {
        if (body == null || author == null)
            throw new NullPointerException();
        this.name      = body;
        this.author    = author;
        this.timestamp = timestamp;
    }

    public String getBody()      { return name; }
    public String getAuthor()    { return author; }
    public long   getTimestamp() { return timestamp; }

    public String toString() {
        return author + ": " + name + " (" + timestamp + ")";
    }

    //////////////////////////////////////////////////////////////////////////
    // JSON representation

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class",    _class);
        obj.put("name",      name);
        obj.put("author",    author);
        obj.put("timestamp", timestamp);
        return obj;
    }

    // Tries to deserialize a Message instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static Channel fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize message fields (checking timestamp for null)
            String name      = (String)obj.get("name");
            String author    = (String)obj.get("author");
            long   timestamp = (long)obj.get("timestamp");
            // construct the object to return (checking for nulls)
            return new Channel(name, author, timestamp);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
