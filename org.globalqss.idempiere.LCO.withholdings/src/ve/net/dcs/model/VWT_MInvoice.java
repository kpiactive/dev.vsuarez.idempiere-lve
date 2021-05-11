package ve.net.dcs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MInvoice;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.globalqss.model.LCO_MInvoice;
import org.globalqss.model.X_LCO_WithholdingType;

public class VWT_MInvoice extends LCO_MInvoice {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2395133244329847859L;

	public VWT_MInvoice(Properties ctx, int C_Invoice_ID, String trxName) {
		super(ctx, C_Invoice_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 	Get Payments Of BPartner
	 *	@param ctx context
	 *	@param C_BPartner_ID id
	 *	@param trxName transaction
	 *	@return array
	 */

	public static MInvoice[] getOfBPartnerDateFromDateTo (Properties ctx, MLVEVoucherWithholding voucher, String trxName)
	{
		
		ArrayList<Object> parameters = new ArrayList<Object>();
		
		X_LCO_WithholdingType wt = new X_LCO_WithholdingType(ctx, voucher.getLCO_WithholdingType_ID(), trxName);
		String isIssotrx = wt.isSOTrx() ? "Y": "N";
		
		String sqlwhere = COLUMNNAME_C_BPartner_ID+"=? AND IsSoTrx = ? ";
		
		parameters.add(voucher.getC_BPartner_ID());
		parameters.add(isIssotrx);
		
		if (voucher.get_ValueAsInt("C_Invoice_ID") != 0){
			sqlwhere += " AND "+COLUMNNAME_C_Invoice_ID+"=? ";
			parameters.add(voucher.get_ValueAsInt("C_Invoice_ID"));
		}
		
		if (voucher.getDateTo() != null && voucher.getDateFrom() != null){
			sqlwhere += " AND "+COLUMNNAME_DateAcct+" BETWEEN ? AND ? ";
			parameters.add(voucher.getDateFrom());
			parameters.add(voucher.getDateTo());
		}
		else if (voucher.getDateFrom() != null) {
			sqlwhere += " AND "+COLUMNNAME_DateAcct+" >= ? ";
			parameters.add(voucher.getDateFrom());
		}else if (voucher.getDateTo() != null) {
			sqlwhere += " AND "+COLUMNNAME_DateAcct+" <= ? ";
			parameters.add(voucher.getDateTo());
		}
		
		sqlwhere += " AND AD_Org_ID = ? AND DOCSTATUS IN ('CO','CL') ";
		
		if(MSysConfig.getValue("LVE_GenerateInvoiceWithholdingIsPaid","N",voucher.getAD_Client_ID()).compareTo("N")==0){
			sqlwhere += " AND ISPaid = 'N' ";
		}
		
		sqlwhere += " AND "+COLUMNNAME_C_Invoice_ID+" NOT IN (SELECT lw.C_Invoice_ID FROM LCO_InvoiceWithholding lw JOIN LVE_VoucherWithholding vw ON lw.LVE_VoucherWithholding_ID = vw.LVE_VoucherWithholding_ID WHERE  vw.DocStatus IN ('CO','DR') AND lw.LCO_WithholdingType_ID = ? AND lw.AD_Org_ID = ?) ";
		
		parameters.add(voucher.getAD_Org_ID());
		parameters.add(voucher.getLCO_WithholdingType_ID());
		parameters.add(voucher.getAD_Org_ID());
		
		List<MInvoice> list = new Query(ctx, Table_Name, sqlwhere, trxName)
									.setParameters(parameters)
									.setOnlyActiveRecords(true)
									.list();
		return list.toArray(new MInvoice[list.size()]);
	}	//	getOfBPartner

}
