package org.messaging;


import org.json.simple.*;  // required for JSON encoding and decoding

public class SubscribeRequest extends Request {
    // class name to be used as tag in JSON representation
    private static final String _class =
            SubscribeRequest.class.getSimpleName();

    private String channel;

    // Constructor; throws NullPointerException if message is null.
    public SubscribeRequest(String channel) {
        // check for null
        if (channel == null)
            throw new NullPointerException();
        this.channel = channel;
    }

    String getChannel() { return channel; }

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        obj.put("channel", channel);
        return obj;
    }

    // Tries to deserialize a OpenRequest instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static SubscribeRequest fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize posted message
            String channel = (String)obj.get("channel");
            // construct the object to return (checking for nulls)
            return new SubscribeRequest(channel);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
