package org.richfaces.component.html;

import org.richfaces.component.UIDataTable;

public class HtmlDataTable extends UIDataTable {

    public static final String COMPONENT_TYPE = "org.richfaces.DataTable";

    public HtmlDataTable() {
        setRendererType("org.richfaces.DataTableRenderer");
    }
}
