package org.messaging;

import org.json.simple.*;  // required for JSON encoding and decoding

public class ReadRequest extends Request {
    // class name to be used as tag in JSON representation
    private static final String _class =
            ReadRequest.class.getSimpleName();

    // Constructor; no arguments as there are no instance fields
    public ReadRequest() {}

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        return obj;
    }

    // Tries to deserialize a ReadRequest instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static ReadRequest fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // construct the new object to return
            return new ReadRequest();
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
