package org.messaging;

import org.json.simple.JSONAware;

public abstract class Request implements JSONAware {

    public abstract  Object toJSON();
    public  String toJSONString() {return toJSON().toString();}
    public  String toString(){return toJSONString();}
}
