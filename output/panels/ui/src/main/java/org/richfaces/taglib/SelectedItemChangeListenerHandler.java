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

package org.richfaces.taglib;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.view.AttachedObjectHandler;
import javax.faces.view.EditableValueHolderAttachedObjectHandler;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.richfaces.component.AbstractTogglePanel;
import org.richfaces.event.SelectedItemChangeEvent;
import org.richfaces.event.SelectedItemChangeListener;

//TODO nick - what does "-4712-01-01" mean?
/**
 *
 * @author akolonitsky
 * @version 1.0
 * @since -4712-01-01
 */
public final class SelectedItemChangeListenerHandler extends TagHandler implements EditableValueHolderAttachedObjectHandler {

    //TODO nick - take names from primitives
    private static final String[] PRIMITIVE_NAMES = new String[] {"boolean",
        "byte", "char", "double", "float", "int", "long", "short", "void" };

    private static final Class[] PRIMITIVES = new Class[] {boolean.class,
        byte.class, char.class, double.class, float.class, int.class, long.class, short.class, Void.TYPE };

    private static class LazySelectedItemChangeListener implements SelectedItemChangeListener, Serializable {

        private static final long serialVersionUID = 1L;

        private final String type;

        private final ValueExpression binding;

        LazySelectedItemChangeListener(String type, ValueExpression binding) {
            this.type = type;
            this.binding = binding;
        }

        public void processSelectedItemChange(SelectedItemChangeEvent event)
            throws AbortProcessingException {
            
            FacesContext faces = FacesContext.getCurrentInstance();
            if (faces == null) {
                return;
            }

            SelectedItemChangeListener instance = null;
            if (this.binding != null) {
                instance = (SelectedItemChangeListener) binding.getValue(faces.getELContext());
            }
            if (instance == null && this.type != null) {
                try {
                    instance = (SelectedItemChangeListener) forName(this.type).newInstance();
                } catch (Exception e) {
                    throw new AbortProcessingException("Couldn't Lazily instantiate SelectedItemChangeListener", e);
                }
                if (this.binding != null) {
                    binding.setValue(faces.getELContext(), instance);
                }
            }
            if (instance != null) {
                instance.processSelectedItemChange(event);
            }
        }
    }

    private final TagAttribute binding;

    private final String listenerType;

    public SelectedItemChangeListenerHandler(TagConfig config) {
        super(config);
        this.binding = this.getAttribute("binding");
        TagAttribute type = this.getAttribute("type");
        if (type != null) {
            if (type.isLiteral()) {
                try {
                    forName(type.getValue());
                } catch (ClassNotFoundException e) {
                    throw new TagAttributeException(type, "Couldn't qualify SelectedItemChangeListener", e);
                }
            } else {
                throw new TagAttributeException(type, "Must be a literal class name of type SelectedItemChangeListener");
            }
            this.listenerType = type.getValue();
        } else {
            this.listenerType = null;
        }
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        // only process if it's been created
        if (parent == null || !ComponentHandler.isNew(parent)) {
            return;
        }

        if (parent instanceof AbstractTogglePanel) {
            applyAttachedObject(ctx.getFacesContext(), parent);
        } else if (parent.getAttributes().containsKey(Resource.COMPONENT_RESOURCE_KEY)) {
            //TODO nick - javax.faces.component.UIComponent.isCompositeComponent(UIComponent)
            
            // Allow the composite component to know about the target component.
            getAttachedObjectHandlers(parent).add(this);
        } else {
            throw new TagException(this.tag, "Parent is not of type EditableValueHolder, type is: " + parent);
        }
    }

    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        ValueExpression b = null;
        if (this.binding != null) {
            FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
            //TODO nick - b is already instance of listener - add it directly without LazySelectedItemChangeListener
            b = this.binding.getValueExpression(ctx, SelectedItemChangeListener.class);
        }

        AbstractTogglePanel evh = (AbstractTogglePanel) parent;
        //TODO nick - this should be done via API interface
        evh.addSelectedItemChangeListener(new LazySelectedItemChangeListener(this.listenerType, b));
    }

    public String getFor() {
        TagAttribute attr = this.getAttribute("for");
        return attr == null ? null : attr.getValue();
    }

    // TODO Move to utility class
    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component) {
        return getAttachedObjectHandlers(component, true);
    }

    //TODO nick - see org.richfaces.view.facelets.html.TagHandlerUtils
    @SuppressWarnings({"unchecked"})
    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component,
                                                                        boolean create) {
        Map<String, Object> attrs = component.getAttributes();
        List<AttachedObjectHandler> result = (List<AttachedObjectHandler>)
              attrs.get("javax.faces.RetargetableHandlers");

        if (result == null) {
            if (create) {
                result = new ArrayList<AttachedObjectHandler>();
                attrs.put("javax.faces.RetargetableHandlers", result);
            } else {
                result = Collections.EMPTY_LIST;
            }
        }
        return result;

    }

    public static Class forName(String name) throws ClassNotFoundException {
        if (null == name || "".equals(name)) {
            return null;
        }
        
        //TODO nick - primitive class won't be necessary listener
        Class c = forNamePrimitive(name);
        if (c == null) {
            //TODO nick - standard syntax for array classes is different + what about arrays of > 1 dims?
            if (name.endsWith("[]")) {
                String nc = name.substring(0, name.length() - 2);
                c = Class.forName(nc, false, Thread.currentThread().getContextClassLoader());
                c = Array.newInstance(c, 0).getClass();
            } else {
                c = Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            }
        }
        return c;
    }

    protected static Class forNamePrimitive(String name) {
        if (name.length() <= 8) {
            int p = Arrays.binarySearch(PRIMITIVE_NAMES, name);
            if (p >= 0) {
                return PRIMITIVES[p];
            }
        }
        return null;
    }
}

