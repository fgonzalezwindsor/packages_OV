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

//import it.businesslogic.ireport.gui.JNumberField;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.compiere.apps.AEnv;
import org.compiere.apps.ConfirmPanel;
import org.compiere.grid.ed.VDate;
//import org.compiere.grid.ed.Calculator;
import org.compiere.grid.ed.VLookup;
import org.compiere.grid.ed.VNumber;
import org.compiere.model.GridTab;
//import org.compiere.model.MBankAccount;
//import org.compiere.model.MBankStatement;
import org.compiere.model.MDocType;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.swing.CButton;
import org.compiere.swing.CLabel;
import org.compiere.swing.CPanel;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

public class VCreateFromInvoiceUI extends CreateFromInvoice implements ActionListener, VetoableChangeListener
{
	private static final long serialVersionUID = 1L;
	
	private VCreateFromDialog dialog;
	private String docBaseType; //faaguilar OFB
	int C_BPartner_ID=0;
	private String valuePTK = "";

	public VCreateFromInvoiceUI(GridTab mTab)
	{
		super(mTab);
		log.info(getGridTab().toString());
		//faaguilar OFB begin
		Integer docTypeId = (Integer)getGridTab().getValue("C_DocTypeTarget_ID");
		MDocType docType = MDocType.get(Env.getCtx(), docTypeId);
		docBaseType=docType.getDocBaseType();
    	if(docBaseType.equals("FAT"))//factoring
    		wType=1;
    	else if  (docBaseType.equals("PTK"))//protesto
    	{
    		wType=2;
    		//ininoles seteamos tipo de protesto    					
			valuePTK = "BA";
			try
			{
				valuePTK = docType.get_ValueAsString("ptkType");
			}
			catch(Exception e)
	        {
				valuePTK = "BA";
				log.log(Level.SEVERE, "No se pudo setear variable ptkType", e);
	        }
			if(valuePTK == null || valuePTK == "" || valuePTK == " ")
				valuePTK = "BA";
			//ininoles end
    	}
    	else if  ( docBaseType.equals("CDC"))//cambio doc cliente
    		wType=3;
    	else if  ( docBaseType.equals("VDC"))//cambio doc vendor
    		wType=4;
    	else if  ( docBaseType.equals("PRV") && docType.getName().toLowerCase().contains("nota"))//tipo para mutual nota de credito
    		wType=6;
    	else if  ( docBaseType.equals("PRV"))//tipo para mutual nota de credito
    		wType=5;
    	else
    		wType=0;// create from comun
    	
    	if(wType==2)
    		getPay=true;
    	
		//faaguilar OFB end
		
		dialog = new VCreateFromDialog(this, getGridTab().getWindowNo(), true);
		
		p_WindowNo = getGridTab().getWindowNo();

		try
		{
			if (!dynInit())
				return;
			jbInit();

			setInitOK(true);
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
			setInitOK(false);
		}
		AEnv.positionCenterWindow(Env.getWindow(p_WindowNo), dialog);
	}   //  VCreateFrom
	
	/** Window No               */
	private int p_WindowNo;

	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(getClass());
	
	//
	private JLabel bPartnerLabel = new JLabel();
	private VLookup bPartnerField;
	
	private JLabel orderLabel = new JLabel();
	private JComboBox orderField = new JComboBox();
	
	private JLabel shipmentLabel = new JLabel();
	private JComboBox shipmentField = new JComboBox();
	
	private JLabel llegadaLabel = new JLabel();
	private JComboBox llegadaField = new JComboBox();
    
    /** Label for the rma selection */
	private JLabel rmaLabel = new JLabel();
    /** Combo box for selecting RMA document */
	private JComboBox rmaField = new JComboBox();
	
	//faaguilar OFB Begin
	private JCheckBox getPayments = new JCheckBox();
	private CLabel dateFromLabel = new CLabel(Msg.translate(Env.getCtx(), "Fecha Vencimiento"));
	protected VDate dateFromField = new VDate("DateFrom", false, false, true, DisplayType.Date, Msg.translate(Env.getCtx(), "DateFrom"));
	private CLabel dateToLabel = new CLabel("-");
	protected VDate dateToField = new VDate("DateTo", false, false, true, DisplayType.Date, Msg.translate(Env.getCtx(), "DateTo"));
	private CLabel bankListLabel = new CLabel("Banco");
	protected JComboBox bankListField = new JComboBox();
	//ininoles nuevos filtros solicitados
	private CLabel payTypeLabel = new CLabel("Tipo Pago");
	protected JComboBox payTypeField = new JComboBox();
	private CLabel NPaymentLabel = new CLabel("N° Documento");
	protected JTextField NPaymentField = new JTextField();
	private CLabel amtFromLabel = new CLabel("Cantidad");
	protected VNumber amtFromField = new VNumber();
	private CLabel amtToLabel = new CLabel("-");
	protected VNumber amtToField = new VNumber();
	//ininoles se agrega parametro fecha contable
	private CLabel dateFromLabelAcct = new CLabel(Msg.translate(Env.getCtx(), "DateAcct"));
	protected VDate dateFromFieldAcct = new VDate("DateFromAcct", false, false, true, DisplayType.Date, Msg.translate(Env.getCtx(), "DateFromAcct"));
	private CLabel dateToLabelAcct = new CLabel("-");
	protected VDate dateToFieldAcct = new VDate("DateToAcct", false, false, true, DisplayType.Date, Msg.translate(Env.getCtx(), "DateToAcct"));
	//ininoles nuevos filtros mutual
	private JLabel docNoLabel = new JLabel();
	private VLookup docNoField;
	private CLabel REmployeeLabel = new CLabel("Rut Trabajador");
	protected JTextField REmployeeField = new JTextField();
	private CLabel NEmployeeLabel = new CLabel("Solo Nombre");
	protected JTextField NEmployeeField = new JTextField();
	
	
	//faaguilar OFB End
	/**
	 *  Dynamic Init
	 *  @throws Exception if Lookups cannot be initialized
	 *  @return true if initialized
	 */
	public boolean dynInit() throws Exception
	{
		log.config("");
		
		super.dynInit();
		
		//Refresh button
				CButton refreshButton = ConfirmPanel.createRefreshButton(false);
				refreshButton.setMargin(new Insets (1, 10, 0, 10));
				refreshButton.setDefaultCapable(true);
				refreshButton.addActionListener(this);
				dialog.getConfirmPanel().addButton(refreshButton);
				dialog.getRootPane().setDefaultButton(refreshButton);
		dialog.setTitle(getTitle());

		// RMA Selection option should only be available for AP Credit Memo
		Integer docTypeId = (Integer)getGridTab().getValue("C_DocTypeTarget_ID");
		MDocType docType = MDocType.get(Env.getCtx(), docTypeId);
		if (!MDocType.DOCBASETYPE_APCreditMemo.equals(docType.getDocBaseType()))
		{
			rmaLabel.setVisible(false);
		    rmaField.setVisible(false);
		}
		
		initBPartner(true);
		bPartnerField.addVetoableChangeListener(this);
		if (wType == 5 || wType == 6)//ininoles filtros para mutual
			docNoField.addVetoableChangeListener(this);
		
		Timestamp date = Env.getContextAsDate(Env.getCtx(), p_WindowNo, "DateInvoiced");//faaguilar OFB
		dateToField.setValue(date);//faaguilar OFB
		dateToFieldAcct.setValue(date);//faaguilar OFB
		if(wType == 2)
		{
			//
			getPayments.setSelected(true);
			//nuevos filtros de pagos
			bankListLabel.setVisible(true);
			bankListField.setVisible(true);
			payTypeLabel.setVisible(true);
			payTypeField.setVisible(true);
			NPaymentLabel.setVisible(true);
			NPaymentField.setVisible(true);
			amtFromLabel.setVisible(true);
			amtFromField.setVisible(true);
			amtToLabel.setVisible(true);
			amtToField.setVisible(true);
			dateFromLabelAcct.setVisible(true);
			dateFromFieldAcct.setVisible(true);
			dateToLabelAcct.setVisible(true);
			dateToFieldAcct.setVisible(true);
		}
		else
		{
			//nuevos filtros de pagos
			bankListLabel.setVisible(false);
			bankListField.setVisible(false);
			payTypeLabel.setVisible(false);
			payTypeField.setVisible(false);
			NPaymentLabel.setVisible(false);
			NPaymentField.setVisible(false);
			amtFromLabel.setVisible(false);
			amtFromField.setVisible(false);
			amtToLabel.setVisible(false);
			amtToField.setVisible(false);
			dateFromLabelAcct.setVisible(false);
			dateFromFieldAcct.setVisible(false);
			dateToLabelAcct.setVisible(false);
			dateToFieldAcct.setVisible(false);
		}
		return true;
	}   //  dynInit
    
	/**
	 *  Static Init.
	 *  <pre>
	 *  parameterPanel
	 *      parameterBankPanel
	 *      parameterStdPanel
	 *          bPartner/order/invoice/shopment/licator Label/Field
	 *  dataPane
	 *  southPanel
	 *      confirmPanel
	 *      statusBar
	 *  </pre>
	 *  @throws Exception
	 */
    private void jbInit() throws Exception
    {
    	bPartnerLabel.setText(Msg.getElement(Env.getCtx(), "C_BPartner_ID"));
    	orderLabel.setText(Msg.getElement(Env.getCtx(), "C_Order_ID", false));
    	shipmentLabel.setText(Msg.getElement(Env.getCtx(), "M_InOut_ID", false));
    	rmaLabel.setText(Msg.translate(Env.getCtx(), "M_RMA_ID"));
    	getPayments.setText("Traer Pagos");//faaguilar OFB
    	llegadaLabel.setText("Llegada");
    	
    	
    	//faaguilar OFB begin
    	dateFromLabel.setLabelFor(dateFromField);
    	dateFromField.setToolTipText(Msg.translate(Env.getCtx(), "DateFrom"));
    	dateToLabel.setLabelFor(dateToField);
    	dateToField.setToolTipText(Msg.translate(Env.getCtx(), "DateTo"));
    	dateFromLabelAcct.setLabelFor(dateFromFieldAcct);
    	dateFromFieldAcct.setToolTipText(Msg.translate(Env.getCtx(), "DateFrom"));
    	dateToLabelAcct.setLabelFor(dateToField);
    	dateToFieldAcct.setToolTipText(Msg.translate(Env.getCtx(), "DateTo"));
    	getPayments.addActionListener(this);    	
    	//faaguilar OFB end
    	//ininoles campos dinamico mutual
    	if(wType == 5)
    		docNoLabel.setText("Pre-Factura");
    	else if (wType == 6)
    		docNoLabel.setText("Solicitud");
    	//ininoles lista de bancos
    	
    	int Reference_IDBL=DB.getSQLValue(null,"Select AD_Reference_ID from AD_Reference where upper(name)='BANCOS'");
    	String sqlValuesBank = "SELECT name FROM AD_Ref_List WHERE AD_Reference_ID = "+Reference_IDBL;
    	String sqlPayType = "select rlt.name from AD_Ref_List rl " +
    			"inner join AD_Ref_List_Trl rlt on (rl.AD_Ref_List_ID = rlt.AD_Ref_List_ID) " +
    			"where AD_Reference_ID = 214 AND rlt.ad_language = 'es_CL' AND rl.IsActive = 'Y'";
    	
    	bankListField.addItem(" ");
    	payTypeField.addItem(" ");
    	try
        {	
            PreparedStatement pstmt = DB.prepareStatement(sqlValuesBank, null);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
            	bankListField.addItem(rs.getString(1));
            }
            
            PreparedStatement pstmt2 = DB.prepareStatement(sqlPayType, null);
            ResultSet rs2 = pstmt2.executeQuery();
            while (rs2.next())
            {
            	payTypeField.addItem(rs2.getString(1));
            }
        }
    	catch (SQLException e)
        {
    		log.log(Level.SEVERE, sqlValuesBank.toString(), e);
        }   
    	amtFromField.setValue(0);    	
    	amtToField.setValue(0);    	
    	//end ininoles
    	
   	
    	CPanel parameterPanel = dialog.getParameterPanel();
    	parameterPanel.setLayout(new BorderLayout());
    	
    	CPanel parameterStdPanel = new CPanel(new GridBagLayout());
    	
    	parameterPanel.add(parameterStdPanel, BorderLayout.CENTER);

    	parameterStdPanel.add(bPartnerLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
    			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    	if (bPartnerField != null)
    		parameterStdPanel.add(bPartnerField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
    				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    	
    	if(wType==0){//faaguilar OFB
    	parameterStdPanel.add(orderLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
    			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    	parameterStdPanel.add(orderField,  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
    			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    	parameterStdPanel.add(shipmentLabel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
    			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    	parameterStdPanel.add(shipmentField,  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
    			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    	// Add RMA document selection to panel
    	parameterStdPanel.add(rmaLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
    			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    	parameterStdPanel.add(rmaField,  new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
    			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    	// OpenVia
    	if (Env.getContext(Env.getCtx(), p_WindowNo, "IsSOTrx").equals("N")) {
    		parameterStdPanel.add(llegadaLabel, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(llegadaField,  new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    	}
    	// Fin OpenVia
    	}
    	//if(wType!=2 && wType!=0)//faaguilar OFb factoring //ininoles que no aparesca en factura
    	if(wType!=0 && wType != 5 && wType != 6)
    	{
    		parameterStdPanel.add(getPayments, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    	}
    	if(wType!=0 && wType != 5 && wType != 6){//faaguilar OFb
    		parameterStdPanel.add(dateFromLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
    			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    		parameterStdPanel.add(dateFromField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
    			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    		parameterStdPanel.add(dateToLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
    			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    		parameterStdPanel.add(dateToField, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
    			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    		
    		//ininoles nuevo parametro de campo para pagos    		
    		parameterStdPanel.add(bankListLabel, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(bankListField, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	//nuevos filtros de pago
        	parameterStdPanel.add(payTypeLabel, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(payTypeField, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	parameterStdPanel.add(NPaymentLabel, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(NPaymentField, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	
        	parameterStdPanel.add(dateFromLabelAcct, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(dateFromFieldAcct, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	parameterStdPanel.add(dateToLabelAcct, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(dateToFieldAcct, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	
        	parameterStdPanel.add(amtFromLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(amtFromField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	parameterStdPanel.add(amtToLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(amtToField, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    	}//faaguilar OFb
    	if (wType == 5 || wType == 6)//ininoles filtros para mutual 
    	{
    		parameterStdPanel.add(docNoLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    		parameterStdPanel.add(docNoField, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
    				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    		
    		parameterStdPanel.add(dateFromLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(dateFromField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	parameterStdPanel.add(dateToLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        	parameterStdPanel.add(dateToField, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
        			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
        	//Filtros de empleado
    		parameterStdPanel.add(REmployeeLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    		parameterStdPanel.add(REmployeeField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
    				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    		parameterStdPanel.add(NEmployeeLabel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
        			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    		parameterStdPanel.add(NEmployeeField, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
    				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
    	}
    	
    }   //  jbInit

	/*************************************************************************/

	private boolean 	m_actionActive = false;
	
	/**
	 *  Action Listener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		log.config("Action=" + e.getActionCommand());
		
		if (m_actionActive)
			return;
		m_actionActive = true;
		log.config("Action=" + e.getActionCommand());
		//  Order
		if (e.getSource().equals(orderField))
		{
			KeyNamePair pp = (KeyNamePair)orderField.getSelectedItem();
			int C_Order_ID = 0;
			if (pp != null)
				C_Order_ID = pp.getKey();
			//  set Invoice, RMA and Shipment to Null
			rmaField.setSelectedIndex(-1);
			shipmentField.setSelectedIndex(-1);
			llegadaField.setSelectedIndex(-1);
			loadOrder(C_Order_ID, true);
		}
		//  Shipment
		else if (e.getSource().equals(shipmentField))
		{
			KeyNamePair pp = (KeyNamePair)shipmentField.getSelectedItem();
			int M_InOut_ID = 0;
			if (pp != null)
				M_InOut_ID = pp.getKey();
			//  set Order, RMA and Invoice to Null
			orderField.setSelectedIndex(-1);
			rmaField.setSelectedIndex(-1);
			loadShipment(M_InOut_ID);
		}
		//  RMA
		else if (e.getSource().equals(rmaField))
		{
		    KeyNamePair pp = (KeyNamePair)rmaField.getSelectedItem();
		    int M_RMA_ID = 0;
		    if (pp != null)
		        M_RMA_ID = pp.getKey();
		    //  set Order and Invoice to Null
		    orderField.setSelectedIndex(-1);
		    shipmentField.setSelectedIndex(-1);
		    llegadaField.setSelectedIndex(-1);
		    loadRMA(M_RMA_ID);
		}
		else if (e.getSource().equals(getPayments))//faaguilar OFB
		{
			if(getPayments.isSelected())
			{
				getPay=true;
				bankListLabel.setVisible(true);
				bankListField.setVisible(true);
				payTypeLabel.setVisible(true);
				payTypeField.setVisible(true);
				NPaymentLabel.setVisible(true);
				NPaymentField.setVisible(true);
				amtFromLabel.setVisible(true);
				amtFromField.setVisible(true);
				amtToLabel.setVisible(true);
				amtToField.setVisible(true);
				dateFromLabelAcct.setVisible(true);
				dateFromFieldAcct.setVisible(true);
				dateToLabelAcct.setVisible(true);
				dateToFieldAcct.setVisible(true);
				
			}
			else
			{
				getPay=false;
				bankListLabel.setVisible(false);
				bankListField.setVisible(false);
				payTypeLabel.setVisible(false);
				payTypeField.setVisible(false);
				NPaymentLabel.setVisible(false);
				NPaymentField.setVisible(false);
				amtFromLabel.setVisible(false);
				amtFromField.setVisible(false);
				amtToLabel.setVisible(false);
				amtToField.setVisible(false);
				dateFromLabelAcct.setVisible(false);
				dateFromFieldAcct.setVisible(false);
				dateToLabelAcct.setVisible(false);
				dateToFieldAcct.setVisible(false);
			}			
			initOFBDetails(C_BPartner_ID);
		}
		//Llegada
		else if (e.getSource().equals(llegadaField))
		{
			KeyNamePair pp = (KeyNamePair)llegadaField.getSelectedItem();
			int OV_Llegada_ID = 0;
			if (pp != null)
				OV_Llegada_ID = pp.getKey();
			//  set Order, RMA and Invoice to Null
			orderField.setSelectedIndex(-1);
			shipmentField.setSelectedIndex(-1);
			rmaField.setSelectedIndex(-1);
			loadLlegada(OV_Llegada_ID);	
		}
		
		if ( e.getActionCommand().equals(ConfirmPanel.A_REFRESH) )//faaguilar OFB
		{
			Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
			if (wType==5 || wType == 6)
				initOFBDetailsForMutual(C_BPartner_ID);
			else				
				initOFBDetails(C_BPartner_ID);
			dialog.tableChanged(null);
			Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		}
		m_actionActive = false;
	}   //  actionPerformed

	/**
	 *  Change Listener
	 *  @param e event
	 */
	public void vetoableChange (PropertyChangeEvent e)
	{
		log.config(e.getPropertyName() + "=" + e.getNewValue());

		//  BPartner - load Order/Invoice/Shipment
		if (e.getPropertyName().equals("C_BPartner_ID"))
		{
			C_BPartner_ID = ((Integer)e.getNewValue()).intValue();
			if(wType==0)
				initBPOrderDetails (C_BPartner_ID, true);
			if(wType==5 || wType == 6)
				initOFBDetailsForMutual(C_BPartner_ID);
			else 
				initOFBDetails (C_BPartner_ID);//faaguilar OFB
			
		}
		
		dialog.tableChanged(null);
	}   //  vetoableChange
	
	/**************************************************************************
	 *  Load BPartner Field
	 *  @param forInvoice true if Invoices are to be created, false receipts
	 *  @throws Exception if Lookups cannot be initialized
	 */
	protected void initBPartner (boolean forInvoice) throws Exception
	{
		//  load BPartner
		int AD_Column_ID = 3499;        //  C_Invoice.C_BPartner_ID
		MLookup lookup = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, AD_Column_ID, DisplayType.Search);
		bPartnerField = new VLookup ("C_BPartner_ID", true, false, true, lookup);
		//
		int C_BPartner_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "C_BPartner_ID");
		bPartnerField.setValue(new Integer(C_BPartner_ID));
		int Ref_Invoice_ID = 0;
		int Ref_InvoiceRequest_ID = 0;
		//nuevos campos documento mutual
		if(wType == 5)
		{
			AD_Column_ID = 10788;        //  C_Invoice.C_BPartner_ID
			MLookup lookup2 = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, AD_Column_ID, DisplayType.Search);
			docNoField = new VLookup ("Ref_Invoice_ID", true, false, true, lookup2);
			Ref_Invoice_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "Ref_Invoice_ID");
			docNoField.setValue(new Integer(Ref_Invoice_ID));
		}
		else if(wType == 6)
		{
			if (DB.isPostgreSQL())
				AD_Column_ID = 1001943;        
			else
				AD_Column_ID = 1001776;        
			MLookup lookup2 = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, AD_Column_ID, DisplayType.Search);
			docNoField = new VLookup ("Ref_InvoiceRequest_ID", true, false, true, lookup2);
			Ref_InvoiceRequest_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "Ref_InvoiceRequest_ID");
			docNoField.setValue(new Integer(Ref_InvoiceRequest_ID));
		}

		//  initial loading
		if(wType==0)//faaguilar OFB
			initBPOrderDetails(C_BPartner_ID, forInvoice);
		else if(wType==5 || wType == 6)//metodo para mutual
			initOFBDetailsForMutual(C_BPartner_ID);
		else //faaguilar OFB
			initOFBDetails(C_BPartner_ID);//faaguilar OFB
		
	}   //  initBPartner

	/**
	 *  Load PBartner dependent Order/Invoice/Shipment Field.
	 *  @param C_BPartner_ID BPartner
	 *  @param forInvoice for invoice
	 */
	protected void initBPOrderDetails (int C_BPartner_ID, boolean forInvoice)
	{
		log.config("C_BPartner_ID=" + C_BPartner_ID);
		KeyNamePair pp = new KeyNamePair(0,"");
		//  load PO Orders - Closed, Completed
		orderField.removeActionListener(this);
		orderField.removeAllItems();
		orderField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadOrderData(C_BPartner_ID, forInvoice, false);
		for(KeyNamePair knp : list)
			orderField.addItem(knp);
		
		orderField.setSelectedIndex(0);
		orderField.addActionListener(this);
		dialog.pack();

		initBPDetails(C_BPartner_ID);
	}   //  initBPartnerOIS
	
	public void initBPDetails(int C_BPartner_ID) 
	{
		initBPShipmentDetails(C_BPartner_ID);
		initBPRMADetails(C_BPartner_ID);
		initBPLlegadaDetails(C_BPartner_ID);
	}
	/**
	 * Load PBartner dependent Order/Invoice/Shipment Field.
	 * @param C_BPartner_ID
	 */
	private void initBPShipmentDetails(int C_BPartner_ID)
	{
		log.config("C_BPartner_ID: " + C_BPartner_ID);

		//  load Shipments (Receipts) - Completed, Closed
		shipmentField.removeActionListener(this);
		shipmentField.removeAllItems();
		//	None
		KeyNamePair pp = new KeyNamePair(0,"");
		shipmentField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadShipmentData(C_BPartner_ID);
		for(KeyNamePair knp : list)
			shipmentField.addItem(knp);
		
		shipmentField.setSelectedIndex(0);
		shipmentField.addActionListener(this);
	}
	
	/**
	 * Load RMA that are candidates for shipment
	 * @param C_BPartner_ID BPartner
	 */
	private void initBPRMADetails(int C_BPartner_ID)
	{
	    rmaField.removeActionListener(this);
	    rmaField.removeAllItems();
	    //  None
	    KeyNamePair pp = new KeyNamePair(0,"");
	    rmaField.addItem(pp);
	    
	    ArrayList<KeyNamePair> list = loadRMAData(C_BPartner_ID);
		for(KeyNamePair knp : list)
			rmaField.addItem(knp);
		
	    rmaField.setSelectedIndex(0);
	    rmaField.addActionListener(this);
	}
	
	private void initBPLlegadaDetails(int C_BPartner_ID)
	{
		log.config("C_BPartner_ID: " + C_BPartner_ID);

		//  load Shipments (Receipts) - Completed, Closed
		llegadaField.removeActionListener(this);
		llegadaField.removeAllItems();
		//	None
		KeyNamePair pp = new KeyNamePair(0,"");
		llegadaField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadLlegadaData(C_BPartner_ID);
		for(KeyNamePair knp : list)
			llegadaField.addItem(knp);
		
		llegadaField.setSelectedIndex(0);
		llegadaField.addActionListener(this);
	}

	/**
	 *  Load Data - Order
	 *  @param C_Order_ID Order
	 *  @param forInvoice true if for invoice vs. delivery qty
	 */
	protected void loadOrder (int C_Order_ID, boolean forInvoice)
	{
		loadTableOIS(getOrderData(C_Order_ID, forInvoice));
	}   //  LoadOrder
	
	protected void loadRMA (int M_RMA_ID)
	{
		loadTableOIS(getRMAData(M_RMA_ID));
	}
	
	protected void loadShipment (int M_InOut_ID)
	{
		loadTableOIS(getShipmentData(M_InOut_ID));
	}
	
	protected void loadLlegada (int OV_Llegada_ID)
	{
		loadTableOIS(getLlegadaData(OV_Llegada_ID));
	}
	
	/**
	 *  Load Order/Invoice/Shipment data into Table
	 *  @param data data
	 */
	protected void loadTableOIS (Vector<?> data)
	{
		//  Remove previous listeners
		dialog.getMiniTable().getModel().removeTableModelListener(dialog);
		//  Set Model
		DefaultTableModel model = new DefaultTableModel(data, getOISColumnNames());
		model.addTableModelListener(dialog);
		dialog.getMiniTable().setModel(model);
		// 
		
		configureMiniTable(dialog.getMiniTable());
	}   //  loadOrder
	
	public void showWindow()
	{
		dialog.setVisible(true);
	}
	
	public void closeWindow()
	{
		dialog.dispose();
	}
	

	/**
	 * faaguilar OFB custom methods
	 *  Load for Factoring Documents
	 *  @param C_BPartner_ID BPartner
	 *  @param forInvoice for invoice false for Payments
	 */
	protected void initOFBDetails (int C_BPartner_ID)
	{
		log.config("C_BPartner_ID=" + C_BPartner_ID);
		boolean PutInFactoring= "Y".equals(Env.getContext(Env.getCtx(), p_WindowNo, "PutInFactoring"));
		int C_Currency_ID=Env.getContextAsInt(Env.getCtx(), p_WindowNo, "C_Currency_ID");
		int Reference_ID=DB.getSQLValue(null,"Select AD_Reference_ID from AD_Reference where upper(name)='BANCOS'");
		extinguir= "Y".equals(Env.getContext(Env.getCtx(), p_WindowNo, "Extinguir"));
		boolean IsSOTrx = "Y".equals(Env.getContext(Env.getCtx(), p_WindowNo, "IsSOTrx"));

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();

               StringBuffer sql= new StringBuffer();;
               //if(!getPay && wType!=2) ininoles se saca validacion para que no aparescan facturas en protesto
               if(!getPay)
               {
            	   sql.append("SELECT p.DateInvoiced,p.C_invoice_ID,p.DocumentNo,p.C_Currency_ID, ");
                   sql.append("invoiceOpen(p.C_Invoice_ID,p.C_InvoicePaySchedule_ID),");
            	   sql.append( " bp.Name,org.name as OrgName, doc.name as Tipo, COALESCE(pter.name,pter2.name) as pterName ");            	   
            	   sql.append( " FROM C_Invoice_v p");
            	   sql.append( " INNER JOIN AD_Org org ON (p.AD_Org_ID=org.AD_Org_ID)");
            		sql.append( " INNER JOIN C_BPartner bp ON (p.C_BPartner_ID=bp.C_BPartner_ID) ");
            		sql.append( " Inner Join C_DocType doc ON (P.C_DocType_ID=doc.C_DocType_ID) ");
            		sql.append( " LEFT Join C_PaymentTerm pter ON (P.C_PaymentTerm2_ID=pter.C_PaymentTerm_ID) ");
            		sql.append( " LEFT Join C_PaymentTerm pter2 ON (P.C_PaymentTerm_ID=pter2.C_PaymentTerm_ID) ");
            		sql.append( " WHERE p.Processed='Y' AND p.DocStatus IN ('CO','CL')  and p.AD_Client_ID="+Env.getAD_Client_ID(Env.getCtx()));
            		sql.append( " And p.C_Currency_ID="+C_Currency_ID);
                
            	if(wType==1){//factoring
	               if(!PutInFactoring)
	            	   sql.append(" AND p.ISFACTORING='Y' And p.Extinta='N'");
	               else
	            	   sql.append( " AND p.ISFACTORING='N' and p.ispaid='N' ");
            	}
            	if(wType==3)//CDC
            		//ininoles se agrega protesto para cambio de documento
            		sql.append(" and doc.IsSOTrx='Y' AND doc.docbasetype IN ('ARI','ARC','CDC','PTK')  AND p.ispaid <> 'Y' ");
            	if(wType==4)//VDC
            		sql.append(" and doc.IsSOTrx='N' AND doc.docbasetype IN ('API','APC') ");
            	
                if(C_BPartner_ID>0)
            	   sql.append( " AND p.C_Bpartner_ID=" + C_BPartner_ID);
               
                if (dateFromField.getValue() != null || dateToField.getValue() != null)
	       		{
	       			Timestamp from = (Timestamp) dateFromField.getValue();
	       			Timestamp to = (Timestamp) dateToField.getValue();
	       			if (from == null && to != null)
	       				sql.append(" AND TRUNC(p.DateInvoiced) <= ?");
	       			else if (from != null && to == null)
	       				sql.append(" AND TRUNC(p.DateInvoiced) >= ?");
	       			else if (from != null && to != null)
	       				sql.append(" AND TRUNC(p.DateInvoiced) BETWEEN ? AND ?");
	       		}
               }
               else{
            	   
            	   sql.append( "SELECT p.DateTrx,p.C_Payment_ID,p.DocumentNo, p.C_Currency_ID,c.ISO_Code, p.PayAmt,");
            			   sql.append("currencyConvert(p.PayAmt,p.C_Currency_ID,ba.C_Currency_ID,?,null,p.AD_Client_ID,p.AD_Org_ID),");   //  #1
            		if(Reference_ID>0)
            		{
            			if(DB.isOracle())
            				sql.append( " bp.value||'-'||bp.name as Name,p.dateacct," +
            						"(SELECT NAME FROM AD_Ref_List_Trl arlt WHERE AD_Ref_List_ID=l2.AD_Ref_List_ID AND AD_Language='es_CL') as Tender ," +
            						"l.name, COALESCE(p.checkno,n'') as checkno ");
            			else
            				sql.append( " bp.value||'-'||bp.name as Name,p.dateacct," +
            						"(SELECT NAME FROM AD_Ref_List_Trl arlt WHERE AD_Ref_List_ID=l2.AD_Ref_List_ID AND AD_Language='es_CL') as Tender ," +
            						"l.name, COALESCE(p.checkno,'') as checkno ");
            		}
            		else
            		{
            			if(DB.isOracle())
            				sql.append( " bp.value||'-'||bp.name as Name,p.dateacct," +
            						"(SELECT NAME FROM AD_Ref_List_Trl arlt WHERE AD_Ref_List_ID=l2.AD_Ref_List_ID AND AD_Language='es_CL') as Tender, " +
            						"COALESCE(p.checkno,n'') as checkno ");
            			else
            				sql.append( " bp.value||'-'||bp.name as Name,p.dateacct," +
            						"(SELECT NAME FROM AD_Ref_List_Trl arlt WHERE AD_Ref_List_ID=l2.AD_Ref_List_ID AND AD_Language='es_CL') as Tender, " +
            						"COALESCE(p.checkno,'') as checkno ");
            		}
           			sql.append( "FROM C_BankAccount ba");
           			sql.append( " INNER JOIN C_Payment_v p ON (p.C_BankAccount_ID=ba.C_BankAccount_ID)");
           			sql.append( " INNER JOIN C_Currency c ON (p.C_Currency_ID=c.C_Currency_ID)");
           			sql.append( " LEFT OUTER JOIN C_BPartner bp ON (p.C_BPartner_ID=bp.C_BPartner_ID) ");
           		if(Reference_ID>0)//bank list faaguilar
           			sql.append(" LEFT OUTER JOIN AD_Ref_List l ON (p.Banco=l.value and l.AD_Reference_ID="+Reference_ID+") ");
           		    
           			sql.append(" LEFT OUTER JOIN AD_Ref_List l2 ON (p.TenderType=l2.value and l2.AD_Reference_ID=214) ");
           			//se agrega nuevo tender type para geminis Z cheque a cliente ininoles. se agregan todos los tender type
           			if(wType==3)//CDC
           			{
           				sql.append( "WHERE p.Processed='Y' AND p.TenderType IN ('L','K','I','P','Z','A','C','D','T','V','X') ");
           			}
           			else
           			{
           				sql.append( "WHERE p.Processed='Y' AND p.TenderType IN ('L','K','I','P','Z') ");
           			}
           			sql.append( " AND p.DocStatus IN ('CO','CL') AND p.PayAmt<>0" );
           			sql.append( " AND p.AD_Client_ID="  + Env.getAD_Client_ID(Env.getCtx()) + " AND p.C_Currency_ID="+C_Currency_ID);
           			//ininoles sacamos tipo de protesto
           			
           			
           			
           			if(wType==2)//protesto
           			{
           				/* ininoles se revisan con don alberto las condiciones y se genera nuevo codigo
           				if (valuePTK.compareToIgnoreCase("NB")==0)
           				{
           					sql.append(" AND p.isreceipt='"+(IsSOTrx?"Y":"N") +"' AND p.IsProtested='N'" +
    	           					" AND ( p.IsReconciled='N' OR custodio = 'D' OR custodio = 'B' " +
               							  ") "); //ININOLES se agrega validacion nuevo custodio.
           				}else if(valuePTK.compareToIgnoreCase("BA")==0) 
           				{
           					sql.append(" AND p.isreceipt='"+(IsSOTrx?"Y":"N") +"' AND p.IsProtested='N'" +
    	           					" AND ( p.TenderType IN ('K','P','Z') and p.IsReconciled='Y' " + //se agrega nuevo tender type para geminis Z cheque a cliente ininoles
               							" ) "); //ININOLES se agrega validacion nuevo custodio.
           				}*/
           		        if (valuePTK.compareToIgnoreCase("NB")==0)
                        {
           		        	sql.append(" AND p.isreceipt='"+(IsSOTrx?"Y":"N") +"' AND p.IsProtested='N'" +
                                    " AND p.IsReconciled='N' "+
                                    " AND (custodio = 'A' OR custodio is null)");
                        }
           		        else if(valuePTK.compareToIgnoreCase("BA")==0) 
           				{
           		        	sql.append(" AND p.isreceipt='"+(IsSOTrx?"Y":"N") +"' AND p.IsProtested='N'" +
                                    " AND p.TenderType IN ('K','P','Z') AND " +
                                    " (p.IsReconciled='Y' OR (p.IsReconciled='N' AND custodio IN ('B','D')))");
           				}           				
           				else
           				{
           					sql.append(" AND p.isreceipt='"+(IsSOTrx?"Y":"N") +"' AND p.IsProtested='N'" +
	           					" AND ( (p.TenderType IN ('K','P','Z') and p.IsReconciled='Y') OR " + //se agrega nuevo tender type para geminis Z cheque a cliente ininoles
	           							"(p.TenderType NOT IN ('K','P') and p.IsReconciled='N') OR " +
           								"(custodio = 'D')  ) "); //ININOLES se agrega validacion nuevo custodio.
           				}
           			}
           			if(wType==1)//factoring
           				sql.append( " AND p.isreceipt='Y' AND p.IsProtested='N' AND p.IsReconciled='N' ");
           			
	           		if(!PutInFactoring && wType==1)
	           			sql.append(" AND p.Custodio='F' ");
	           		if(PutInFactoring && wType==1)
	           			sql.append(" AND (p.Custodio is null OR p.Custodio IN ('A')) ");
	     			/*else if(wType==2)//protesto. Custodio es nulo o devuelto por factoring ininoles
	     				sql.append(" AND (custodio is null OR custodio IN ('D','A')) ");
	     			else
	     				sql.append(" AND (p.Custodio is null OR p.Custodio IN ('A')) ");
	           		 */
	           		if(wType==3)//CDC
	           		{
	            		sql.append(" and p.isreceipt='Y' AND p.IsReconciled='N' AND p.IsProtested='N'");
	            		sql.append(" AND (p.Custodio is null OR p.Custodio IN ('A')) ");
	           		}
	            	if(wType==4)//VDC
	            		sql.append(" and p.isreceipt='N' AND p.IsReconciled='N' AND p.IsProtested='N'");
	           		 
	           		if(C_BPartner_ID>0)
	           			sql.append(" AND p.C_Bpartner_ID = " + C_BPartner_ID);
	           		
	           		if (dateFromField.getValue() != null || dateToField.getValue() != null)
		       		{
		       			Timestamp from = (Timestamp) dateFromField.getValue();
		       			Timestamp to = (Timestamp) dateToField.getValue();
		       			if (from == null && to != null)
		       				sql.append(" AND TRUNC(p.DateTrx) <= ?");
		       			else if (from != null && to == null)
		       				sql.append(" AND TRUNC(p.DateTrx) >= ?");
		       			else if (from != null && to != null)
		       				sql.append(" AND TRUNC(p.DateTrx) BETWEEN ? AND ?");
		       		}
	           		//ininoles se agregan filtros de fecha contable
	           		if (getPay)
		                if (dateFromFieldAcct.getValue() != null || dateToFieldAcct.getValue() != null)
			       		{
			       			Timestamp fromAcct = (Timestamp) dateFromFieldAcct.getValue();
			       			Timestamp toAcct = (Timestamp) dateToFieldAcct.getValue();
			       			if (fromAcct == null && toAcct != null)
			       				sql.append(" AND TRUNC(p.DateAcct) <= ?");
			       			else if (fromAcct != null && toAcct == null)
			       				sql.append(" AND TRUNC(p.DateAcct) >= ?");
			       			else if (fromAcct != null && toAcct != null)
			       				sql.append(" AND TRUNC(p.DateAcct) BETWEEN ? AND ?");
			       		}
	           		//filtro bancos
	           		if (bankListField.getSelectedItem() != null && bankListField.getSelectedItem() != "" && 
	             		   bankListField.getSelectedItem() != " ")
	                {
	             	   sql.append(" AND l.name like '"+bankListField.getSelectedItem().toString()+"'");
	                }
	           		//filtros tipo de pago
	           		if (payTypeField.getSelectedItem() != null && payTypeField.getSelectedItem() != "" && 
	           				payTypeField.getSelectedItem() != " ")
		            {
		           		String sqlValPayType = "select MAX(rl.value) from AD_Ref_List rl " +
		           				"inner join AD_Ref_List_Trl rlt on (rl.AD_Ref_List_ID = rlt.AD_Ref_List_ID) " +
		           				"where AD_Reference_ID = 214 AND rlt.ad_language = 'es_CL' AND rl.IsActive = 'Y'" +
		           				"AND rlt.name like '"+payTypeField.getSelectedItem().toString()+"'";
		           		String valpayType = DB.getSQLValueString(null, sqlValPayType);
		           		
		               sql.append(" AND l2.value like '"+valpayType+"'");
		            }
	           		//filtro N° documento
	           		if (NPaymentField.getText() != null && NPaymentField.getText() != "" && 
	           				NPaymentField.getText() != " ")
	                {
	             	   sql.append(" AND p.DocumentNo like '%"+NPaymentField.getText()+"%'");
	                }
	           		//filtro monto
	           		BigDecimal amtFrom = (BigDecimal)amtFromField.getValue();
	           		BigDecimal amtTo = (BigDecimal)amtToField.getValue();
	           		
	           		if (amtFrom == null)
	           			amtFrom = Env.ZERO;
	           		if (amtTo == null)
	           			amtTo = Env.ZERO;
	           		
	           		if (amtFrom.compareTo(Env.ZERO) > 0)
	                {
	             	   sql.append(" AND p.payAmt > "+amtFrom);
	                }
	           		if (amtTo.compareTo(Env.ZERO) > 0)
	                {
	             	   sql.append(" AND p.payAmt < "+amtTo);
	                }	           		
               }
               log.config ("**"+sql.toString());
               Timestamp ts = new Timestamp(System.currentTimeMillis());
               try
               {
            	       int index=1;
                       PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
                       if(getPay)
                    	   pstmt.setTimestamp(index++, ts);
                       if (dateFromField.getValue() != null || dateToField.getValue() != null)
	       	       		{
	       	       			Timestamp from = (Timestamp) dateFromField.getValue();
	       	       			Timestamp to = (Timestamp) dateToField.getValue();
		               			log.fine("Date From=" + from + ", To=" + to);
		               			if (from == null && to != null)
		               				pstmt.setTimestamp(index++, to);
		               			else if (from != null && to == null)
		               				pstmt.setTimestamp(index++, from);
		               			else if (from != null && to != null)
		               			{
		               				pstmt.setTimestamp(index++, from);
		               				pstmt.setTimestamp(index++, to);
		               			}
		               	}
                       if(getPay)
	                       if (dateFromFieldAcct.getValue() != null || dateToFieldAcct.getValue() != null)
		       	       	   {
		       	       			Timestamp fromAcct = (Timestamp) dateFromFieldAcct.getValue();
		       	       			Timestamp toAcct = (Timestamp) dateToFieldAcct.getValue();
			               			if (fromAcct == null && toAcct != null)
			               				pstmt.setTimestamp(index++, toAcct);
			               			else if (fromAcct != null && toAcct == null)
			               				pstmt.setTimestamp(index++, fromAcct);
			               			else if (fromAcct != null && toAcct != null)
			               			{
			               				pstmt.setTimestamp(index++, fromAcct);
			               				pstmt.setTimestamp(index++, toAcct);
			               			}
			               }
                       ResultSet rs = pstmt.executeQuery();
                       while (rs.next())
                       {
                               Vector<Object> line = new Vector<Object>(10);
                               if(!getPay)
                               {
		               			   line.add(new Boolean(false));       //  0-Selection
	                               line.add(rs.getString(3));     //1- docno
	                               KeyNamePair pp = new KeyNamePair(rs.getInt(2), rs.getString(3));
	                               line.add(pp);                       //  2--C_invoice_ID
	                               line.add(rs.getBigDecimal(5));      //  3-grandtotal
	                               line.add(rs.getTimestamp(1));       //  4-DateTrx
	                               line.add(rs.getString(6));              //  5-BParner
	                               line.add(rs.getString(7));                      // 6- Org
	                               line.add(rs.getString(8));                      // 7-docType
	                               line.add(rs.getString(9));                      // 8-Termino de pago
                               }
                               else
                               {
                   				line.add(new Boolean(false));       //  0-Selection
                   				line.add(rs.getTimestamp(1));       //  1-DateTrx
                   				
                   				KeyNamePair pp = new KeyNamePair(rs.getInt(2), rs.getString(3));
                   				line.add(pp);                       //  2-C_Payment_ID
                   				if(Reference_ID>0)
                   					line.add(rs.getString(12));      	//  8-Numero de Cheque
                   				else
                   					line.add(rs.getString(11));      	//  8-Numero de Cheque
                   				pp = new KeyNamePair(rs.getInt(4), rs.getString(5));
                   				line.add(pp);                       //  3-Currency
                   				line.add(rs.getBigDecimal(6));      //  4-PayAmt
                   				line.add(rs.getBigDecimal(7));      //  5-Conv Amt
                   				line.add(rs.getString(8));      	//  6-BParner
                   				line.add(rs.getString(10));      	//  7-Tender
                   				if(Reference_ID>0)                   				
                   					line.add(rs.getString(11));      	//  8-Banco                   				
                   				line.add(rs.getTimestamp(9));       //  9-DateAcct
                               }
                               data.add(line);
                       }
                       rs.close();
                       pstmt.close();
               }
               catch (SQLException e)
               {
                       log.log(Level.SEVERE, sql.toString(), e);
               }   
               
               Vector<String> columnNames = new Vector<String>(10);
               if(!getPay)
               {
	               columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
	               columnNames.add(Msg.getElement(Env.getCtx(), "DocumentNo"));
	               columnNames.add(Msg.getElement(Env.getCtx(), "C_Invoice_ID"));
	               columnNames.add(Msg.translate(Env.getCtx(), "Amount"));
	               columnNames.add(Msg.translate(Env.getCtx(), "Date"));
	               columnNames.add(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
	               columnNames.add(Msg.translate(Env.getCtx(), "AD_Org_ID"));
	               columnNames.add("Tipo");
	               columnNames.add("Termino de Pago");
               }
               else{
            	columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
           		columnNames.add(Msg.translate(Env.getCtx(), "Date"));
           		columnNames.add(Msg.getElement(Env.getCtx(), "C_Payment_ID"));           		
           		columnNames.add(Msg.translate(Env.getCtx(), "N° Cheque"));
           		columnNames.add(Msg.translate(Env.getCtx(), "C_Currency_ID"));
           		columnNames.add(Msg.translate(Env.getCtx(), "Amount"));
           		columnNames.add(Msg.translate(Env.getCtx(), "ConvertedAmount"));
           		columnNames.add(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
           		columnNames.add(Msg.translate(Env.getCtx(), "TenderType"));
           		if(Reference_ID>0)
           			columnNames.add(Msg.translate(Env.getCtx(), "Banco"));
           		columnNames.add(Msg.translate(Env.getCtx(), "DateAcct"));
               }
		dialog.getMiniTable().getModel().removeTableModelListener(dialog);
		//  Set Model
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		model.addTableModelListener(dialog);
		dialog.getMiniTable().setModel(model);
		// 
		
		if(!getPay)
        {
			dialog.getMiniTable().setColumnClass(0, Boolean.class, false);      //  0-Selection
			dialog.getMiniTable().setColumnClass(1, String.class, true);                //  1-DocumentNo
			dialog.getMiniTable().setColumnClass(2, String.class, true);        //  2-Invoice
			dialog.getMiniTable().setColumnClass(3, BigDecimal.class, true);    //  3-Amount
			dialog.getMiniTable().setColumnClass(4, Timestamp.class, true);     //  4-TrxDate
			dialog.getMiniTable().setColumnClass(5, String.class, true);        //  5-BPartner
			dialog.getMiniTable().setColumnClass(6, String.class, true);        //  6-Org
			dialog.getMiniTable().setColumnClass(7, String.class, true);        //  7-Tipo
			dialog.getMiniTable().setColumnClass(8, String.class, true);        //  8-termino de pago
        }
		else
		{
			dialog.getMiniTable().setColumnClass(0, Boolean.class, false);      //  0-Selection
			dialog.getMiniTable().setColumnClass(1, Timestamp.class, true);     //  1-TrxDate
			dialog.getMiniTable().setColumnClass(2, String.class, true);        //  2-Payment
			if (Reference_ID>0)
			{
				dialog.getMiniTable().setColumnClass(3, String.class, true);    	//  3-N° de Cheque
				dialog.getMiniTable().setColumnClass(4, String.class, true);        //  4-Currency
				dialog.getMiniTable().setColumnClass(5, BigDecimal.class, true);    //  5-Amount
				dialog.getMiniTable().setColumnClass(6, BigDecimal.class, true);    //  6-ConvAmount
				dialog.getMiniTable().setColumnClass(7, String.class, true);    	//  7-BPartner
				dialog.getMiniTable().setColumnClass(8, String.class, true);    	//  8-Tender
				dialog.getMiniTable().setColumnClass(9, String.class, true);    	//  9-Banco
				dialog.getMiniTable().setColumnClass(10, Timestamp.class, true);     //  10-DateAcct
			}
			else
			{
				dialog.getMiniTable().setColumnClass(3, String.class, true);    	//  3-N° de Cheque
				dialog.getMiniTable().setColumnClass(4, String.class, true);        //  4-Currency
				dialog.getMiniTable().setColumnClass(5, BigDecimal.class, true);    //  5-Amount
				dialog.getMiniTable().setColumnClass(6, BigDecimal.class, true);    //  6-ConvAmount
				dialog.getMiniTable().setColumnClass(7, String.class, true);    	//  7-BPartner
				dialog.getMiniTable().setColumnClass(8, String.class, true);    	//  8-Tender
				dialog.getMiniTable().setColumnClass(9, Timestamp.class, true);     //  9-DateAcct
			}
			
		}
	}   //  initBPartnerOIS
	
	protected void initOFBDetailsForMutual (int C_BPartner_ID)
	{
		
		log.config("C_BPartner_ID=" + C_BPartner_ID);
		int C_Currency_ID=Env.getContextAsInt(Env.getCtx(), p_WindowNo, "C_Currency_ID");
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();

		StringBuffer sql= new StringBuffer();;
               
		sql.append( "SELECT p.DateInvoiced,pl.C_invoiceLine_ID,pl.line, ");
		sql.append(	" p.DocumentNo, p.C_Currency_ID, ");
		sql.append( " bp.Name,org.name as OrgName,doc.name,pl.m_product_id,pro.name, ");
		sql.append( " pl.C_Charge_id,cc.name,pl.linetotalamt,p.Ref_Invoice_ID,p.Ref_InvoiceRequest_ID, ");
		sql.append( " (select value from m_attributeinstance ai where m_attributesetinstance_ID = pl.m_attributesetinstance_ID ");
		sql.append( " AND m_attribute_id = 1000009) as rut_trabajador ,");
		sql.append( " (select value from m_attributeinstance ai ");
		sql.append( " where m_attributesetinstance_ID = pl.m_attributesetinstance_ID AND m_attribute_id = 1000010) ||' '|| ");
		sql.append( "(select value from m_attributeinstance ai ");
		sql.append( " where m_attributesetinstance_ID = pl.m_attributesetinstance_ID AND m_attribute_id = 1000011) ||' '|| ");
		sql.append( "(select value from m_attributeinstance ai ");
		sql.append( " where m_attributesetinstance_ID = pl.m_attributesetinstance_ID AND m_attribute_id = 1000012) as nombre_trabajador ");
		sql.append( " FROM C_Invoice p");
		sql.append( " INNER JOIN C_InvoiceLine pl ON (p.C_Invoice_ID = pl.C_Invoice_ID)");
		sql.append( " INNER JOIN AD_Org org ON (p.AD_Org_ID=org.AD_Org_ID)");
		sql.append( " INNER JOIN C_BPartner bp ON (p.C_BPartner_ID=bp.C_BPartner_ID)"); 
		sql.append( " Inner Join C_DocType doc ON (P.C_DocType_ID=doc.C_DocType_ID)"); 
		sql.append( " LEFT JOIN M_Product pro ON (pl.M_Product_ID = pro.M_Product_ID)");
		sql.append( " LEFT JOIN C_Charge cc ON (pl.C_Charge_ID = cc.C_Charge_ID)");		
		sql.append( " WHERE p.Processed='Y' AND p.DocStatus IN ('CO','CL')  and p.AD_Client_ID ="+Env.getAD_Client_ID(Env.getCtx()));
		sql.append( " AND IsUseMutualSP = 'N' And p.C_Currency_ID="+C_Currency_ID);
		if(C_BPartner_ID>0)
 		   sql.append( " AND p.C_Bpartner_ID=" + C_BPartner_ID);
		if(docNoField != null)
			sql.append( " AND p.C_Invoice_ID=" + docNoField.getValue());
		if (dateFromField.getValue() != null || dateToField.getValue() != null)
			{
   			Timestamp from = (Timestamp) dateFromField.getValue();
   			Timestamp to = (Timestamp) dateToField.getValue();
   			if (from == null && to != null)
   				sql.append(" AND TRUNC(p.DateInvoiced) <= ?");
   			else if (from != null && to == null)
   				sql.append(" AND TRUNC(p.DateInvoiced) >= ?");
   			else if (from != null && to != null)
   				sql.append(" AND TRUNC(p.DateInvoiced) BETWEEN ? AND ?");
   		}
		if (REmployeeField.getText() != null && REmployeeField.getText().trim() != "" 
			&& REmployeeField.getText().trim() != " " && REmployeeField.getText().length() > 0)
		{
			sql.append(" AND (select value from m_attributeinstance ai where m_attributesetinstance_ID = pl.m_attributesetinstance_ID " +
					" AND m_attribute_id = 1000009) like '%"+REmployeeField.getText()+"%'");
		}
		if (NEmployeeField.getText() != null && NEmployeeField.getText().trim() != "" 
			&& NEmployeeField.getText().trim() != " " && NEmployeeField.getText().length() > 0)
		{
			sql.append(" AND (select value from m_attributeinstance ai where m_attributesetinstance_ID = pl.m_attributesetinstance_ID " +
					" AND m_attribute_id = 1000010) like '%"+NEmployeeField.getText()+"%'");
		}
		
		sql.append( " ORDER BY bp.C_BPartner_ID, p.DocumentNo ");
		   
		log.config ("**"+sql.toString());
		//Timestamp ts = new Timestamp(System.currentTimeMillis());
		try
		{
			int index=1;
            PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
            if (dateFromField.getValue() != null || dateToField.getValue() != null)
	       	{
            	Timestamp from = (Timestamp) dateFromField.getValue();
	       	    Timestamp to = (Timestamp) dateToField.getValue();
		        log.fine("Date From=" + from + ", To=" + to);
		        if (from == null && to != null)
		        	pstmt.setTimestamp(index++, to);
		        else if (from != null && to == null)
		        	pstmt.setTimestamp(index++, from);
		        else if (from != null && to != null)
		        {
		        	pstmt.setTimestamp(index++, from);
		            pstmt.setTimestamp(index++, to);
		        }
	       	}
                       
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
            	Vector<Object> line = new Vector<Object>(10);
           		line.add(new Boolean(false));       //  0- Selection
           		line.add(rs.getString(4));     		//	1- docno           		
           		line.add(rs.getString(6));          //  2- BParnername           		
           		line.add(rs.getString(16));     	//	3- rut empleado
           		line.add(rs.getString(17));     	//	4- nombre empleado
           		KeyNamePair pprod = new KeyNamePair(rs.getInt(9), rs.getString(10));           		
           		line.add(pprod);                  	//  5--M_product_ID
           		KeyNamePair pp = new KeyNamePair(rs.getInt(2), rs.getString(3));           		
           		line.add(pp);                       //  6--C_invoiceLine_ID
           		line.add(rs.getString(7));          // 	7- Org
           		line.add(rs.getBigDecimal(13));     //  8- linetotalamt
           		line.add(rs.getTimestamp(1));       //  9-DateTrx
           		line.add(rs.getString(8));          //  10-docType
           		KeyNamePair pch = new KeyNamePair(rs.getInt(11), rs.getString(12));           		
           		line.add(pch);                  	//  11--C_Charge_ID
           		           		

           	
            	data.add(line);
            }
            rs.close();
            pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}   
		
		Vector<String> columnNames = new Vector<String>(10);
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add(Msg.getElement(Env.getCtx(), "DocumentNo"));
		columnNames.add("Cliente");
		columnNames.add("Rut Empleado");
		columnNames.add("Empleado");
		columnNames.add(Msg.translate(Env.getCtx(), "Exámen"));
		columnNames.add(Msg.translate(Env.getCtx(), "Line"));
		columnNames.add(Msg.translate(Env.getCtx(), "Centro de Atención"));
		columnNames.add(Msg.translate(Env.getCtx(), "Total"));
		columnNames.add(Msg.translate(Env.getCtx(), "DateDoc"));		
		columnNames.add(Msg.translate(Env.getCtx(), "C_DocType_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "Cargo"));
		
		dialog.getMiniTable().getModel().removeTableModelListener(dialog);
		//  Set Model
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		model.addTableModelListener(dialog);
		dialog.getMiniTable().setModel(model);
		// 
		
		dialog.getMiniTable().setColumnClass(0, Boolean.class, false);      //  0-Selection
		dialog.getMiniTable().setColumnClass(1, String.class, true);        //  1-DocumentNo
		dialog.getMiniTable().setColumnClass(2, String.class, true);        //  2-Bpartner
		dialog.getMiniTable().setColumnClass(3, String.class, true);    	//  3-Rut Empleado
		dialog.getMiniTable().setColumnClass(4, String.class, true);     	//  4-Nombre Empleado
		dialog.getMiniTable().setColumnClass(5, String.class, true);        //  5-Exámen
		dialog.getMiniTable().setColumnClass(6, String.class, true);        //  6-Linea
		dialog.getMiniTable().setColumnClass(7, String.class, true);        //  7-Centro de atencion
		dialog.getMiniTable().setColumnClass(8, BigDecimal.class, true);    //  8-total
		dialog.getMiniTable().setColumnClass(9, Timestamp.class, true);     //  9-Datedoc
		dialog.getMiniTable().setColumnClass(9, String.class, true);        //  10-tipo de documento
		dialog.getMiniTable().setColumnClass(9, String.class, true);        //  11-Cargo
    }   //  initBPartnerOIS
}



