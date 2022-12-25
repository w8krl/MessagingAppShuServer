package org.messaging;
import org.json.simple.*;

public class LoginRequest extends Request {
    private static final String _class =
            LoginRequest.class.getSimpleName();

    private String name;

    // Constructor; throws NullPointerException if name is null.
    public LoginRequest(String name) {
        // check for null
        if (name == null)
            throw new NullPointerException();
        this.name = name;
    }

    String getName() { return name; }

    // Serializes this object into a JSONObject
    @SuppressWarnings("unchecked")
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", _class);
        obj.put("name", name);
        return obj;
    }

    // Tries to deserialize a LoginRequest instance from a JSONObject.
    // Returns null if deserialization was not successful (e.g. because a
    // different object was serialized).
    public static LoginRequest fromJSON(Object val) {
        try {
            JSONObject obj = (JSONObject)val;
            // check for _class field matching class name
            if (!_class.equals(obj.get("_class")))
                return null;
            // deserialize login name
            String name = (String)obj.get("name");
            // construct the object to return (checking for nulls)
            return new LoginRequest(name);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
