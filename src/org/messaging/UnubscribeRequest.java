package org.messaging;


import org.json.simple.JSONObject;

public class UnubscribeRequest extends Request {
    // class name to be used as tag in JSON representation
    private static final String _class =
            UnubscribeRequest.class.getSimpleName();

    private String identity;

    // Constructor; throws NullPointerException if message is null.
    public UnubscribeRequest(String identity) {
        // check for null
        if (identity == null)
            throw new NullPointerException();
        this.identity = identity;
    }

    String getChannel() { return identity; }

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        obj.put("identity", identity);
        return obj;
    }

    // Tries to deserialize a OpenRequest instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static UnubscribeRequest fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize posted message
            String identity = (String)obj.get("identity");
            // construct the object to return (checking for nulls)
            return new UnubscribeRequest(identity);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
