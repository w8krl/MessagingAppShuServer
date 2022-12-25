package org.messaging;

import org.json.simple.*;  // required for JSON encoding and decoding

public class ErrorResponse extends Response {
    // class name to be used as tag in JSON representation
    private static final String _class =
            ErrorResponse.class.getSimpleName();

    private String error;

    // Constructor; throws NullPointerException if error is null.
    public ErrorResponse(String error) {
        // check for null
        if (error == null)
            throw new NullPointerException();
        this.error = error;
    }

    public String getError() { return error; }

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        obj.put("error", error);
        return obj;
    }

    // Tries to deserialize a ErrorResponse instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static ErrorResponse fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize error message
            String error = (String)obj.get("error");
            // construct the object to return (checking for nulls)
            return new ErrorResponse(error);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}

