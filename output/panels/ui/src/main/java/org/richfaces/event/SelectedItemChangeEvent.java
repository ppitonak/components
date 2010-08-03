/*
 * JBoss, Home of Professional Open Source
 * Copyright ${year}, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.richfaces.event;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;


/**
 * <p>A {@link SelectedItemChangeEvent} is a ...</p>
 *
 * @author akolonitsky
 * @version 1.0
 * @since -4712-01-01
 */
//TODO nick - move to API
//TODO nick - serialVersionUUID is missing
public class SelectedItemChangeEvent extends FacesEvent {

    private final String oldItem;
    private final String newItem; 

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a new event object from the specified source component,
     * old value, and new value.</p>
     *
     * <p>The default {@link javax.faces.event.PhaseId} for this event is {@link
     * javax.faces.event.PhaseId#ANY_PHASE}.</p>
     *
     * @param component Source {@link UIComponent} for this event
     *
     * @param oldItem
     * @param newItem
     * 
     * @throws IllegalArgumentException if <code>component</code> is
     *  <code>null</code>
     */
    public SelectedItemChangeEvent(UIComponent component, String oldItem, String newItem) {
        super(component);
        this.oldItem = oldItem;
        this.newItem = newItem;
    }


    // -------------------------------------------------------------- Properties

    public String getOldItem() {
        return oldItem;
    }

    public String getNewItem() {
        return newItem;
    }

    // ------------------------------------------------- Event Broadcast Methods


    public boolean isAppropriateListener(FacesListener listener) {
        return listener instanceof SelectedItemChangeListener;
    }

    /**
     * @throws javax.faces.event.AbortProcessingException {@inheritDoc}
     */
    public void processListener(FacesListener listener) {
        ((SelectedItemChangeListener) listener).processSelectedItemChange(this);
    }
}

