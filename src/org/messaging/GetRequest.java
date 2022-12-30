package org.messaging;


import org.json.simple.JSONObject;

public class GetRequest extends Request {
    // class name to be used as tag in JSON representation
    private static final String _class =
            GetRequest.class.getSimpleName();

    private String identity;
    private final long timestamp;


    // Constructor; throws NullPointerException if message is null.
    public GetRequest(String identity, long timestamp) {
        // check for null
        if (identity == null)
            throw new NullPointerException();
        this.identity = identity;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    String getChannel() {
        return identity;
    }

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        obj.put("identity", identity);
        obj.put("timestamp", timestamp);
        return obj;
    }

    // Tries to deserialize a OpenRequest instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static GetRequest fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject) val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize posted message
            String identity = (String) obj.get("identity");
            long timestamp = (long) obj.get("timestamp");
            // construct the object to return (checking for nulls)
            return new GetRequest(identity, timestamp);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
