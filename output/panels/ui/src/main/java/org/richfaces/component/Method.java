package org.richfaces.component;

/**
 * @author akolonitsky
 * @since Jun 15, 2010
 */
//TODO nick - rename into SwitchType or PanelSwitchType
//TODO nick - move to API
public enum Method {
    /**
     * value for tab change method for - client-side tabs.
     */
    client,

    /**
     * value for tab change method - server-side tabs
     */
    server,

    /**
     * value for tab change method - ajax tabs
     */
    ajax
    
    //TODO nick - add DEFAULT constant 
}
