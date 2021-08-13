package org.openvia.process;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MCierreComex;
import org.compiere.model.MCierreComexLine;
import org.compiere.model.MClient;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MProcess;
import org.compiere.model.MProduct;
import org.compiere.model.MQuery;
import org.compiere.model.PrintInfo;
import org.compiere.model.X_C_Invoice;
import org.compiere.model.X_OV_CierreComexLine;
import org.compiere.print.MPrintFormat;
import org.compiere.print.MPrintFormatProcess;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.report.MReport;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Ini;
import org.compiere.util.Language;
import org.openvia.inacatalog.I_iPedidos;

public class ActualizaCostos extends SvrProcess {
	
	private int 	p_C_Order_ID = 0;
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 1000000;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 1000000;
	
	private int p_C_AcctSchema_ID = 1000000;
	private int p_M_CostType_ID = 1000000;
	private int p_M_CostElement_ID = 1000000;
	private int p_M_PriceList_Version_ID = 1000001; // Compras
	private int p_AD_PrintFormat_ID = 1001794; // Cierre Comex PRD
	//private int p_AD_PrintFormat_ID = 1001703; // Cierre Comex QAS
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = 1000000;
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = 1000000;
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_C_Order_ID = getRecord_ID();
	}	//	prepare

	@Override
	protected String doIt() throws Exception {
		MOrder order = new MOrder(Env.getCtx(), p_C_Order_ID, null);
		
		if (p_C_Order_ID == 0)
			throw new IllegalArgumentException("No OCM");
		
		int cantLineas = DB.getSQLValue(getName(), "SELECT COUNT(*) FROM OV_CierreComexLine WHERE C_Order_ID = " + p_C_Order_ID);
		if (cantLineas == 0)
			throw new AdempiereException("Orden de Compra sin lineas Comex.");
		String ret = "";
		log.info("C_Order_ID=" + p_C_Order_ID);
		
		// Actualizar Costos
		String sql = "SELECT OV_CierreComexLine_ID"
				+ " FROM OV_CierreComexLine"
				+ " WHERE C_Order_ID = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, p_C_Order_ID);
			rs = pstmt.executeQuery();		
			while(rs.next()){
				MCierreComexLine line = new MCierreComexLine(getCtx(), rs.getInt("OV_CierreComexLine_ID"), get_TrxName());
				String sqlUpd = "UPDATE M_Cost"
						+ " SET CurrentCostPrice = " + line.getNewCost()
						+ " WHERE M_Product_ID = " + line.getM_Product_ID()
						+ " AND AD_Org_ID=0"
						+ " AND C_AcctSchema_ID=" + p_C_AcctSchema_ID
						+ " AND M_CostType_ID=" + p_M_CostType_ID
						+ " AND M_CostElement_ID=" + p_M_CostElement_ID;
				int upd = DB.executeUpdate(sqlUpd, get_TrxName());
				if (upd == -1)
					ret = "Error al actualizar costo (" + MProduct.get(getCtx(), line.getM_Product_ID()).getName() + ").\n";
				
				sqlUpd = "UPDATE M_ProductPrice"
						+ " SET PRICELIST = " + line.getNewCost()
						+ " , PRICESTD = " + line.getNewCost()
						+ " , PRICELIMIT = " + line.getNewCost()
						+ " WHERE M_Product_ID = " + line.getM_Product_ID()
						+ " AND M_PriceList_Version_ID=" + p_M_PriceList_Version_ID;
				upd = DB.executeUpdate(sqlUpd, get_TrxName());
				if (upd == -1)
					ret = "Error al actualizar precio de compra (" + MProduct.get(getCtx(), line.getM_Product_ID()).getName() + ").\n";
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, sql, e);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//
			
		if (ret.length() > 0) {
			throw new AdempiereException(ret);
		} else {
			MClient M_Client = new MClient(Env.getCtx(),get_TrxName());
			/*EMail email = M_Client.createEMail("icastroruz@gmail.com", "Informe Cierre Comex " + new Timestamp(System.currentTimeMillis()),"Adjunto informe cierre comex", true);
			email.addCc("raranda@comten.cl");*/
			EMail email = M_Client.createEMail("agalemiri@comercialwindsor.cl", "Informe Cierre Comex " + new Timestamp(System.currentTimeMillis()),"Adjunto informe cierre comex", true);
			email.addCc("ychavez@comercialwindsor.cl");
			email.addCc("csalvo@comercialwindsor.cl");
			email.addCc("itroncoso@comercialwindsor.cl");

			MPrintFormat format = null;
			Language language = Language.getLoginLanguage();
			
			String DocumentNo = order.getDocumentNo();
			String documentDir = System.getProperty("user.dir"); //client.getDocumentDir();
			
			MQuery query = new MQuery("OV_CierreComexLine");
			query.addRestriction("C_Order_ID", MQuery.EQUAL, new Integer(p_C_Order_ID));
			
			format = MPrintFormat.get (getCtx(), p_AD_PrintFormat_ID, false);
			format.setLanguage(language);
			format.setTranslationLanguage(language);
			PrintInfo info = new PrintInfo(
					DocumentNo,
					X_OV_CierreComexLine.Table_ID,
					p_C_Order_ID);
				info.setCopies(1);
			ReportEngine re = new ReportEngine(getCtx(), format, query, info);
			File cierreLine = null;
			if (!Ini.isClient())
				cierreLine = new File(getPDFFileName(documentDir, p_C_Order_ID));
			File attachment = re.getPDF(cierreLine);
			
			email.addAttachment(attachment);
			EMail.SENT_OK.equals(email.send());
			
			cierreLine.delete();
			ret = "Costos actualizados.";
		}		
		
		return ret;
	}
	
	private String getPDFFileName (String documentDir, int C_Order_ID)
	{
		StringBuffer sb = new StringBuffer (documentDir);
		if (sb.length() == 0)
			sb.append(".");
		if (!sb.toString().endsWith(File.separator))
			sb.append(File.separator);
		sb.append("C_Order_ID_")
			.append(C_Order_ID)
			.append(".pdf");
		return sb.toString();
	}	//	getPDFFileName
}
