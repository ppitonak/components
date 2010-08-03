package org.richfaces.taglib;

import javax.el.MethodExpression;
import javax.faces.context.FacesContext;

import org.richfaces.event.ToggleEvent;
import org.richfaces.event.ToggleListener;

public class MethodExpressionToggleListener implements ToggleListener {
    
    private MethodExpression methodExpression;
    
    public MethodExpressionToggleListener() {
        super();
    }
    
    MethodExpressionToggleListener(MethodExpression methodExpression) {
        super();
        this.methodExpression = methodExpression;
    }
    
    public void processToggle(ToggleEvent toggleEvent) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        methodExpression.invoke(facesContext.getELContext(), new Object[]{toggleEvent});
    }
}
