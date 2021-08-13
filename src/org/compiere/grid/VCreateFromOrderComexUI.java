/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.compiere.grid;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.compiere.apps.ADialog;
import org.compiere.apps.AEnv;
import org.compiere.apps.ConfirmPanel;
import org.compiere.grid.ed.VLookup;
import org.compiere.model.GridTab;
import org.compiere.model.MDocType;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.swing.CButton;
import org.compiere.swing.CPanel;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

public class VCreateFromOrderComexUI extends CreateFromCierreComex implements ActionListener, VetoableChangeListener {
	private static final long serialVersionUID = 1L;

	private VCreateFromDialog dialog;
	//int C_BPartner_ID = 0;
	int M_Product_ID = 0;
	int C_Order_ID = 0;
	
	public VCreateFromOrderComexUI(GridTab mTab) {
		super(mTab);
		log.info(getGridTab().toString());

		dialog = new VCreateFromDialog(this, getGridTab().getWindowNo(), true);

		p_WindowNo = getGridTab().getWindowNo();

		try {
			if (!dynInit())
				return;
			jbInit();

			setInitOK(true);
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
			setInitOK(false);
		}
		AEnv.positionCenterWindow(Env.getWindow(p_WindowNo), dialog);
	} // VCreateFrom

	/** Window No */
	private int p_WindowNo;

	/** Logger */
	private CLogger log = CLogger.getCLogger(getClass());

	private JLabel orderLabel = new JLabel();
	private JComboBox orderField = new JComboBox();

	/**
	 * Dynamic Init
	 * 
	 * @throws Exception if Lookups cannot be initialized
	 * @return true if initialized
	 */
	public boolean dynInit() throws Exception {
		log.config("");

		super.dynInit();

		// Refresh button
		CButton refreshButton = ConfirmPanel.createRefreshButton(false);
		refreshButton.setMargin(new Insets(1, 10, 0, 10));
		refreshButton.setDefaultCapable(true);
		refreshButton.addActionListener(this);
		dialog.getConfirmPanel().addButton(refreshButton);
		dialog.getRootPane().setDefaultButton(refreshButton);
		
		/*if (getGridTab().getValue("C_Order_ID") != null) {
			ADialog.error(0, dialog, null, "La preventa ya tiene lineas de una OC, debe crear una nueva o volver a ingresar todas las lineas nuevamente");
			return false;
		}*/
		
		dialog.setTitle(getTitle());

		// RMA Selection option should only be available for AP Credit Memo
		Integer docTypeId = (Integer) getGridTab().getValue("C_DocType_ID");
		MDocType docType = MDocType.get(Env.getCtx(), docTypeId);
		Integer orderId = (Integer) getGridTab().getValue("C_Order_ID");
//		int M_Product_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "M_Product_ID");
		initOrdenes(orderId);
		//bPartnerField.addVetoableChangeListener(this);

		//Timestamp date = Env.getContextAsDate(Env.getCtx(), p_WindowNo, "DateRequired");
		//dateToField.setValue(date);

		return true;
	} // dynInit

	/**
	 * Static Init.
	 * 
	 * <pre>
	 *  parameterPanel
	 *      parameterBankPanel
	 *      parameterStdPanel
	 *          bPartner/order/invoice/shopment/licator Label/Field
	 *  dataPane
	 *  southPanel
	 *      confirmPanel
	 *      statusBar
	 * </pre>
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		orderLabel.setText(Msg.getElement(Env.getCtx(), "C_Order_ID", false));

		/*dateFromLabel.setLabelFor(dateFromField);
		dateFromField.setToolTipText(Msg.translate(Env.getCtx(), "DateFrom"));
		dateToLabel.setLabelFor(dateToField);
		dateToField.setToolTipText(Msg.translate(Env.getCtx(), "DateTo"));*/

		CPanel parameterPanel = dialog.getParameterPanel();
		parameterPanel.setLayout(new BorderLayout());

		CPanel parameterStdPanel = new CPanel(new GridBagLayout());

		parameterPanel.add(parameterStdPanel, BorderLayout.CENTER);
		/*parameterStdPanel.add(bPartnerLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		if (bPartnerField != null)
			parameterStdPanel.add(bPartnerField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0);*/

		parameterStdPanel.add(orderLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		parameterStdPanel.add(orderField, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,	GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
	} // jbInit

	/*************************************************************************/

	private boolean m_actionActive = false;

	/**
	 * Action Listener
	 * 
	 * @param e event
	 */
	public void actionPerformed(ActionEvent e) {
		log.config("Action=" + e.getActionCommand());
		if (m_actionActive)
			return;
		m_actionActive = true;
		log.config("Action=" + e.getActionCommand());
		// Order
		if (e.getSource().equals(orderField)) {
			KeyNamePair pp = (KeyNamePair) orderField.getSelectedItem();
			int C_Order_ID = 0;
			if (pp != null)
				C_Order_ID = pp.getKey();
			initDetails(C_Order_ID);
		}

		if (e.getActionCommand().equals(ConfirmPanel.A_REFRESH)) {
			Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
//			initDetails(C_BPartner_ID);
			initDetails(C_Order_ID);
			dialog.tableChanged(null);
			Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		}
		m_actionActive = false;
	} // actionPerformed

	/**
	 * Change Listener
	 * 
	 * @param e event
	 */
	public void vetoableChange(PropertyChangeEvent e) {
		log.config(e.getPropertyName() + "=" + e.getNewValue());
		if (e.getPropertyName().equals("M_Product_ID")) {
			M_Product_ID = ((Integer) e.getNewValue()).intValue();
			initOrdenes(M_Product_ID);
		} else if (e.getPropertyName().equals("C_Order_ID")) {
			C_Order_ID = ((Integer) e.getNewValue()).intValue();
			initDetails(C_Order_ID);
		}

		dialog.tableChanged(null);
	} // vetoableChange

	/**
	 * Load Data - Order
	 * 
	 * @param C_Order_ID Order
	 * @param forInvoice true if for invoice vs. delivery qty
	 */
	protected void loadOrder(int C_Order_ID, boolean forInvoice) {
		loadTableOIS(getOrderData(C_Order_ID, forInvoice));
	} // LoadOrder
	/**
	 * Load Order/Invoice/Shipment data into Table
	 * 
	 * @param data data
	 */
	protected void loadTableOIS(Vector<?> data) {
		// Remove previous listeners
		dialog.getMiniTable().getModel().removeTableModelListener(dialog);
		// Set Model
		DefaultTableModel model = new DefaultTableModel(data, getOISColumnNames());
		model.addTableModelListener(dialog);
		dialog.getMiniTable().setModel(model);
		//
		configureMiniTable(dialog.getMiniTable());
	} // loadOrder

	public void showWindow() {
		dialog.setVisible(true);
	}

	public void closeWindow() {
		dialog.dispose();
	}
	
	protected void initDetails(int C_Order_ID) {
		log.config("C_Order_ID=" + C_Order_ID);
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ol.C_OrderLine_ID, to_char(ol.Line), ol.M_Product_ID, p.Value, ol.QtyEntered, ol.PriceEntered, ROUND((ol.PriceEntered*ol.QtyEntered),2), p.name");
		sql.append(" FROM C_OrderLine ol, M_Product p");
		sql.append(" WHERE ol.M_Product_ID = p.M_Product_ID");
		sql.append(" AND ol.C_Order_ID=" + C_Order_ID);
		sql.append(" ORDER BY ol.Line");

		log.config("**" + sql.toString());
		try {
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Vector<Object> line = new Vector<Object>(10);
				line.add(new Boolean(false)); // 0-Selection
//				line.add(rs.getInt(2)); // 2- Line
				KeyNamePair ppOl = new KeyNamePair(rs.getInt(1), rs.getString(2));
				line.add(ppOl); // 1--c_orderline_id
				KeyNamePair pp = new KeyNamePair(rs.getInt(3), rs.getString(4));
				line.add(pp); // 3--m_product_id
				line.add(rs.getString(8));
				line.add(rs.getBigDecimal(5));
				line.add(rs.getBigDecimal(6));
				line.add(rs.getBigDecimal(7));
				
				data.add(line);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql.toString(), e);
		}

		Vector<String> columnNames = new Vector<String>(10);
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add(Msg.getElement(Env.getCtx(), "C_OrderLine_ID"));
		columnNames.add(Msg.getElement(Env.getCtx(), "M_Product_ID"));
		columnNames.add("Cantidad");
		columnNames.add("Precio");
		columnNames.add("Neto Linea");
		dialog.getMiniTable().getModel().removeTableModelListener(dialog);
		// Set Model
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		model.addTableModelListener(dialog);
		dialog.getMiniTable().setModel(model);
		//
		dialog.getMiniTable().setColumnClass(0, Boolean.class, false); // 0-Selection
		dialog.getMiniTable().setColumnClass(1, String.class, true); // 1-C_OrderLine
		dialog.getMiniTable().setColumnClass(2, String.class, true); // 2-Product
		dialog.getMiniTable().setColumnClass(3, BigDecimal.class, true); // 3-Cant. Recibida
		dialog.getMiniTable().setColumnClass(4, BigDecimal.class, true); // 4-Precio
		dialog.getMiniTable().setColumnClass(5, BigDecimal.class, true); // 5-Neto Linea
	} // initBPartnerOIS
	
	protected void initOrdenes(int C_Order_ID) {
		KeyNamePair pp = new KeyNamePair(C_Order_ID,"");
		//  load PO Orders - Closed, Completed
		orderField.removeActionListener(this);
		orderField.removeAllItems();
		orderField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadOrdenes(C_Order_ID);
		for(KeyNamePair knp : list)
			orderField.addItem(knp);
		
		orderField.setSelectedIndex(0);
		orderField.addActionListener(this);
		
		if (list.size()>0)
			orderField.setSelectedIndex(1);
		
		orderField.setEnabled(false);
		dialog.pack();

//		initBPDetails(C_BPartner_ID);

	} // initBPartnerOIS
	
	private ArrayList<KeyNamePair> loadOrdenes(int C_Order_ID) {
		log.config("C_Order_ID=" + C_Order_ID);
		
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer display = new StringBuffer("o.DocumentNo||' - ' ||")
				.append(DB.TO_CHAR("o.DatePromised", DisplayType.Date, Env.getAD_Language(Env.getCtx())));

		StringBuffer sql = new StringBuffer("SELECT o.C_Order_ID,").append(display)
		.append(" FROM C_Order o");
		sql.append(" WHERE o.C_Order_ID = " + C_Order_ID);
		
		log.fine(sql.toString());
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return list;
	}

}
