/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.attribute;

import name.huliqing.luoying.data.AttributeData;

/**
 *
 * @author huliqing
 */
public class BooleanAttribute extends SimpleAttribute<Boolean> {

    @Override
    public void setData(AttributeData data) {
        super.setData(data); 
        value = data.getAsBoolean(ATTR_VALUE, false);
    }

    @Override
    protected boolean doSetSimpleValue(Boolean newValue) {
        if (value.booleanValue() != newValue.booleanValue()) {
            value = newValue;
            return true;
        }
        return false;
    }

}