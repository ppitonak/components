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

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.model.DataVisitResult;
import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.renderkit.RendererBase;
import org.richfaces.component.Row;

/**
 * @author Anton Belevich
 * 
 */
public abstract class AbstractRowsRenderer extends RendererBase implements DataVisitor {
  
    public abstract void encodeRow(ResponseWriter writer, FacesContext facesContext, RowHolderBase rowHolder) throws IOException;
    
    public abstract RowHolderBase createRowHolder(FacesContext context, UIComponent component);
    
    public DataVisitResult process(FacesContext facesContext, Object rowKey, Object argument) {
        RowHolderBase holder = (RowHolderBase) argument;
        Row row = holder.getRow();
        row.setRowKey(facesContext, rowKey);
        
        try {
            ResponseWriter writer = facesContext.getResponseWriter();
            encodeRow(writer, facesContext, holder);
        } catch (IOException e) {
            throw new FacesException(e);
        }
        holder.nextRow();
        return DataVisitResult.CONTINUE;
    }
    
    protected void encodeRows(FacesContext facesContext, RowHolderBase rowHolder) {
        rowHolder.getRow().walk(facesContext, this, rowHolder);
    }

    public void processRows(ResponseWriter writer, FacesContext facesContext, UIComponent component, boolean updatePartial) throws IOException {
        RowHolderBase rowHolder = createRowHolder(facesContext, component);
        rowHolder.setPartialUpdate(updatePartial);
        
        encodeBeforeRows(writer, facesContext, rowHolder);
        encodeRows(facesContext, rowHolder);
        encodeAfterRows(writer, facesContext, rowHolder);
    }
    

    protected void doEncodeChildren(ResponseWriter writer, FacesContext facesContext, UIComponent component) throws IOException {
        processRows(writer, facesContext, component, false);
    }

    public void  encodeBeforeRows(ResponseWriter writer, FacesContext facesContext, RowHolderBase holder) throws IOException {
    }
    
    public void  encodeAfterRows(ResponseWriter writer, FacesContext facesContext, RowHolderBase holder) throws IOException {
    }

    public boolean getRendersChildren() {
        return true;
    }
}
