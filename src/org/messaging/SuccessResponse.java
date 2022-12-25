package org.messaging;

import org.json.simple.*;  // required for JSON encoding and decoding

public class SuccessResponse extends Response {
    // class name to be used as tag in JSON representation
    private static final String _class =
            SuccessResponse.class.getSimpleName();

    // Constructor; no arguments as there are no instance fields
    public SuccessResponse() {}

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        return obj;
    }

    // Tries to deserialize a SuccessResponse instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static SuccessResponse fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // construct the new object to return
            return new SuccessResponse();
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
