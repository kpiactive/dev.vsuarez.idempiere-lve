/**********************************************************************
* This file is part of iDempiere ERP Open Source                      *
* http://www.idempiere.org                                            *
*                                                                     *
* Copyright (C) Contributors                                          *
*                                                                     *
* This program is free software; you can redistribute it and/or       *
* modify it under the terms of the GNU General Public License         *
* as published by the Free Software Foundation; either version 2      *
* of the License, or (at your option) any later version.              *
*                                                                     *
* This program is distributed in the hope that it will be useful,     *
* but WITHOUT ANY WARRANTY; without even the implied warranty of      *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
* GNU General Public License for more details.                        *
*                                                                     *
* You should have received a copy of the GNU General Public License   *
* along with this program; if not, write to the Free Software         *
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
* MA 02110-1301, USA.                                                 *
*                                                                     *
* Contributors:                                                       *
* - Carlos Ruiz - globalqss                                           *
**********************************************************************/

package org.globalqss.model;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.model.MInvoice;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

import ve.net.dcs.model.MLVEVoucherWithholding;

/**
 *	Invoice Withholding Model
 *
 *  @author Carlos Ruiz - globalqss
 */
public class MLCOInvoiceWithholding extends X_LCO_InvoiceWithholding
{
	/**
	 *
	 */
	private static final long serialVersionUID = -3086189821486687908L;
	/**	Static Logger	*/
	@SuppressWarnings("unused")
	private static CLogger	s_log	= CLogger.getCLogger (MLCOInvoiceWithholding.class);


	/**************************************************************************
	 * 	Default Constructor
	 *	@param ctx context
	 *	@param MLCOInvoiceWithholding_ID id
	 *	@param trxName transaction
	 */
	public MLCOInvoiceWithholding (Properties ctx, int MLCOInvoiceWithholding_ID, String trxName)
	{
		super(ctx, MLCOInvoiceWithholding_ID, trxName);
	}	//	MLCOInvoiceWithholding

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MLCOInvoiceWithholding(Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MLCOInvoiceWithholding
	
	/**
	 * Get Withholding Amt
	 * @param invoice
	 * @return
	 * @throws IOException
	 */
	public static BigDecimal getWithholdingAmt(MInvoice invoice) throws IOException {
		String SQL = "SELECT COALESCE(SUM(TaxAmt),0) " 
				   + "FROM LCO_InvoiceWithholding iw " 
				   + "WHERE AD_Client_ID = ? AND C_Invoice_ID = ? ";
		
		BigDecimal WithholdingAmt = DB.getSQLValueBD(invoice.get_TrxName(), SQL, new Object[] {invoice.getAD_Client_ID(), invoice.getC_Invoice_ID()});
		return WithholdingAmt;
	}

	/**************************************************************************
	 * 	Before Save
	 *	@param newRecord
	 *	@return true if save
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		log.fine("New=" + newRecord);
		MInvoice inv = new MInvoice(getCtx(), getC_Invoice_ID(), get_TrxName());
		int LVE_VoucherWithholding_ID = 0;
		if (get_Value("LVE_VoucherWithholding_ID") != null){
			LVE_VoucherWithholding_ID = ((Integer)get_Value("LVE_VoucherWithholding_ID")).intValue();
		}
		if (LVE_VoucherWithholding_ID > 0){
			MLVEVoucherWithholding voucher = new MLVEVoucherWithholding(getCtx(), LVE_VoucherWithholding_ID, get_TrxName());
			setDateAcct((Timestamp)voucher.get_Value("DateAcct"));
			setDateTrx(voucher.getDateTrx());
		} else {
			if (inv.getReversal_ID() <= 0) {
				if (getLCO_WithholdingRule_ID() > 0) {

					// Fill isCalcOnPayment according to rule
					X_LCO_WithholdingRule wr = new X_LCO_WithholdingRule(getCtx(), getLCO_WithholdingRule_ID(), get_TrxName());
					X_LCO_WithholdingCalc wc = new X_LCO_WithholdingCalc(getCtx(), wr.getLCO_WithholdingCalc_ID(), get_TrxName());
					setIsCalcOnPayment( ! wc.isCalcOnInvoice() );

				} else {
					// Fill isCalcOnPayment according to isSOTrx on type
					X_LCO_WithholdingType wt = new X_LCO_WithholdingType (getCtx(), getLCO_WithholdingType_ID(), get_TrxName());
					// set on payment for sales, and on invoice for purchases
					setIsCalcOnPayment(wt.isSOTrx());
				}

				// Fill DateTrx and DateAcct for isCalcOnInvoice according to the invoice
				if (getC_AllocationLine_ID() <= 0) {
					setDateAcct(inv.getDateAcct());
					setDateTrx(inv.getDateInvoiced());
				}
			}
		}

		return true;
	}	//	beforeSave

	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return saved
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (!success)
			return success;

		return LCO_MInvoice.updateHeaderWithholding(getC_Invoice_ID(), get_TrxName());
	}	//	afterSave

	/**
	 * 	After Delete
	 *	@param success success
	 *	@return deleted
	 */
	protected boolean afterDelete (boolean success)
	{
		if (!success)
			return success;
		return LCO_MInvoice.updateHeaderWithholding(getC_Invoice_ID(), get_TrxName());
	}	//	afterDelete

}	//	MLCOInvoiceWithholding
