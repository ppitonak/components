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


package org.richfaces.renderkit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.javascript.JSFunction;
import org.ajax4jsf.renderkit.AjaxRendererUtils;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.richfaces.component.Row;
import org.richfaces.component.UIDataTableBase;
import org.richfaces.component.UISubTable;
import org.richfaces.event.ToggleEvent;

/**
 * @author Anton Belevich
 *
 */
public class SubTableRenderer extends AbstractTableRenderer {
    
    private static final String STATE = ":state"; 
    
    private static final String OPTIONS = ":options";
    

    protected void doDecode(FacesContext facesContext, UIComponent component) {
        UISubTable subTable = (UISubTable)component;
        
        String clientId = subTable.getClientId(facesContext);
        
        String stateId = clientId + STATE;
        Map<String, String> requestMap = facesContext.getExternalContext().getRequestParameterMap();
        
        String state = (String)requestMap.get(stateId);
        
        boolean isExpand = true; 
        if(state != null) {
            int newValue = Integer.parseInt(state);
            
            if(newValue < 1) {
                isExpand = false;
            } 
            
            if(subTable.isExpanded() != isExpand) {
                new ToggleEvent(subTable, isExpand).queue();
            }
        }
    }
    
    public void encodeTableFacets(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable) throws IOException {
        UISubTable subTable = (UISubTable)dataTable;
        
        encodeHeaderFacet(writer, context, subTable, false);
        
        String rowClass =  getRowSkinClass();
        String cellClass = getCellSkinClass();
        String firstClass = getFirstRowSkinClass();
        
        rowClass = mergeStyleClasses("rowClass", rowClass, subTable);
        cellClass = mergeStyleClasses("cellClass", cellClass, subTable);
        firstClass = mergeStyleClasses("firstRowClass", firstClass, subTable);
        
        saveRowStyles(context, subTable.getClientId(context), firstClass, rowClass, cellClass);
    }
    
    public void encodeTableBodyStart(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable)
        throws IOException {
        
        UISubTable subTable = (UISubTable) dataTable;
        writer.startElement(HTML.TBODY_ELEMENT, subTable);
        getUtils().encodeId(context, subTable);

        String predefinedStyles = !subTable.isExpanded() ?  "display: none;" : null;
        writer.writeAttribute(HTML.CLASS_ATTRIBUTE, getTableSkinClass(), null);
        encodeStyle(writer, context, dataTable, predefinedStyles);
    }
    
    public RowHolderBase createRowHolder(FacesContext context, UIComponent component) {
        return  new RowHolder(context, (UISubTable)component);
    }
       
    @Override
    public void encodeBeforeRows(ResponseWriter writer, FacesContext facesContext, RowHolderBase holder)
        throws IOException {
        RowHolder rowHolder = (RowHolder)holder;
        Row row = rowHolder.getRow();
        if(rowHolder.isPartialUpdate()) {
            facesContext.getPartialViewContext().getPartialResponseWriter().startUpdate(row.getClientId(facesContext)+ ":tb");
        }
        encodeTableBodyStart(writer, facesContext, (UISubTable)row);
        encodeHeaderFacet(writer, facesContext, (UISubTable)row, false);
    }
    
    public void encodeRow(ResponseWriter writer, FacesContext context, RowHolderBase holder) throws IOException {
        RowHolder rowHolder = (RowHolder)holder;
        Row row = rowHolder.getRow();
        
        rowHolder.setRowStart(true);
        Iterator<UIComponent> components = row.columns();
        while(components.hasNext()) {
            encodeColumn(context, writer, (UIColumn)components.next(), rowHolder);
        }
        encodeRowEnd(writer);
    }
    
    public void encodeAfterRows(ResponseWriter writer, FacesContext facesContext, RowHolderBase holder) throws IOException {
        RowHolder rowHolder = (RowHolder)holder;
        Row row = rowHolder.getRow();
        
        UISubTable subTable = (UISubTable)row;
        encodeFooterFacet(writer, facesContext, subTable, false);
        encodeTableBodyEnd(writer);
        encodeHiddenContainer(writer, facesContext, subTable);
        if(rowHolder.isPartialUpdate()) {
            facesContext.getPartialViewContext().getPartialResponseWriter().endUpdate();
        }    
    }
    
    @Override
    public boolean encodeParentTBody(UIDataTableBase dataTableBase) {
        return true;
    }
    
    public void encodeHiddenInput(ResponseWriter writer, FacesContext facesContext, UIDataTableBase dataTableBase) throws IOException {
        UISubTable subTable = (UISubTable)dataTableBase;
        
        String stateId = subTable.getClientId(facesContext) + STATE;

        writer.startElement(HTML.INPUT_ELEM, subTable);
        writer.writeAttribute(HTML.ID_ATTRIBUTE, stateId , null);
        writer.writeAttribute(HTML.NAME_ATTRIBUTE, stateId , null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
        int state = subTable.isExpanded() ? 1 : 0;
        writer.writeAttribute(HTML.VALUE_ATTRIBUTE, state, null);
        writer.endElement(HTML.INPUT_ELEM);
        
        String optionsId = subTable.getClientId(facesContext) + OPTIONS;
        writer.startElement(HTML.INPUT_ELEM, subTable);
        writer.writeAttribute(HTML.ID_ATTRIBUTE, optionsId , null);
        writer.writeAttribute(HTML.NAME_ATTRIBUTE, optionsId , null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
        writer.endElement(HTML.INPUT_ELEM);
        
    }
    
    public boolean containsThead() {
        return false;
    }

    public HeaderEncodeStrategy getHeaderEncodeStrategy(UIComponent column, String tableFacetName) {
        //TODO: anton -> use RichHeaderEncodeStrategy for our columns ???   
        return new SimpleHeaderEncodeStrategy();
        
    }

    public void encodeClientScript(ResponseWriter writer, FacesContext facesContext, UIDataTableBase component) throws IOException {
        UISubTable subTable = (UISubTable)component;
        String id = subTable.getClientId(facesContext);
        
        UIComponent nestingForm = getUtils().getNestingForm(facesContext, subTable);
        String formId = nestingForm != null ? nestingForm.getClientId(facesContext) : "";
         
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("stateInput", subTable.getClientId(facesContext) +STATE);
        options.put("optionsInput", subTable.getClientId(facesContext) +OPTIONS);
        options.put("expandMode", subTable.getExpandMode());
        options.put("eventOptions", AjaxRendererUtils.buildEventOptions(facesContext, subTable));
        
        JSFunction jsFunction = new JSFunction("new RichFaces.ui.SubTable");
        jsFunction.addParameter(id);
        jsFunction.addParameter(formId);
        jsFunction.addParameter(options);

        writer.startElement(HTML.SCRIPT_ELEM, subTable);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript",null);
        writer.writeText(jsFunction.toScript(), null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }
    
    public String getTableSkinClass() {
        return "rf-st";
    }
        
    public String getRowSkinClass() {
        return "rf-st-r";
    }

    public String getFirstRowSkinClass() {
        return "rf-st-f-r";
    }

    public String getHeaderRowSkinClass() {
        return "rf-st-h-r";
    }

    public String getHeaderFirstRowSkinClass() {
        return "rf-st-h-f-r";
    }

    public String getCellSkinClass() {
        return "rf-st-c";
    }
   
    public String getHeaderCellSkinClass() {
        return "rf-st-h-c";
    }


    public String getColumnHeaderCellSkinClass() {
        return "rf-st-sh-c";
    }

    public String getColumnHeaderSkinClass() {
        return "rf-st-sh";
    }

    public String getFooterSkinClass() {
        return "rf-st-f";
    }
    
    public String getFooterCellSkinClass() {
        return "rf-st-f-c";
    }

    public String getFooterFirstRowSkinClass() {
        return "rf-st-f-f";
    }

    public String getColumnFooterCellSkinClass() {
        return "rf-st-sf-c";
    }

    public String getColumnFooterSkinClass() {
        return "rf-st-sf";
    }
    
    public String getColumnFooterFirstSkinClass() {
        return "rf-st-sf-f";    
    }

    public String getColumnHeaderFirstSkinClass() {
        return "rf-st-sh-f";
    }

    public String getFooterFirstSkinClass() {
        return "rf-st-f-f";
    }

    public String getHeaderFirstSkinClass() {
        return "rf-st-h-f";
    }

    public String getHeaderSkinClass() {
        return "rf-st-h";
    }
    
    public String getNoDataClass() {
        return "rf-st-nd-c";
    }

    protected void setupTableStartElement(FacesContext context, UIComponent component) {
        put(context, component.getClientId(context), CELL_ELEMENT_KEY, HTML.TD_ELEM);
    }
}
