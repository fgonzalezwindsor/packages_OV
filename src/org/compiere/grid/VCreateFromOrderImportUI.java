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
import javax.swing.table.DefaultTableModel;

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

public class VCreateFromOrderImportUI extends CreateFromPrereserva implements ActionListener, VetoableChangeListener {
	private static final long serialVersionUID = 1L;

	private VCreateFromDialog dialog;
	//int C_BPartner_ID = 0;
	int M_Product_ID = 0;
	int C_Order_ID = 0;
	
	public VCreateFromOrderImportUI(GridTab mTab) {
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

	//
	/*private JLabel bPartnerLabel = new JLabel();
	private VLookup bPartnerField;*/

	private JLabel orderLabel = new JLabel();
	private JComboBox orderField = new JComboBox();

	/*private CLabel dateFromLabel = new CLabel(Msg.translate(Env.getCtx(), "Fecha Vencimiento"));
	protected VDate dateFromField = new VDate("DateFrom", false, false, true, DisplayType.Date, Msg.translate(Env.getCtx(), "DateFrom"));
	private CLabel dateToLabel = new CLabel("-");
	protected VDate dateToField = new VDate("DateTo", false, false, true, DisplayType.Date, Msg.translate(Env.getCtx(), "DateTo"));*/
	
	private JLabel productoLabel = new JLabel();
	private VLookup productoField;

	// faaguilar OFB End
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
		dialog.setTitle(getTitle());
		
		

		// RMA Selection option should only be available for AP Credit Memo
		Integer docTypeId = (Integer) getGridTab().getValue("C_DocType_ID");
		MDocType docType = MDocType.get(Env.getCtx(), docTypeId);

//		initBPartner(true);
		initProduct();
		productoField.addVetoableChangeListener(this);
		productoField.addActionListener(this);
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
//		bPartnerLabel.setText(Msg.getElement(Env.getCtx(), "C_BPartner_ID"));
		productoLabel.setText(Msg.getElement(Env.getCtx(), "M_Product_ID"));
		orderLabel.setText(Msg.getElement(Env.getCtx(), "C_Order_ID", false));

		/*dateFromLabel.setLabelFor(dateFromField);
		dateFromField.setToolTipText(Msg.translate(Env.getCtx(), "DateFrom"));
		dateToLabel.setLabelFor(dateToField);
		dateToField.setToolTipText(Msg.translate(Env.getCtx(), "DateTo"));*/

		CPanel parameterPanel = dialog.getParameterPanel();
		parameterPanel.setLayout(new BorderLayout());

		CPanel parameterStdPanel = new CPanel(new GridBagLayout());

		parameterPanel.add(parameterStdPanel, BorderLayout.CENTER);
		
		parameterStdPanel.add(productoLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		if (productoField != null)
			parameterStdPanel.add(productoField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));

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
		
		if (e.getSource().equals(productoField)) {
			initOrdenes(((Integer)productoField.getValue()).intValue());
		}
//		initDetails(C_BPartner_ID);

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

		// BPartner - load Order/Invoice/Shipment
		/*if (e.getPropertyName().equals("C_BPartner_ID")) {
			C_BPartner_ID = ((Integer) e.getNewValue()).intValue();
			// initBPOrderDetails (C_BPartner_ID, true);
			initDetails(C_BPartner_ID);
		}*/

		dialog.tableChanged(null);
	} // vetoableChange

	/**************************************************************************
	 * Load BPartner Field
	 * 
	 * @param forInvoice true if Invoices are to be created, false receipts
	 * @throws Exception if Lookups cannot be initialized
	 */
//	protected void initBPartner(boolean forInvoice) throws Exception {
//		// load BPartner
//		int AD_Column_ID = 3499; // C_Invoice.C_BPartner_ID
//		MLookup lookup = MLookupFactory.get(Env.getCtx(), p_WindowNo, 0, AD_Column_ID, DisplayType.Search);
////		bPartnerField = new VLookup("C_BPartner_ID", true, false, true, lookup);
//		productoField = new VLookup("M_Product_ID", true, false, true, lookup);
//		//
//		/*int C_BPartner_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "C_BPartner_ID");
//		bPartnerField.setValue(new Integer(C_BPartner_ID));*/
//		
//		int M_Product_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "M_Product_ID");
//		productoField.setValue(new Integer(M_Product_ID));
//		
//		// initBPOrderDetails(C_BPartner_ID, forInvoice);
//		//initDetails(C_BPartner_ID);
//		initDetails(C_Order_ID);
//	} // initBPartner
	
	protected void initProduct() throws Exception {
		int AD_Column_ID = 1019566; //OV_PrereservaLine.M_Product_ID
		MLookup lookup = MLookupFactory.get(Env.getCtx(), p_WindowNo, 0, AD_Column_ID, DisplayType.Search);
		productoField = new VLookup("M_Product_ID", true, false, true, lookup);
		
		int M_Product_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "M_Product_ID");
//		productoField.setValue(new Integer(M_Product_ID));aki

		//  initial loading
		initOrdenes(M_Product_ID);
	}

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

	/**
	 * 
	 * @param C_BPartner_ID BPartner
	 * @param forInvoice    for invoice false for Payments
	 */
	protected void initDetails(int C_Order_ID) {
		log.config("C_Order_ID=" + C_Order_ID);
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		
		StringBuffer sqlSumPrereservaLine = new StringBuffer();
		sqlSumPrereservaLine.append(" SELECT coalesce(sum(pl.Qty),0)"); 
		sqlSumPrereservaLine.append(" FROM OV_PrereservaLine pl, OV_Prereserva p"); 
		sqlSumPrereservaLine.append(" WHERE pl.OV_Prereserva_ID = p.OV_Prereserva_ID"); 
		sqlSumPrereservaLine.append(" AND p.DocStatus IN ('CO','CL')"); 
		sqlSumPrereservaLine.append(" AND pl.M_Product_ID = ol.m_product_id");
		sqlSumPrereservaLine.append(" AND pl.C_OrderLine_ID = ol.C_OrderLine_ID");

		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ol.C_OrderLine_ID, ol.Line, ol.M_Product_ID, p.Value, ol.QtyEntered, (ol.QtyEntered - (");
		sql.append(sqlSumPrereservaLine);
		sql.append(" )) AS QtyDisponible");
		sql.append(" FROM C_OrderLine ol, M_Product p");
		sql.append(" WHERE ol.M_Product_ID = p.M_Product_ID");
		sql.append(" AND ol.C_Order_ID=" + C_Order_ID);
		sql.append(" GROUP BY ol.C_OrderLine_ID, ol.Line, ol.M_Product_ID, p.Value, ol.QtyEntered");
		sql.append(" HAVING (ol.QtyEntered - (");
		sql.append(sqlSumPrereservaLine);
		sql.append(" )) > 0");

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
//				line.add(rs.getString(4)); // 4- value
				KeyNamePair pp = new KeyNamePair(rs.getInt(3), rs.getString(4));
				line.add(pp); // 3--m_product_id
				line.add(rs.getBigDecimal(5)); // 5-qtyentered
				line.add(rs.getBigDecimal(6)); // 6-qtydisponible
				data.add(line);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql.toString(), e);
		}

		Vector<String> columnNames = new Vector<String>(10);
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
//		columnNames.add(Msg.getElement(Env.getCtx(), "Line"));
		columnNames.add(Msg.getElement(Env.getCtx(), "C_OrderLine_ID"));
//		columnNames.add(Msg.getElement(Env.getCtx(), "Value"));
		columnNames.add(Msg.getElement(Env.getCtx(), "M_Product_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyEntered"));
		columnNames.add("Cant. Disponible");
		columnNames.add("Cant. Solicitada");
		dialog.getMiniTable().getModel().removeTableModelListener(dialog);
		// Set Model
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		model.addTableModelListener(dialog);
		dialog.getMiniTable().setModel(model);
		//
		dialog.getMiniTable().setColumnClass(0, Boolean.class, false); // 0-Selection
//		dialog.getMiniTable().setColumnClass(1, Integer.class, true); // 1-Line
		dialog.getMiniTable().setColumnClass(1, Integer.class, true); // 1-C_OrderLine
//		dialog.getMiniTable().setColumnClass(3, String.class, true); // 3-Value
		dialog.getMiniTable().setColumnClass(2, String.class, true); // 2-Product
		dialog.getMiniTable().setColumnClass(3, BigDecimal.class, true); // 3-QtyEntered
		dialog.getMiniTable().setColumnClass(4, BigDecimal.class, true); // 4-QtyDisponible
		dialog.getMiniTable().setColumnClass(5, BigDecimal.class, false); // 5-Qty
	} // initBPartnerOIS
	
	protected void initOrdenes(int M_Product_ID) {
		KeyNamePair pp = new KeyNamePair(0,"");
		//  load PO Orders - Closed, Completed
		orderField.removeActionListener(this);
		orderField.removeAllItems();
		orderField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadOrdenes(M_Product_ID);
		for(KeyNamePair knp : list)
			orderField.addItem(knp);
		
		orderField.setSelectedIndex(0);
		orderField.addActionListener(this);
		dialog.pack();

//		initBPDetails(C_BPartner_ID);

	} // initBPartnerOIS
	
	private ArrayList<KeyNamePair> loadOrdenes(int M_Product_ID) {
		log.config("M_Product_ID=" + M_Product_ID);
		
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer display = new StringBuffer("o.DocumentNo||' - ' ||")
				.append(DB.TO_CHAR("o.DatePromised", DisplayType.Date, Env.getAD_Language(Env.getCtx())))
				.append("||' - '||")
				.append("bp.Name");

		StringBuffer sql = new StringBuffer("SELECT o.C_Order_ID,").append(display)
		.append(" FROM C_Order o");
		sql.append(" INNER JOIN AD_Org org ON (o.AD_Org_ID=org.AD_Org_ID) ");
		sql.append(" INNER JOIN C_BPartner bp ON (o.C_BPartner_ID=bp.C_BPartner_ID) ");
		sql.append(" INNER JOIN C_OrderLine ol ON (ol.C_Order_ID=o.C_Order_ID) AND ol.qtydelivered = 0 ");
		sql.append(" INNER JOIN C_DocType doc ON (o.C_DocType_ID=doc.C_DocType_ID) ");
		sql.append(" LEFT Join C_PaymentTerm pter ON (o.C_PaymentTerm_ID=pter.C_PaymentTerm_ID) ");
		sql.append(" WHERE o.Processed='Y' AND o.DocStatus IN ('CO','CL')  and o.AD_Client_ID="	+ Env.getAD_Client_ID(Env.getCtx()));
		sql.append(" AND ol.M_Product_ID = " + M_Product_ID);
		sql.append(" AND o.C_DocType_ID = 1000047 ");
		sql.append(" AND ol.QtyEntered > coalesce((SELECT sum(pl.Qty) "
												+ " FROM OV_Prereserva p, OV_PrereservaLine pl "
												+ " WHERE p.OV_Prereserva_ID = pl.OV_Prereserva_ID "
												+ " AND pl.M_Product_ID = " + M_Product_ID
												+ " AND pl.C_OrderLine_ID = ol.C_OrderLine_ID"
												+ " AND p.DocStatus IN ('CO','CL')),0)");
		sql.append(" GROUP BY o.C_Order_ID, o.DocumentNo, o.DatePromised, bp.Name");
		
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
