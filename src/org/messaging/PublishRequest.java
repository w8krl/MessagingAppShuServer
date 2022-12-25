package org.messaging;

import org.json.simple.JSONObject;

public class PublishRequest extends Request{
    // class name to be used as tag in JSON representation
    private static final String _class =
            PublishRequest.class.getSimpleName();

    private String message;

    // Constructor; throws NullPointerException if message is null.
    public PublishRequest(String message) {
        // check for null
        if (message == null)
            throw new NullPointerException();
        this.message = message;
    }

    String getMessage() { return message; }

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        obj.put("message", message);
        return obj;
    }

    // Tries to deserialize a PublishRequest instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static PublishRequest fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize posted message
            String message = (String)obj.get("message");
            // construct the object to return (checking for nulls)
            return new PublishRequest(message);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
