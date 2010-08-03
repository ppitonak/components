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

package org.richfaces.renderkit.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.context.AjaxContext;
import org.ajax4jsf.javascript.JSObject;
import org.ajax4jsf.renderkit.AjaxEventOptions;
import org.ajax4jsf.renderkit.AjaxRendererUtils;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.richfaces.component.AbstractTogglePanel;
import org.richfaces.component.AbstractTogglePanelItem;

/**
 * @author akolonitsky
 * @since -4712-01-01
 */
@ResourceDependencies({
    @ResourceDependency(library = "javax.faces", name = "jsf.js"),
    @ResourceDependency(name = "jquery.js"),
    @ResourceDependency(name = "richfaces.js"),
    @ResourceDependency(name = "richfaces-event.js"),
    @ResourceDependency(name = "richfaces-base-component.js"),
    @ResourceDependency(name = "script/TogglePanel.js") })
public class TogglePanelRenderer extends DivPanelRenderer {

    private static final String VALUE_POSTFIX = "-value";

    @Override
    protected void doDecode(FacesContext context, UIComponent component) {
        Map<String, String> requestMap =
              context.getExternalContext().getRequestParameterMap();

        // Don't overwrite the value unless you have to!
        String newValue = requestMap.get(getSelectedItemRequestParamName(context, component));
        if (newValue != null) {
            setSubmittedValue(component, newValue);

//            if (logger.isLoggable(Level.FINE)) {
//                logger.log(Level.FINE, "new value after decoding {0}", newValue);
//            }
        }

        String compClientId = component.getClientId(context);
        String clientId = requestMap.get(compClientId);
        if (clientId != null && clientId.equals(compClientId)) {
            AbstractTogglePanel panel = (AbstractTogglePanel) component;
            AbstractTogglePanelItem panelItem = panel.getItem(newValue);
            if (panelItem != null) {
                context.getPartialViewContext().getRenderIds().add(panelItem.getClientId(context));
                
                //TODO nick - this should be done on encode, not on decode
                addOnCompleteParam(newValue, panel);
            }
        }
    }

    private void addOnCompleteParam(String newValue, AbstractTogglePanel panel) {
        StringBuilder onComplete = new StringBuilder();
        onComplete.append("RichFaces.$('").append(panel.getClientId()).append("').onCompleteHandler('")
                  .append(panel.getSelectedItem()).append("','").append(newValue).append("');");

        AjaxContext.getCurrentInstance().appendOncomplete(onComplete.toString());
    }

    private static String getSelectedItemRequestParamName(FacesContext context, UIComponent component) {
        return component.getClientId(context) + VALUE_POSTFIX;
    }

//    @Override
    public void setSubmittedValue(UIComponent component, Object value) {
        if (component instanceof AbstractTogglePanel) {
            ((AbstractTogglePanel) component).setSubmittedSelectedItem((String) value);

//            if (logger.isLoggable(Level.FINE)) {
//                logger.fine("Set submitted value " + value + " on component ");
//            }
        }

    }


//    @Override
//    public boolean getRendersChildren() {
//        return true;
//    }

    @Override
    protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent comp) throws IOException {
        super.doEncodeBegin(writer, context, comp);
        AbstractTogglePanel panel = (AbstractTogglePanel) comp;

        writer.startElement(HTML.INPUT_ELEM, comp);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
        writer.writeAttribute(HTML.VALUE_ATTRIBUTE, panel.getSelectedItem(), "selectedItem");
        writer.writeAttribute(HTML.ID_ATTRIBUTE, getSelectedItemRequestParamName(context, comp), null);
        writer.writeAttribute(HTML.NAME_ATTRIBUTE, getSelectedItemRequestParamName(context, comp), null);
        writer.endElement(HTML.INPUT_ELEM);
    }

    @Override
    protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        if (component.getChildCount() <= 0) {
            return;
        }

        for (UIComponent child : component.getChildren()) {
            if (!(child instanceof AbstractTogglePanelItem)) {
                throw new IllegalStateException("Child of TogglePanel can be only TogglePanelItem");
            }

            doEncodeChild(context, (AbstractTogglePanel) component, (AbstractTogglePanelItem) child);
        }
    }

    private void doEncodeChild(FacesContext facesContext, AbstractTogglePanel panel, AbstractTogglePanelItem item)
        throws IOException {

        boolean isSelected = panel.getSelectedItem().equals(item.getName());
        if (isSelected) {
            item.encodeAll(facesContext);
            
        } else {
            
            switch (item.getSwitchType()) {
                //TODO nick - non-rendered items shouldn't be processed
                case client:
                    hidePanelItem(item);

                    item.encodeAll(facesContext);
                    break;

                case ajax:
                    if (!item.isRendered()) {
                        break;
                    }

                    hidePanelItem(item);
                    item.encodeBegin(facesContext);
                    item.encodeEnd(facesContext);
                    break;

                case server:
                    //TODO nick - why nothing?
                    break;

                default:
                    throw new IllegalStateException("Unknown switch type : " + item.getSwitchType());
            }
        }


    }

    private static void hidePanelItem(UIComponent item) {
        //TODO nick - attributes shouldn't be overwritten
        item.getAttributes().put(HTML.STYLE_ATTRIBUTE, "display:none");
    }

    @Override
    protected JSObject getScriptObject(FacesContext context, UIComponent component) {
        return new JSObject("RichFaces.ui.TogglePanel", component.getClientId(), getScriptObjectOptions(context, component));
    }

    @Override
    protected Map<String, Object> getScriptObjectOptions(FacesContext context, UIComponent component) {
        AbstractTogglePanel panel = (AbstractTogglePanel) component;

        Map<String, Object> options = new HashMap<String, Object>(3);
        options.put("selectedItem", panel.getValue());
        options.put("switchMode", panel.getSwitchType());
        options.put("items", getChildrenScriptObjects(context, panel));

        options.put("ajax", getAjaxOptions(context, panel));

        return options;
    }

    private static AjaxEventOptions getAjaxOptions(FacesContext context, UIComponent panel) {
        return AjaxRendererUtils.buildEventOptions(context, panel);
    }

    private List<JSObject> getChildrenScriptObjects(FacesContext context, UIComponent component) {
        List<JSObject> res = new ArrayList<JSObject>(component.getChildCount());
        for (UIComponent child : component.getChildren()) {
            res.add(getChildScriptObject(context, (AbstractTogglePanelItem) child));
        }
        return res;
    }

    private JSObject getChildScriptObject(FacesContext context, AbstractTogglePanelItem child) {
        return ((TogglePanelItemRenderer) child.getRenderer(context))
                            .getScriptObject(context, child);
    }

    @Override
    protected Class<? extends UIComponent> getComponentClass() {
        return AbstractTogglePanel.class;
    }
}

