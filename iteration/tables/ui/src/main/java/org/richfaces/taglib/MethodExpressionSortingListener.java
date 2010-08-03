package org.richfaces.taglib;

import javax.el.MethodExpression;
import javax.faces.context.FacesContext;

import org.richfaces.event.SortingEvent;
import org.richfaces.event.SortingListener;

public class MethodExpressionSortingListener implements SortingListener {

    private MethodExpression methodExpression;
    
    public MethodExpressionSortingListener() {
        super();
    }
    
    MethodExpressionSortingListener(MethodExpression methodExpression) {
        super();
        this.methodExpression = methodExpression;
    }
    
    public void processSorting(SortingEvent sortingEvent) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        methodExpression.invoke(facesContext.getELContext(), new Object[]{sortingEvent});
    }
}
