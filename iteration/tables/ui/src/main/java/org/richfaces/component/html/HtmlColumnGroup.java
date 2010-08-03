package org.richfaces.component.html;

import org.richfaces.component.UIColumnGroup;

public class HtmlColumnGroup extends UIColumnGroup {
    
    public static final String COMPONENT_TYPE = "org.richfaces.ColumnGroup";

    public static final String COMPONENT_FAMILY = "org.richfaces.Column";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

}
