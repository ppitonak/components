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

import java.util.Iterator;
import java.util.List;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UpdateModelException;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PostValidateEvent;
import javax.faces.event.PreValidateEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.validator.Validator;

import org.richfaces.event.SelectedItemChangeEvent;
import org.richfaces.event.SelectedItemChangeListener;

/**
 * @author akolonitsky
 * @version 1.0
 */
public abstract class AbstractTogglePanel extends AbstractDivPanel implements EditableValueHolder {

    //TODO nick - http://community.jboss.org/docs/DOC-13693
    public static final String COMPONENT_TYPE = "org.richfaces.panels.TogglePanel";

    //TODO nick - http://community.jboss.org/docs/DOC-13693
    public static final String COMPONENT_FAMILY = "org.richfaces.panels.TogglePanel";

    private String submittedSelectedItem = null;

    private enum PropertyKeys {
        localValueSet,
        required,
        valid,
        immediate
    }

    protected AbstractTogglePanel() {
        setRendererType("org.richfaces.panels.TogglePanelRenderer");
    }


    // -------------------------------------------------- Editable Value Holder

    public Object getSubmittedValue() {
        return this.submittedSelectedItem;
    }

    public void resetValue() {
        this.setValue(null);
        this.setSubmittedValue(null);
        this.setLocalValueSet(false);
        this.setValid(true);
    }

    public void setSubmittedValue(Object submittedValue) {
        this.submittedSelectedItem = String.valueOf(submittedValue);
    }

    /**
     * Return the "local value set" state for this component.
     * Calls to <code>setValue()</code> automatically reset
     * this property to <code>true</code>.
     */
    public boolean isLocalValueSet() {
        return (Boolean) getStateHelper().eval(PropertyKeys.localValueSet, false);
    }

    /**
     * Sets the "local value set" state for this component.
     */
    public void setLocalValueSet(boolean localValueSet) {
        getStateHelper().put(PropertyKeys.localValueSet, localValueSet);
    }

    public boolean isValid() {
        return (Boolean) getStateHelper().eval(PropertyKeys.valid, true);
    }


    public void setValid(boolean valid) {
        getStateHelper().put(PropertyKeys.valid, valid);
    }

    /**
     * <p>Return the "required field" state for this component.</p>
     */
    public boolean isRequired() {
        return (Boolean) getStateHelper().eval(PropertyKeys.required, false);
    }

    /**
     * <p>Set the "required field" state for this component.</p>
     *
     * @param required The new "required field" state
     */
    public void setRequired(boolean required) {
        getStateHelper().put(PropertyKeys.required, required);
    }


    public boolean isImmediate() {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, false);
    }


    public void setImmediate(boolean immediate) {
        getStateHelper().put(PropertyKeys.immediate, immediate);
    }

    public MethodBinding getValidator() {
        //TODO nick - Errors shouldn't ne thrown
        throw new UnknownError();
    }

    public void setValidator(MethodBinding validatorBinding) {
        //TODO nick - Errors shouldn't ne thrown
        throw new UnknownError();
    }

    public MethodBinding getValueChangeListener() {
        throw new UnsupportedOperationException();
    }

    public void setValueChangeListener(MethodBinding valueChangeMethod) {
        throw new UnsupportedOperationException();
    }

    public void addValidator(Validator validator) {
        throw new UnsupportedOperationException();
    }

    public Validator[] getValidators() {
        throw new UnsupportedOperationException();
    }

    public void removeValidator(Validator validator) {
        throw new UnsupportedOperationException();
    }

    public void addValueChangeListener(ValueChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    public ValueChangeListener[] getValueChangeListeners() {
        throw new UnsupportedOperationException();
    }

    public void removeValueChangeListener(ValueChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------- UIComponent Methods

    /**
     * <p>Specialized decode behavior on top of that provided by the
     * superclass.  In addition to the standard
     * <code>processDecodes</code> behavior inherited from {@link
     * javax.faces.component.UIComponentBase}, calls <code>validate()</code> if the the
     * <code>immediate</code> property is true; if the component is
     * invalid afterwards or a <code>RuntimeException</code> is thrown,
     * calls {@link FacesContext#renderResponse}.  </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processDecodes(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, null);

        // Process all facets and children of this component
        Iterator<UIComponent> kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (isSelectedItem(kid)) {
                kid.processDecodes(context);
            }
        }

        // Process this component itself
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        } finally {
            popComponentFromEL(context);
        }

        executeValidate(context);
    }

    /**
     * <p>In addition to the standard <code>processValidators</code> behavior
     * inherited from {@link javax.faces.component.UIComponentBase}, calls <code>validate()</code>
     * if the <code>immediate</code> property is false (which is the
     * default);  if the component is invalid afterwards, calls
     * {@link FacesContext#renderResponse}.
     * If a <code>RuntimeException</code> is thrown during
     * validation processing, calls {@link FacesContext#renderResponse}
     * and re-throw the exception.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processValidators(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, null);

        Application app = context.getApplication();
        app.publishEvent(context, PreValidateEvent.class, this);
        // Process all the facets and children of this component
        Iterator<UIComponent> kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (isSelectedItem(kid)) {
                kid.processValidators(context);
            }
        }
        app.publishEvent(context, PostValidateEvent.class, this);
        popComponentFromEL(context);
    }

    /**
     * <p>In addition to the standard <code>processUpdates</code> behavior
     * inherited from {@link javax.faces.component.UIComponentBase}, calls
     * <code>updateModel()</code>.
     * If the component is invalid afterwards, calls
     * {@link FacesContext#renderResponse}.
     * If a <code>RuntimeException</code> is thrown during
     * update processing, calls {@link FacesContext#renderResponse}
     * and re-throw the exception.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processUpdates(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, null);

        // Process all facets and children of this component
        Iterator<UIComponent> kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (isSelectedItem(kid)) {
                kid.processUpdates(context);
            }
        }
        
        popComponentFromEL(context);
        
        try {
            updateModel(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            context.renderResponse();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void decode(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Force validity back to "true"
        setValid(true);
        super.decode(context);
    }

    public void updateModel(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (!isValid() || !isLocalValueSet()) {
            return;
        }

        //TODO nick - selectedItem attribute?
        ValueExpression ve = getValueExpression("value");
        if (ve == null) {
            return;
        }
        
        Throwable caught = null;
        FacesMessage message = null;
        try {
            ve.setValue(context.getELContext(), getLocalValue());
            setValue(null);
            setLocalValueSet(false);
        } catch (ELException e) {
            caught = e;
            String messageStr = e.getMessage();
            Throwable result = e.getCause();
            while (null != result && result.getClass().isAssignableFrom(ELException.class)) {
                messageStr = result.getMessage();
                result = result.getCause();
            }

            if (messageStr == null) {
                // todo
                //message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID,
                //              MessageFactory.getLabel(context, this));
            } else {
                //message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageStr, messageStr);
            }
            setValid(false);
        } catch (Exception e) {
            caught = e;
            //message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID,
            //              MessageFactory.getLabel(context, this));
            setValid(false);
        }

        if (caught != null) {
            assert message != null;

            @SuppressWarnings({"ThrowableInstanceNeverThrown"})
            UpdateModelException toQueue = new UpdateModelException(message, caught);
            ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext(context,
                    toQueue, this, PhaseId.UPDATE_MODEL_VALUES);
            context.getApplication().publishEvent(context, ExceptionQueuedEvent.class, eventContext);
        }
    }

    /**
     * Executes validation logic.
     */
    private void executeValidate(FacesContext context) {
        try {
            validate(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            context.validationFailed();
            context.renderResponse();
        }
    }

    public void validate(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Submitted value == null means "the component was not submitted at all".
        String submittedValue = getSubmittedSelectedItem();
        if (submittedValue == null) {
            return;
        }

        String previous = (String) getValue();
        setValue(submittedValue);
        setSubmittedSelectedItem(null);
        if (!previous.equalsIgnoreCase(submittedValue)) {
            queueEvent(new SelectedItemChangeEvent(this, previous, submittedValue));
        }
    }

    public void queueEvent(FacesEvent event) {
        if ((event instanceof SelectedItemChangeEvent) && (event.getComponent() == this)) {
            if (isImmediate()) {
                event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
            } else if (isBypassUpdates()) {
                event.setPhaseId(PhaseId.PROCESS_VALIDATIONS);
            } else {
                event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            }
        }

        super.queueEvent(event);
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        super.broadcast(event);

        //TODO nick - immediate?
        if (event instanceof SelectedItemChangeEvent && isBypassUpdates()) {
            FacesContext.getCurrentInstance().renderResponse();
        }
    }

    // -------------------------------------------------- Panel Items Managing
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    private boolean isSelectedItem(UIComponent kid) {
        //TODO nick - selectedItem should be cached in local variable
        String value = getSelectedItem();
        if (value == null) {
            value = getSubmittedSelectedItem();
        }
        return getChildName(kid).equals(value);
    }

    public String getFirstItem() {
        checkChildCount(getChildCount());

        //TODO nick - children can be rendered or not
        return getChildName(getChildren().get(0));
    }

    private static void checkChildCount(int childCount) {
        //TODO nick - remove this check
        if (childCount < 1) {
            throw new IllegalStateException("TogglePanel must have at least one TogglePanelItem.");
        }
    }

    public AbstractTogglePanelItem getItem(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required parameter.");
        }
        checkChildCount(getChildCount());

        List<UIComponent> children = getChildren();
        //TODO nick - return child itself
        int index = getChildIndex(name, children);
        if (index == -1) {
            return null;
        }
        return (AbstractTogglePanelItem) children.get(index);
    }

    public String getNext(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required parameter.");
        }
        checkChildCount(getChildCount());


        List<UIComponent> children = getChildren();

        int nextItem = getChildIndex(name, children) + 1;
        if (nextItem < children.size()) {
            return getFirstItem();
        } else {
            return getChildName(children.get(nextItem));
        }
    }

    private static int getChildIndex(String name, List<UIComponent> children) {
        int ind = 0;
        while (ind < children.size()) {
            UIComponent child = children.get(ind);
            if (name.equals(getChildName(child))) {
                return ind;
            }
            ind++;
        }
        
//        throw new IllegalStateException("Can't find child panel item with name: " + name);
        return -1;
    }

    private static String getChildName(UIComponent item) {
        //TODO nick - panel can include UIParam children - remove this exception
        if (!(item instanceof AbstractTogglePanelItem)) {
            throw new IllegalStateException("TogglePanel can contain only TogglePanelItem as child.");
        }

        return ((AbstractTogglePanelItem) item).getName();
    }

    // ------------------------------------------------

    public String getSubmittedSelectedItem() {
        return submittedSelectedItem;
    }

    public void setSubmittedSelectedItem(String submittedSelectedItem) {
        this.submittedSelectedItem = submittedSelectedItem;
    }


    // ------------------------------------------------ Properties

    public String getSelectedItem() {
        return (String) getValue();
    }

    public void setSelectedItem(String value) {
        setValue(value);
    }

    public abstract Method getSwitchType();

    public abstract boolean isBypassUpdates();

    public abstract boolean isLimitToList();

    public abstract Object getData();

    public abstract String getStatus();

    public abstract Object getExecute();

    public abstract Object getRender();

    public abstract MethodExpression getSelectedItemChangeListener();



    // ------------------------------------------------ Event Processing Methods

    /**
     * <p>Add a new {@link SelectedItemChangeListener} to the set of listeners
     * interested in being notified when {@link org.richfaces.event.SelectedItemChangeEvent}s occur.</p>
     *
     * @param listener The {@link SelectedItemChangeListener} to be added
     * @throws NullPointerException if <code>listener</code>
     *                              is <code>null</code>
     */
    public void addSelectedItemChangeListener(SelectedItemChangeListener listener) {
        addFacesListener(listener);
    }

    /**
     * <p>Return the set of registered {@link SelectedItemChangeListener}s for this instance.
     * If there are no registered listeners, a zero-length array is returned.</p>
     */
    public SelectedItemChangeListener[] getSelectedItemChangeListeners() {
        return (SelectedItemChangeListener[]) getFacesListeners(SelectedItemChangeListener.class);
    }

    /**
     * <p>Remove an existing {@link SelectedItemChangeListener} (if any) from the
     * set of listeners interested in being notified when
     * {@link org.richfaces.event.SelectedItemChangeEvent}s occur.</p>
     *
     * @param listener The {@link SelectedItemChangeListener} to be removed
     * @throws NullPointerException if <code>listener</code>
     *                              is <code>null</code>
     */
    public void removeSelectedItemChangeListener(SelectedItemChangeListener listener) {
        removeFacesListener(listener);
    }

}
