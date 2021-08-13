package org.openvia.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MOrder;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class UpdateArancelPais extends SvrProcess {
	
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 1000000;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 1000000;
	
	private int p_C_Country_ID = 0;
	private BigDecimal p_Arancel = BigDecimal.ZERO;
	
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
			else if (name.equals("C_Country_ID"))
				p_C_Country_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else if (name.equals("ov_arancel"))
				p_Arancel = ((BigDecimal)para[i].getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	@Override
	protected String doIt() throws Exception {
		String ret = "";
		log.info("C_Country_ID=" + p_C_Country_ID);
		if (p_C_Country_ID == 0)
			throw new IllegalArgumentException("País nulo");
		
		if (p_Arancel == null)
			throw new IllegalArgumentException("Arancel no puede ser nulo");

		// Mover recibos a bodega abastecimiento
		String sql = "UPDATE C_Country"
				+ " SET ov_arancel = " + p_Arancel
				+ " WHERE C_Country_ID = " + p_C_Country_ID;
		PreparedStatement pstmt = null;
		try {
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.executeUpdate();
		} catch (Exception e) {
			log.log(Level.SEVERE, sql, e);
		} finally {
			DB.close(pstmt);
		}
		//
		
		ret = "Arancel actualizado.";
		
		return ret;
	}
}
