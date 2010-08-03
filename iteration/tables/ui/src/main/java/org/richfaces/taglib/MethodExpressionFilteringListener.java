package org.richfaces.taglib;

import javax.el.MethodExpression;
import javax.faces.context.FacesContext;

import org.richfaces.event.FilteringEvent;
import org.richfaces.event.FilteringListener;

public class MethodExpressionFilteringListener implements FilteringListener {

    private MethodExpression methodExpression;
    
    public MethodExpressionFilteringListener() {
        super();
    }
    
    MethodExpressionFilteringListener(MethodExpression methodExpression) {
        super();
        this.methodExpression = methodExpression;
    }
    
    public void processFiltering(FilteringEvent filteringEvent) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        methodExpression.invoke(facesContext.getELContext(), new Object[]{filteringEvent});
    }
}
