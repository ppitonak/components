/**
 * 
 */
package org.richfaces.component;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitResult;
import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.SequenceRange;
import org.jboss.test.faces.AbstractFacesTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Konstantin Mishin
 *
 */
public class UIExtendedDataTableTest extends AbstractFacesTest {

    
    private UIExtendedDataTable table;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupFacesRequest();
        table = new UIExtendedDataTable();
        table.setValue(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        table = null;
        super.tearDown();
    }

    /**
     * Test method for {@link org.richfaces.component.UIExtendedDataTable#visitDataChildren(javax
     * .faces.component.visit.VisitContext, javax.faces.component.visit.VisitCallback, boolean)}.
     */
    @Test
    public final void testVisitDataChildren() {
        //TODO fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.richfaces.component.UIExtendedDataTable#getActualFirst()}.
     */
    @Test
    public final void testGetActualFirst() {
        assertEquals(0, table.getActualFirst());
        table.setFirst(5);
        table.setClientFirst(3);
        assertEquals(8, table.getActualFirst());
    }

    /**
     * Test method for {@link org.richfaces.component.UIExtendedDataTable#getActualRows()}.
     */
    @Test
    public final void testGetActualRows() {
        assertEquals(0, table.getActualRows());
        table.setRows(5);
        assertEquals(5, table.getActualRows());
        table.setClientRows(3);
        assertEquals(3, table.getActualRows());
        table.setClientRows(8);
        assertEquals(5, table.getActualRows());
    }

    /**
     * Test method for {@link org.richfaces.component.UIExtendedDataTable#setFirst(int)}.
     */
    @Test
    public final void testSetFirst() {
        table.setClientFirst(3);
        assertEquals(3, table.getClientFirst());
        table.setFirst(3);
        assertEquals(0, table.getClientFirst());
    }

    /**
     * Test method for {@link org.richfaces.component.UIExtendedDataTable#walk(javax.faces.context.FacesContext,
     * org.ajax4jsf.model.DataVisitor, org.ajax4jsf.model.Range, java.lang.Object)}.
     */
    @Test
    public final void testWalkFacesContextDataVisitorRangeObject() {
        final StringBuilder builder = new StringBuilder(5);
        table.walk(facesContext, new DataVisitor() {
            public DataVisitResult process(FacesContext context, Object rowKey, Object argument) {
                table.setRowKey(rowKey);
                builder.append(table.getRowData());
                return DataVisitResult.CONTINUE;
            }
        }, new SequenceRange(2, 5), null);
        assertEquals("23456", builder.toString());
    }

    /**
     * Test method for {@link org.richfaces.component.UIExtendedDataTable#getClientFirst()}
     * and {@link org.richfaces.component.UIExtendedDataTable#setClientFirst(int)}.
     */
    @Test
    public final void testClientFirst() {
        assertEquals(0, table.getClientFirst());
        table.setClientFirst(3);
        assertEquals(3, table.getClientFirst());
    }

    /**
     * Test method for {@link org.richfaces.component.UIExtendedDataTable#getClientRows()}
     * and {@link org.richfaces.component.UIExtendedDataTable#setClientRows(int)}.
     */
    @Test
    public final void testClientRows() {
        assertEquals(0, table.getClientRows());
        table.setClientRows(3);
        assertEquals(3, table.getClientRows());
    }
}
