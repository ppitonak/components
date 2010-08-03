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
import java.util.Iterator;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.richfaces.component.Row;
import org.richfaces.component.UIDataTableBase;

/**
 * @author Anton Belevich
 *
 */
@ResourceDependencies(value = { @ResourceDependency(library = "javax.faces", name = "jsf.js"),
        @ResourceDependency(name = "jquery.js"), @ResourceDependency(name = "richfaces.js"), @ResourceDependency(name = "richfaces-event.js")})
public abstract class AbstractTableRenderer extends AbstractTableBaseRenderer implements MetaComponentRenderer {
    
    protected void doDecode(FacesContext context, UIComponent component) {
        decodeSortingFiltering(context, component);
    }
    
    protected class SimpleHeaderEncodeStrategy implements HeaderEncodeStrategy {

        public void encodeBegin(FacesContext context, ResponseWriter writer, UIComponent column, String facetName)
            throws IOException {

        }

        public void encodeEnd(FacesContext context, ResponseWriter writer, UIComponent column, String facetName)
            throws IOException {
        }
    }

    @Override
    protected void encodeRows(FacesContext facesContext, RowHolderBase rowHolder) {
        UIDataTableBase dataTableBase = (UIDataTableBase)rowHolder.getRow();

        String rowClass = getRowSkinClass();
        String cellClass = getCellSkinClass();
        String firstClass = getFirstRowSkinClass();

        rowClass = mergeStyleClasses(ROW_CLASS_KEY, rowClass, dataTableBase);
        cellClass = mergeStyleClasses(CELL_CLASS_KEY, cellClass, dataTableBase);
        firstClass = mergeStyleClasses(FIRST_ROW_CLASS_KEY, firstClass, dataTableBase);

        saveRowStyles(facesContext, dataTableBase.getClientId(facesContext), firstClass, rowClass, cellClass);
        super.encodeRows(facesContext, rowHolder);
    }
    
    
    /**
     * Returns true if specified attribute (when present on the column) should generate header even if it is not
     * specified on the table
     * 
     * @param table
     *            - rendered UIDataTable
     * @param attributeName
     *            - attribute name
     * @return true if specified attribute should generate header on the table
     */
    // TODO nick - rename this method
    public boolean isHeaderFactoryColumnAttributePresent(UIDataTableBase table, String attributeName) {
        Iterator<UIComponent> columns = table.columns();
        boolean result = false;
        while (columns.hasNext() && !result) {
            UIComponent component = columns.next();
            result = (component.isRendered() && (null != component.getValueExpression(attributeName)));
        }
        return result;
    }

    //TODO: anton -> refactor this
    protected boolean isEncodeHeaders(UIDataTableBase table) {
        return table.isColumnFacetPresent("header") || isHeaderFactoryColumnAttributePresent(table, "sortBy")
            || isHeaderFactoryColumnAttributePresent(table, "comparator")
            || isHeaderFactoryColumnAttributePresent(table, "filterBy");
    }

    protected int getColumnsCount(UIDataTableBase table) {
        // check for exact value in component
        Integer span = (Integer) table.getAttributes().get("columns");
        int count = (null != span && span.intValue() != Integer.MIN_VALUE) ? span.intValue()
            : getColumnsCount(table.columns());
        return count;
    }
    
    public void encodeTableStructure(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable) throws IOException {
        //DataTableRenderer override this method   
    }
    
    @Override
    public void encodeBeforeRows(ResponseWriter writer, FacesContext facesContext, RowHolderBase holder) throws IOException {
        RowHolder rowHolder = (RowHolder)holder;
        Row row = rowHolder.getRow();
   
        if(encodeParentTBody((UIDataTableBase)row)) {
            if(rowHolder.isPartialUpdate()) {
                facesContext.getPartialViewContext().getPartialResponseWriter().startUpdate(row.getClientId(facesContext)+ ":tb");
            }
            encodeTableBodyStart(writer, facesContext, (UIDataTableBase)row);
        }    
    }
    
    public void encodeAfterRows(ResponseWriter writer, FacesContext facesContext, RowHolderBase holder) throws IOException {
        RowHolder rowHolder = (RowHolder)holder;
        Row row = rowHolder.getRow();

        if(encodeParentTBody((UIDataTableBase)row)) {
            encodeTableBodyEnd(writer);
            encodeHiddenContainer(writer, facesContext, (UIDataTableBase)row);
            if(rowHolder.isPartialUpdate()) {
                facesContext.getPartialViewContext().getPartialResponseWriter().endUpdate();
            }    
        }
    }

    
    public abstract boolean encodeParentTBody(UIDataTableBase dataTableBase);
    
    public void encodeTableFacets(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable) throws IOException {

        Object key = dataTable.getRowKey();
        dataTable.captureOrigValue(context);
        dataTable.setRowKey(context, null);
        
        encodeTableStructure(writer, context, dataTable);
        
        setupTableStartElement(context, dataTable);
        encodeHeaderFacet(writer, context, dataTable, false);
        encodeFooterFacet(writer, context, dataTable, false);
        dataTable.setRowKey(context, key);
        dataTable.restoreOrigValue(context);
       
    }
    
    public void encodeTableRows(ResponseWriter writer, FacesContext facesContext, UIDataTableBase dataTableBase,
        boolean encodePartialUpdate) throws IOException {
        put(facesContext, dataTableBase.getClientId(facesContext), CELL_ELEMENT_KEY, HTML.TD_ELEM);
        int rowCount = dataTableBase.getRowCount();
        if (rowCount > 0) {
            processRows(writer, facesContext, dataTableBase, encodePartialUpdate);
        } else {

            String noDataTableBodyId = dataTableBase.getClientId(facesContext) + ":ndtb";
            if (encodePartialUpdate) {
                facesContext.getPartialViewContext().getPartialResponseWriter().startUpdate(noDataTableBodyId);
            }

            int columns = getColumnsCount(dataTableBase.columns());

            writer.startElement(HTML.TBODY_ELEMENT, dataTableBase);
            writer.writeAttribute(HTML.ID_ATTRIBUTE, noDataTableBodyId, null);
            writer.startElement(HTML.TR_ELEMENT, dataTableBase);
            writer.startElement(HTML.TD_ELEM, dataTableBase);
            writer.writeAttribute("colspan", columns, null);

            String styleClass = (String) dataTableBase.getAttributes().get("noDataStyleClass");
            styleClass = styleClass != null ? getNoDataClass() + " " + styleClass : getNoDataClass();

            writer.writeAttribute(HTML.CLASS_ATTRIBUTE, styleClass, null);

            UIComponent noDataFacet = dataTableBase.getNoData();
            if (noDataFacet != null && noDataFacet.isRendered()) {
                noDataFacet.encodeAll(facesContext);
            } else {
                String noDataLabel = dataTableBase.getNoDataLabel();
                if (noDataLabel != null) {
                    writer.writeText(noDataLabel, "noDataLabel");
                }
            }

            writer.endElement(HTML.TD_ELEM);
            writer.endElement(HTML.TR_ELEMENT);
            writer.endElement(HTML.TBODY_ELEMENT);

            if (encodePartialUpdate) {
                facesContext.getPartialViewContext().getPartialResponseWriter().endUpdate();
            }

        }
    }
            
    protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
        if(component instanceof UIDataTableBase) {
            encodeTableRows(writer, context, (UIDataTableBase)component, false);
        }    
    }
    
    public void encodeTableStart(ResponseWriter writer, FacesContext context, UIDataTableBase component) throws IOException {
        writer.startElement(HTML.TABLE_ELEMENT, component);
        writer.writeAttribute(HTML.ID_ATTRIBUTE, component.getClientId(), null);
        String styleClass = getTableSkinClass();
        encodeStyleClass(writer, context, component, HTML.STYLE_CLASS_ATTR, styleClass);
    }
    
    protected void encodeHiddenContainer(ResponseWriter writer, FacesContext context, UIDataTableBase dataTableBase) throws IOException {
        writer.startElement(HTML.TBODY_ELEMENT, dataTableBase);
        writer.writeAttribute(HTML.ID_ATTRIBUTE, dataTableBase.getClientId(context) +":sc",null);
        writer.writeAttribute(HTML.STYLE_ATTRIBUTE, "display: none", null);

        writer.startElement(HTML.TR_ELEMENT, dataTableBase);
        writer.startElement(HTML.TD_ELEM, dataTableBase);
      
        encodeClientScript(writer, context, dataTableBase);
        encodeHiddenInput(writer, context, dataTableBase);
        
        writer.endElement(HTML.TD_ELEM);
        writer.endElement(HTML.TR_ELEMENT);
        writer.endElement(HTML.TBODY_ELEMENT);
    }
   
    
    public void encodeTableEnd(ResponseWriter writer) throws IOException {
        writer.endElement(HTML.TABLE_ELEMENT);
    }
    
    public abstract void encodeClientScript(ResponseWriter writer, FacesContext context, UIDataTableBase component) throws IOException;
    
    public abstract void encodeHiddenInput(ResponseWriter writer, FacesContext context, UIDataTableBase component) throws IOException;
           
    public void encodeTableBodyStart(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable)
        throws IOException {
        writer.startElement(HTML.TBODY_ELEMENT, dataTable);
        writer.writeAttribute(HTML.ID_ATTRIBUTE, dataTable.getClientId(context) + ":tb", null);
        writer.writeAttribute(HTML.CLASS_ATTRIBUTE, getTableSkinClass(), null);
        encodeStyle(writer, context, dataTable, null);
    }
    
    public void encodeTableBodyEnd(ResponseWriter writer) throws IOException {
        writer.endElement(HTML.TBODY_ELEMENT);
    }
  
    public abstract RowHolderBase createRowHolder(FacesContext context, UIComponent component);
      
    public void encodeFooterFacet(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable, 
        boolean encodePartialUpdate) throws IOException {

        UIComponent footer = dataTable.getFooter();
        boolean columnFacetPresent = dataTable.isColumnFacetPresent("footer");

        if ((footer != null && footer.isRendered()) || columnFacetPresent) {
            boolean partialUpdateEncoded = false;

            boolean encodeTfoot = containsThead();
            if (encodeTfoot) {
                String footerClientId = dataTable.getClientId(context) + ":tf";

                if (encodePartialUpdate) {
                    partialUpdateEncoded = true;
                    context.getPartialViewContext().getPartialResponseWriter().startUpdate(footerClientId);
                }

                writer.startElement(HTML.TFOOT_ELEMENT, dataTable);
                writer.writeAttribute(HTML.ID_ATTRIBUTE, footerClientId, null);
                writer.writeAttribute(HTML.CLASS_ATTRIBUTE, "rich-table-tfoot", null);
            }
     
            int columns = getColumnsCount(dataTable);
            String id = dataTable.getClientId(context);

            boolean encodePartialUpdateForChildren = (encodePartialUpdate && !partialUpdateEncoded);
            
            if (columnFacetPresent) {
                
                
                String rowClass =  getColumnFooterSkinClass();
                String cellClass = getColumnFooterCellSkinClass();
                String firstClass = getColumnFooterFirstSkinClass();
                
                rowClass = mergeStyleClasses("columnFooterClass", rowClass, dataTable);
                cellClass = mergeStyleClasses("columnFooterCellClass", cellClass, dataTable);
                firstClass = mergeStyleClasses("firstColumnFooterClass", firstClass, dataTable);
                                
                saveRowStyles(context,id, firstClass, rowClass, cellClass);
                
                String targetId = id + ":cf";
                
                if (encodePartialUpdateForChildren) {
                    context.getPartialViewContext().getPartialResponseWriter().startUpdate(targetId);
                }
                
                writer.startElement(HTML.TR_ELEMENT, dataTable);
                writer.writeAttribute(HTML.ID_ATTRIBUTE, targetId, null);
                
                encodeStyleClass(writer, context, dataTable, null, rowClass);
                encodeColumnFacet(context, writer, dataTable, "footer",columns, cellClass);
                writer.endElement(HTML.TR_ELEMENT);

                if (encodePartialUpdateForChildren) {
                    context.getPartialViewContext().getPartialResponseWriter().endUpdate();
                }
            }

            if (footer != null && footer.isRendered()) {
                         
                String rowClass =  getFooterSkinClass();
                String cellClass = getFooterCellSkinClass();
                String firstClass = getFooterFirstSkinClass();
                
                rowClass = mergeStyleClasses("footerClass", rowClass, dataTable);
                cellClass = mergeStyleClasses("footerCellClass", cellClass, dataTable);
                firstClass = mergeStyleClasses("footerFirstClass", firstClass, dataTable);
                // TODO nick - rename method "encodeTableHeaderFacet"
                saveRowStyles(context, id, firstClass, rowClass, cellClass);
                encodeTableFacet(context, writer, id, columns, footer, "footer", rowClass, cellClass, 
                    encodePartialUpdateForChildren);
            }

            if (encodeTfoot) {
                writer.endElement(HTML.TFOOT_ELEMENT);
                
                if (partialUpdateEncoded) {
                    context.getPartialViewContext().getPartialResponseWriter().endUpdate();
                }
            }
        }

    }
    
    //TODO nick - use org.richfaces.component.util.HtmlUtil.concatClasses(String...)
    protected String mergeStyleClasses(String classAttribibute, String skinClass, UIComponent component) {
        String styleClass = null;
        String resultClass = skinClass; 
                
        if(classAttribibute != null && component != null ) {
            styleClass = (String)component.getAttributes().get(classAttribibute);
        }
        
        if(styleClass != null && styleClass.trim().length() > 0) {
            resultClass = resultClass + " " + styleClass;
        }
     
        return resultClass;
    }
    
    public void encodeHeaderFacet(ResponseWriter writer, FacesContext context, UIDataTableBase dataTable, 
        boolean encodePartialUpdate) throws IOException {

        UIComponent header = dataTable.getHeader();
        boolean isEncodeHeaders = isEncodeHeaders(dataTable);

        boolean encodeThead = containsThead();

        if ((header != null && header.isRendered()) || isEncodeHeaders) {
            boolean partialUpdateEncoded = false;

            if (encodeThead) {
                String headerClientId = dataTable.getClientId(context) + ":th";

                if (encodePartialUpdate) {
                    partialUpdateEncoded = true;
                    context.getPartialViewContext().getPartialResponseWriter().startUpdate(headerClientId);
                }
                
                writer.startElement(HTML.THEAD_ELEMENT, dataTable);
                writer.writeAttribute(HTML.ID_ATTRIBUTE, headerClientId, null);
                writer.writeAttribute(HTML.CLASS_ATTRIBUTE, "rich-table-thead", null);
            }
            
            int columns = getColumnsCount(dataTable);
            String id = dataTable.getClientId(context); 
            
            boolean encodePartialUpdateForChildren = (encodePartialUpdate && !partialUpdateEncoded);
            
            if (header != null && header.isRendered()) {
                                
                String rowClass =  getHeaderSkinClass();
                String cellClass = getHeaderCellSkinClass();
                String firstClass = getHeaderFirstSkinClass();
                
                rowClass = mergeStyleClasses("headerClass", rowClass, dataTable);
                cellClass = mergeStyleClasses("headerCellClass", cellClass, dataTable);
                firstClass = mergeStyleClasses("headerFirstClass", firstClass, dataTable);
                saveRowStyles(context, id, firstClass, rowClass, cellClass);

                encodeTableFacet(context, writer, id, columns, header, "header", rowClass, cellClass, 
                    encodePartialUpdateForChildren);
            }

            if (isEncodeHeaders) {
                String rowClass =  getColumnHeaderSkinClass();
                String cellClass = getColumnHeaderCellSkinClass();
                String firstClass = getColumnHeaderFirstSkinClass();
                
                rowClass = mergeStyleClasses("columnHeaderClass", rowClass, dataTable);
                cellClass = mergeStyleClasses("columnHeaderCellClass", cellClass, dataTable);
                firstClass = mergeStyleClasses("columnHeaderFirstClass", firstClass, dataTable);
                saveRowStyles(context, id, firstClass, rowClass, cellClass);
                 
                String targetId = id + ":ch";
                
                if (encodePartialUpdateForChildren) {
                    context.getPartialViewContext().getPartialResponseWriter().startUpdate(targetId);
                }
                
                writer.startElement(HTML.TR_ELEMENT, dataTable);
                writer.writeAttribute(HTML.ID_ATTRIBUTE, targetId, null);
                
                encodeStyleClass(writer, context, dataTable, null, rowClass);
                
                encodeColumnFacet(context, writer, dataTable, "header", columns, cellClass);
                writer.endElement(HTML.TR_ELEMENT);
                
                if (encodePartialUpdateForChildren) {
                    context.getPartialViewContext().getPartialResponseWriter().endUpdate();
                }
            }

            if (encodeThead) {
                writer.endElement(HTML.THEAD_ELEMENT);
                
                if (partialUpdateEncoded) {
                    context.getPartialViewContext().getPartialResponseWriter().endUpdate();
                }
            }
        }

    }
    
    protected void encodeColumnFacet(FacesContext context, ResponseWriter writer, UIDataTableBase dataTableBase,  String facetName, int colCount, String cellClass) throws IOException {

        int tColCount = 0;
        String id  = dataTableBase.getClientId(context);
        String element = getCellElement(context, id);
        
        Iterator<UIComponent> headers = dataTableBase.columns();
        
        while (headers.hasNext()) {
            UIComponent column = headers.next();
            if (!column.isRendered()) {
                continue;
            }

            Integer colspan = (Integer) column.getAttributes().get("colspan");
            if (colspan != null && colspan.intValue() > 0) {
                tColCount += colspan.intValue();
            } else {
                tColCount++;
            }

            if (tColCount > colCount) {
                break;
            }

            writer.startElement(element, column);

            encodeStyleClass(writer, context, column, null, cellClass);

            writer.writeAttribute("scope", "col", null);
            getUtils().encodeAttribute(context, column, "colspan");
            
            HeaderEncodeStrategy strategy = getHeaderEncodeStrategy(column, facetName);
            if(strategy != null) {
                strategy.encodeBegin(context, writer, column, facetName);
    
                UIComponent facet = column.getFacet(facetName);
                if (facet != null && facet.isRendered()) {
                    facet.encodeAll(context);
                }
    
                strategy.encodeEnd(context, writer, column, facetName);
            } 
            writer.endElement(element);
        }
    }
    
    protected void encodeTableFacet(FacesContext context, ResponseWriter writer, String id, int columns, 
        UIComponent footer, String facetName, String rowClass, String cellClass, boolean encodePartialUpdate) throws IOException {
        
        boolean isColumnGroup = (footer instanceof Row);
        String element = getCellElement(context, id);
        
        boolean partialUpdateEncoded = false;
        
        if (!isColumnGroup) {
            String targetId = id + ":" + facetName.charAt(0);

            if (encodePartialUpdate) {
                partialUpdateEncoded = true;
                context.getPartialViewContext().getPartialResponseWriter().startUpdate(targetId);
            }
            
            writer.startElement(HTML.TR_ELEMENT, footer);
            writer.writeAttribute(HTML.ID_ATTRIBUTE, targetId, null);

            encodeStyleClass(writer, context, footer, null, rowClass);
            
            writer.startElement(element, footer);

            encodeStyleClass(writer, context, footer, null, cellClass);

            if (columns > 0) {
                writer.writeAttribute("colspan", String.valueOf(columns), null);
            }

            writer.writeAttribute("scope", "colgroup", null);
        }    
        
        if (encodePartialUpdate && !partialUpdateEncoded) {
            context.getPartialViewContext().getPartialResponseWriter().startUpdate(footer.getClientId(context));
        }
        
        footer.encodeAll(context);
       
        if (encodePartialUpdate && !partialUpdateEncoded) {
            context.getPartialViewContext().getPartialResponseWriter().endUpdate();
        }

        if (!isColumnGroup){
            writer.endElement(element);
            writer.endElement(HTML.TR_ELEMENT);
            
            if (partialUpdateEncoded) {
                context.getPartialViewContext().getPartialResponseWriter().endUpdate();
            }
        }    
    }   
    
    public  abstract HeaderEncodeStrategy getHeaderEncodeStrategy(UIComponent column, String tableFacetName);
      
    public abstract boolean containsThead();
    
    public abstract String getTableSkinClass();
    
    public abstract String getFirstRowSkinClass();

    public abstract String getRowSkinClass();
        
    public abstract String getHeaderCellSkinClass();

    public abstract String getHeaderSkinClass();

    public abstract String getHeaderFirstSkinClass();
    
    public abstract String getColumnHeaderCellSkinClass();

    public abstract String getColumnHeaderSkinClass();
    
    public abstract String getColumnHeaderFirstSkinClass();

    public abstract String getFooterCellSkinClass();

    public abstract String getFooterSkinClass();
    
    public abstract String getFooterFirstSkinClass();

    public abstract String getColumnFooterCellSkinClass();

    public abstract String getColumnFooterSkinClass();
    
    public abstract String getColumnFooterFirstSkinClass();
    
    public abstract String getCellSkinClass();
    
    public abstract String getNoDataClass();

    protected abstract void setupTableStartElement(FacesContext context, UIComponent component);

    public void encodeMetaComponent(FacesContext context, UIComponent component, String metaComponentId)
        throws IOException {

        UIDataTableBase table = (UIDataTableBase) component;
        
        setupTableStartElement(context, component);
        
        if (UIDataTableBase.HEADER.equals(metaComponentId)) {
            encodeHeaderFacet(context.getResponseWriter(), context, table, true);
        } else if (UIDataTableBase.FOOTER.equals(metaComponentId)) {
            encodeFooterFacet(context.getResponseWriter(), context, table, true);
        } else if(UIDataTableBase.BODY.equals(metaComponentId)) {
            encodeTableRows(context.getResponseWriter(), context, table, true);
        } else {
            throw new IllegalArgumentException("Unsupported metaComponentIdentifier: " + metaComponentId);
        }
    }
}
