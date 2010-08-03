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

import javax.faces.context.FacesContext;

import org.richfaces.component.Row;

/**
 * @author Anton Belevich
 *
 */
public class RowHolder extends RowHolderBase {

    private Row row;
   
    private int processCell;
    
    private String baseClientId;
    
    private boolean isRowStart;
    
    private boolean startPartialUpdate;
   
    public RowHolder(FacesContext context, Row row) {
        this(context, row, 0, true);
    }

    public RowHolder(FacesContext context, Row row, int processCell, boolean isRowStart) {
        super(context);
        this.row = row;
        this.processCell = processCell;
        this.baseClientId = row.getClientId(context);
    }

    public boolean isStartPartialUpdate() {
        return startPartialUpdate;
    }

    public void setStartPartialUpdate(boolean startPartialUpdate) {
        this.startPartialUpdate = startPartialUpdate;
    }

    public String getBaseClientId() {
        return baseClientId;
    }

    public void setBaseClientId(String baseClientId) {
        this.baseClientId = baseClientId;
    }
   
    public Row getRow() {
        return this.row;
    }

    public int getProcessCell() {
        return processCell;
    }

    public void resetProcessCell() {
        this.processCell = 0;
    }
    
    public int nextCell() {
        return processCell++;
    }
     
    public boolean isRowStart() {
        return isRowStart;
    }

    public void setRowStart(boolean isRowStart) {
        this.isRowStart = isRowStart;
    }
}
