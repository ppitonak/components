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

package org.richfaces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

/**
 * @author akolonitsky
 * @version 1.0
 * @since -4712-01-01
 */
public abstract class AbstractTogglePanelItem extends AbstractDivPanel {

    public static final String COMPONENT_TYPE = "org.richfaces.panels.TogglePanelItem";

    public static final String COMPONENT_FAMILY = "org.richfaces.panels.TogglePanelItem";

    protected AbstractTogglePanelItem() {
        setRendererType("org.richfaces.panels.TogglePanelItemRenderer");
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public void setParent(UIComponent parent) {
        if (parent != null && !(parent instanceof AbstractTogglePanel)) {
            throw new IllegalArgumentException("Parent of TogglePanelItem can be only TogglePanel.");
        }
        super.setParent(parent);
    }

    @Override
    public AbstractTogglePanel getParent() {
        return (AbstractTogglePanel) super.getParent();
    }

    @Override
    public Renderer getRenderer(FacesContext context) {
        return super.getRenderer(context);
    }

    public abstract String getName();

    public abstract Method getSwitchType();

    public String toString() {
        return "TogglePanelItem {name: " + getName() + ", switchType: " + getSwitchType() + '}';
    }
}
