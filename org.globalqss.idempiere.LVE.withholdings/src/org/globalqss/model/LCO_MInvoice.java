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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MAcctSchema;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MClientInfo;
import org.compiere.model.MConversionRate;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MLocation;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPriceList;
import org.compiere.model.MSysConfig;
import org.compiere.model.MTax;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import ve.net.dcs.model.MLVEVoucherWithholding;

/**
 *	LCO_MInvoice
 *
 *  @author Carlos Ruiz - globalqss - Quality Systems & Solutions - http://globalqss.com
 */
public class LCO_MInvoice extends MInvoice
{
	/**
	 *
	 */
	private static final long serialVersionUID = -924606040343895114L;

	public LCO_MInvoice(Properties ctx, int C_Invoice_ID, String trxName) {
		super(ctx, C_Invoice_ID, trxName);
	}

	public int recalcWithholdings(MLVEVoucherWithholding voucher) throws SQLException {
		if (! MSysConfig.getBooleanValue("LCO_USE_WITHHOLDINGS", true, Env.getAD_Client_ID(Env.getCtx())))
			return 0;

		MDocType dt = new MDocType(getCtx(), getC_DocTypeTarget_ID(), get_TrxName());
		String genwh = dt.get_ValueAsString("GenerateWithholding");
		//CLient Currency
		
		if (genwh == null || genwh.equals("N") || genwh.equals(""))
			return 0;

		int noins = 0;
		log.info("");
		BigDecimal totwith = new BigDecimal("0");

		// Fill variables normally needed
		// BP variables
		MBPartner bp = new MBPartner(getCtx(), getC_BPartner_ID(), get_TrxName());
		int bp_isic_id = bp.get_ValueAsInt("LCO_ISIC_ID");
		int bp_taxpayertype_id = bp.get_ValueAsInt("LCO_TaxPayerType_ID");
		MBPartnerLocation mbpl = new MBPartnerLocation(getCtx(), getC_BPartner_Location_ID(), get_TrxName());
		MLocation bpl = MLocation.get(getCtx(), mbpl.getC_Location_ID(), get_TrxName());
		int bp_city_id = bpl.getC_City_ID();
		// OrgInfo variables
		MOrgInfo oi = MOrgInfo.get(getCtx(), getAD_Org_ID(), get_TrxName());
		int org_isic_id = oi.get_ValueAsInt("LCO_ISIC_ID");
		int org_taxpayertype_id = oi.get_ValueAsInt("LCO_TaxPayerType_ID");
		MLocation ol = MLocation.get(getCtx(), oi.getC_Location_ID(), get_TrxName());
		int org_city_id = ol.getC_City_ID();

		// Search withholding types applicable depending on IsSOTrx
		List<Object> params = new ArrayList<Object>();
		
		//currency
		MAcctSchema m_ass = MClientInfo.get(getCtx(), getAD_Client_ID()).getMAcctSchema1();
		int C_Currency_ID = m_ass.getC_Currency_ID();
		String sqlwhere  = "IsSOTrx=?";
		params.add(isSOTrx() ? "Y" : "N");
		if (voucher != null){
			sqlwhere += " AND LCO_WithholdingType_ID = ?";
			params.add(voucher.getLCO_WithholdingType_ID());
		}
			
		
		List<X_LCO_WithholdingType> wts = new Query(getCtx(), X_LCO_WithholdingType.Table_Name, sqlwhere, get_TrxName())
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.setParameters(params)
			.list();
		
		for (X_LCO_WithholdingType wt : wts)
		{
			
			String sql = "DELETE FROM LCO_InvoiceWithholding iw USING LVE_VoucherWithholding vw WHERE iw.C_Invoice_ID=? AND (vw.DocStatus = 'DR' OR vw.DocStatus = 'IP' OR iw.LVE_VoucherWithholding_ID IS NULL) AND iw.LCO_WithholdingType_ID=? ";
				
			int nodel = DB.executeUpdateEx(
					sql,
					new Object[]{getC_Invoice_ID(),wt.getLCO_WithholdingType_ID()},
					get_TrxName());
			log.config("LCO_InvoiceWithholding deleted="+nodel);
			
			// For each applicable withholding
			log.info("Withholding Type: "+wt.getLCO_WithholdingType_ID()+"/"+wt.getName());

			X_LCO_WithholdingRuleConf wrc = new Query(getCtx(),
					X_LCO_WithholdingRuleConf.Table_Name,
					"LCO_WithholdingType_ID=?",
					get_TrxName())
					.setOnlyActiveRecords(true)
					.setParameters(wt.getLCO_WithholdingType_ID())
					.first();
			if (wrc == null) {
				log.warning("No LCO_WithholdingRuleConf for LCO_WithholdingType = "+wt.getLCO_WithholdingType_ID());
				continue;
			}

			// look for applicable rules according to config fields (rule)
			StringBuffer wherer = new StringBuffer(" LCO_WithholdingType_ID=? AND ValidFrom<=? ");
			List<Object> paramsr = new ArrayList<Object>();
			paramsr.add(wt.getLCO_WithholdingType_ID());
			paramsr.add(getDateInvoiced());
			if (wrc.isUseBPISIC()) {
				wherer.append(" AND LCO_BP_ISIC_ID=? ");
				paramsr.add(bp_isic_id);
			}
			if (wrc.isUseBPTaxPayerType()) {
				wherer.append(" AND LCO_BP_TaxPayerType_ID=? ");
				paramsr.add(bp_taxpayertype_id);
			}
			if (wrc.isUseOrgISIC()) {
				wherer.append(" AND LCO_Org_ISIC_ID=? ");
				paramsr.add(org_isic_id);
			}
			if (wrc.isUseOrgTaxPayerType()) {
				wherer.append(" AND LCO_Org_TaxPayerType_ID=? ");
				paramsr.add(org_taxpayertype_id);
			}
			if (wrc.isUseBPCity()) {
				wherer.append(" AND LCO_BP_City_ID=? ");
				paramsr.add(bp_city_id);
				if (bp_city_id <= 0)
					log.warning("Possible configuration error bp city is used but not set");
			}
			if (wrc.isUseOrgCity()) {
				wherer.append(" AND LCO_Org_City_ID=? ");
				paramsr.add(org_city_id);
				if (org_city_id <= 0)
					log.warning("Possible configuration error org city is used but not set");
			}

			// Add withholding categories of lines
			if (wrc.isUseWithholdingCategory()) {
				// look the conf fields
				String sqlwcs =
					"SELECT DISTINCT COALESCE (p.LCO_WithholdingCategory_ID, COALESCE (c.LCO_WithholdingCategory_ID, 0)) "
					+ "  FROM C_InvoiceLine il "
					+ "  LEFT OUTER JOIN M_Product p ON (il.M_Product_ID = p.M_Product_ID) "
					+ "  LEFT OUTER JOIN C_Charge c ON (il.C_Charge_ID = c.C_Charge_ID) "
					+ "  WHERE C_Invoice_ID = ? AND il.IsActive='Y' AND (il.M_Product_ID>0 OR il.C_Charge_ID>0)";
				int[] wcids = DB.getIDsEx(get_TrxName(), sqlwcs, new Object[] {getC_Invoice_ID()});
				boolean addedlines = false;
				for (int i = 0; i < wcids.length; i++) {
					int wcid = wcids[i];
					if (wcid > 0) {
						if (! addedlines) {
							wherer.append(" AND LCO_WithholdingCategory_ID IN (");
							addedlines = true;
						} else {
							wherer.append(",");
						}
						wherer.append(wcid);
					}
				}
				if (addedlines)
					wherer.append(") ");
			}

			// Add tax categories of lines
			if (wrc.isUseProductTaxCategory()) {
				// look the conf fields
				String sqlwct =
					"SELECT DISTINCT COALESCE (p.C_TaxCategory_ID, COALESCE (c.C_TaxCategory_ID, 0)) "
					+ "  FROM C_InvoiceLine il "
					+ "  LEFT OUTER JOIN M_Product p ON (il.M_Product_ID = p.M_Product_ID) "
					+ "  LEFT OUTER JOIN C_Charge c ON (il.C_Charge_ID = c.C_Charge_ID) "
					+ "  WHERE C_Invoice_ID = ? AND il.IsActive='Y' AND (il.M_Product_ID>0 OR il.C_Charge_ID>0)";
				int[] wcids = DB.getIDsEx(get_TrxName(), sqlwct, new Object[] {getC_Invoice_ID()});
				boolean addedlines = false;
				for (int i = 0; i < wcids.length; i++) {
					int wcid = wcids[i];
					if (wcid > 0) {
						if (! addedlines) {
							wherer.append(" AND C_TaxCategory_ID IN (");
							addedlines = true;
						} else {
							wherer.append(",");
						}
						wherer.append(wcid);
					}
				}
				if (addedlines)
					wherer.append(") ");
			}

			List<X_LCO_WithholdingRule> wrs = new Query(getCtx(), X_LCO_WithholdingRule.Table_Name, wherer.toString(), get_TrxName())
				.setOnlyActiveRecords(true)
				.setParameters(paramsr)
				.list();
			for (X_LCO_WithholdingRule wr : wrs)
			{
				// for each applicable rule
				// bring record for withholding calculation
				X_LCO_WithholdingCalc wc = null;
				if(voucher!=null){
					if(voucher.get_ValueAsInt("LCO_WithholdingCalc_ID")!=0){
					//Modificaciones para que tome LCO_WithholdingCalc_ID desde la ventana de comprobante de retencion
						try{
							wc = new X_LCO_WithholdingCalc(getCtx(), voucher.get_ValueAsInt("LCO_WithholdingCalc_ID"), get_TrxName());
						}
						catch(NullPointerException e){
							//No hago nada
						}
					}
				}
				if(wc==null)
					wc = (X_LCO_WithholdingCalc) wr.getLCO_WithholdingCalc();
				/*if (voucher.get_ValueAsInt("LCO_WithholdingCalc_ID")>0)
						wc = new X_LCO_WithholdingCalc(getCtx(), voucher.get_ValueAsInt("LCO_WithholdingCalc_ID"), get_TrxName());
				else{
						wc = (X_LCO_WithholdingCalc) wr.getLCO_WithholdingCalc();
					}*/
				//Fin Modificaciones
				
				if (wc == null || wc.getLCO_WithholdingCalc_ID() == 0) {
					log.severe("Rule without calc " + wr.getLCO_WithholdingRule_ID());
					continue;
				}

				// bring record for tax
				MTax tax = new MTax(getCtx(), wc.getC_Tax_ID(), get_TrxName());

				log.info("WithholdingRule: "+wr.getLCO_WithholdingRule_ID()+"/"+wr.getName()
						+" BaseType:"+wc.getBaseType()
						+" Calc: "+wc.getLCO_WithholdingCalc_ID()+"/"+wc.getName()
						+" CalcOnInvoice:"+wc.isCalcOnInvoice()
						+" Tax: "+tax.getC_Tax_ID()+"/"+tax.getName());

				// calc base
				// apply rule to calc base
				BigDecimal base = Env.ZERO;
				
				//SUBTRAHEND
//				BigDecimal MinAmount = Env.ZERO;
//				BigDecimal Subtrahend = Env.ZERO;
				//SUBTRAHEND

				if (wc.getBaseType() == null) {
					log.severe("Base Type null in calc record "+wr.getLCO_WithholdingCalc_ID());
				} else if (wc.getBaseType().equals(X_LCO_WithholdingCalc.BASETYPE_Document)) {
					base = getTotalLines();
				} else if (wc.getBaseType().equals(X_LCO_WithholdingCalc.BASETYPE_Line)) {
					List<Object> paramslca = new ArrayList<Object>();
					paramslca.add(getC_Invoice_ID());
					String sqllca; 
					
					//SUBTRAHEND
//					Integer TaxUnit = MLVETaxUnit.taxUnit(get_TrxName(), getAD_Org_ID(), getDateInvoiced(), getDateInvoiced());
//					BigDecimal Factor = new BigDecimal(wc.get_Value("SubtrahendFactor").toString());
//					MinAmount = Factor.multiply(new BigDecimal(TaxUnit));
					//SUBTRAHEND
					
					if (wrc.isUseWithholdingCategory() && wrc.isUseProductTaxCategory()) {
						// base = lines of the withholding category and tax category
						sqllca = 
							"SELECT COALESCE(SUM (LineNetAmt),0) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND (   EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM M_Product p "
							+ "               WHERE il.M_Product_ID = p.M_Product_ID "
							+ "                 AND p.C_TaxCategory_ID = ? "
							+ "                 AND p.LCO_WithholdingCategory_ID = ?) "
							+ "        OR EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM C_Charge c "
							+ "               WHERE il.C_Charge_ID = c.C_Charge_ID "
							+ "                 AND c.C_TaxCategory_ID = ? "
							+ "                 AND c.LCO_WithholdingCategory_ID = ?) "
							+ "       ) ";
						paramslca.add(wr.getC_TaxCategory_ID());
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
						paramslca.add(wr.getC_TaxCategory_ID());
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
					} else if (wrc.isUseWithholdingCategory()) {
						// base = lines of the withholding category
						sqllca = 
							"SELECT COALESCE(SUM (LineNetAmt),0) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND (   EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM M_Product p "
							+ "               WHERE il.M_Product_ID = p.M_Product_ID "
							+ "                 AND p.LCO_WithholdingCategory_ID = ?) "
							+ "        OR EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM C_Charge c "
							+ "               WHERE il.C_Charge_ID = c.C_Charge_ID "
							+ "                 AND c.LCO_WithholdingCategory_ID = ?) "
							+ "       ) ";
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
					} else if (wrc.isUseProductTaxCategory()) {
						// base = lines of the product tax category
						sqllca = 
							"SELECT COALESCE(SUM (LineNetAmt),0) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND (   EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM M_Product p "
							+ "               WHERE il.M_Product_ID = p.M_Product_ID "
							+ "                 AND p.C_TaxCategory_ID = ?) "
							+ "        OR EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM C_Charge c "
							+ "               WHERE il.C_Charge_ID = c.C_Charge_ID "
							+ "                 AND c.C_TaxCategory_ID = ?) "
							+ "       ) ";
						paramslca.add(wr.getC_TaxCategory_ID());
						paramslca.add(wr.getC_TaxCategory_ID());
					} else {
						// base = all lines
						sqllca = 
							"SELECT COALESCE(SUM (LineNetAmt),0) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? ";
					}
					base = DB.getSQLValueBD(get_TrxName(), sqllca, paramslca);
					//SUBTRAHEND
//					if (MinAmount.compareTo(Env.ZERO) > 0) {
//						PreparedStatement pstmt2 = null;
//						ResultSet rs2 = null;
//						String sqlsus = "SELECT * FROM LCO_InvoiceWithholding wh "
//								+ " JOIN C_Invoice iv ON wh.C_Invoice_ID = iv.C_Invoice_ID AND "
//								+ " iv.C_BPartner_ID = ? AND "
//								+ " iv.DateInvoiced BETWEEN ? AND ?"
//								+ " WHERE wh.LCO_WithholdingRule_ID = ? AND wh.IsActive='Y'";
//
//						pstmt2 = DB.prepareStatement(sqlsus, get_TrxName());
//						pstmt2.setInt(1, getC_BPartner_ID());
//						pstmt2.setTimestamp(2, MLVETaxUnit.firstDayOfMonth(getDateInvoiced()));
//						pstmt2.setTimestamp(3, getDateInvoiced());
//						pstmt2.setInt(4, wr.get_ID());
//
//						rs2 = pstmt2.executeQuery();
//						BigDecimal Sub = Env.ZERO;
//						BigDecimal BaseWH = Env.ZERO;
//						while (rs2.next()) {
//							MLCOInvoiceWithholding iwhc = new MLCOInvoiceWithholding(
//									getCtx(), rs2, get_TrxName());
//
//							Sub = new BigDecimal(iwhc.get_Value(
//									"Subtrahend").toString());
//
//							BaseWH = BaseWH.add(iwhc.getTaxBaseAmt());
//
//							if (Sub.compareTo(Env.ZERO) > 0)
//								MinAmount = Env.ZERO;
//
//						}
//
//						base = base.subtract(BaseWH);
//					}
//
//					Subtrahend = tax.getRate().divide(Env.ONEHUNDRED)
//							.multiply(MinAmount);
					//SUBTRAHEND
					
				} else if (wc.getBaseType().equals(X_LCO_WithholdingCalc.BASETYPE_Tax)) {
					// if specific tax
					if (wc.getC_BaseTax_ID() != 0) {
						// base = value of specific tax
						String sqlbst = "SELECT SUM(TaxAmt) "
							+ " FROM C_InvoiceTax "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND C_Tax_ID = ?";
						base = DB.getSQLValueBD(get_TrxName(), sqlbst, new Object[] {getC_Invoice_ID(), wc.getC_BaseTax_ID()});
					} else {
						// not specific tax
						// base = value of all taxes
						String sqlbsat = "SELECT COALESCE(SUM(TaxAmt),0) "
							+ " FROM C_InvoiceTax "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? ";
						base = DB.getSQLValueBD(get_TrxName(), sqlbsat, new Object[] {getC_Invoice_ID()});
					}
				}
				//convert base
				
				log.info("Base: "+base+ " Thresholdmin:"+wc.getThresholdmin());

				// if base between thresholdmin and thresholdmax inclusive
				// if thresholdmax = 0 it is ignored
				if (base != null &&
						base.compareTo(Env.ZERO) != 0 &&
						base.compareTo(wc.getThresholdmin()) >= 0 &&
						(wc.getThresholdMax() == null || wc.getThresholdMax().compareTo(Env.ZERO) == 0 || base.compareTo(wc.getThresholdMax()) <= 0) &&
						tax.getRate() != null &&
						tax.getRate().compareTo(Env.ZERO) != 0) {

					// insert new withholding record
					// with: type, tax, base amt, percent, tax amt, trx date, acct date, rule
					MLCOInvoiceWithholding iwh = new MLCOInvoiceWithholding(getCtx(), 0, get_TrxName());
					iwh.setAD_Org_ID(getAD_Org_ID());
					iwh.setC_Invoice_ID(getC_Invoice_ID());
					iwh.setDateAcct(getDateAcct());
					iwh.setDateTrx(getDateInvoiced());
					iwh.setIsCalcOnPayment( ! wc.isCalcOnInvoice() );
					iwh.setIsTaxIncluded(false);
					iwh.setLCO_WithholdingRule_ID(wr.getLCO_WithholdingRule_ID());
					iwh.setLCO_WithholdingType_ID(wt.getLCO_WithholdingType_ID());
					iwh.setC_Tax_ID(tax.getC_Tax_ID());
					iwh.setPercent(tax.getRate());
					iwh.setProcessed(false);
					int stdPrecision = MPriceList.getStandardPrecision(getCtx(), getM_PriceList_ID());
					BigDecimal taxamt = tax.calculateTax(base, false, stdPrecision);
					if (wc.getAmountRefunded() != null &&
							wc.getAmountRefunded().compareTo(Env.ZERO) > 0) {
						taxamt = taxamt.subtract(wc.getAmountRefunded());
					}
					if("Y".equalsIgnoreCase(MSysConfig.getValue("LVE_WHUseCurrencyConvert", "Y", getAD_Client_ID()))) {
						base = MConversionRate.convert(getCtx(), base, getC_Currency_ID(), C_Currency_ID, getDateInvoiced(), 114, getAD_Client_ID(), getAD_Org_ID());
						taxamt = MConversionRate.convert(getCtx(), taxamt, getC_Currency_ID(), C_Currency_ID, getDateInvoiced(), 114, getAD_Client_ID(), getAD_Org_ID());
					}
					iwh.setTaxAmt(taxamt);
					iwh.setTaxBaseAmt(base);
					if (    (  isSOTrx() && MSysConfig.getBooleanValue("QSSLCO_GenerateWithholdingInactiveSO", false, getAD_Client_ID(), getAD_Org_ID()) )
						 || ( !isSOTrx() && MSysConfig.getBooleanValue("QSSLCO_GenerateWithholdingInactivePO", false, getAD_Client_ID(), getAD_Org_ID()) )) {
						iwh.setIsActive(false);
					}
					iwh.set_ValueOfColumn("Subtrahend", wc.getAmountRefunded());
					
					//SUBTRAHEND
//					iwh.set_ValueOfColumn("Subtrahend", Subtrahend.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP));
//					iwh.setTaxAmt(taxamt.subtract(Subtrahend).setScale(stdPrecision, BigDecimal.ROUND_HALF_UP));
//					iwh.setTaxBaseAmt(base);
					//SUBTRAHEND
					iwh.saveEx();
					totwith = totwith.add(taxamt);
					if (voucher != null)
						iwh.set_ValueOfColumn("LVE_VoucherWithholding_ID", voucher.getLVE_VoucherWithholding_ID());
					iwh.saveEx();
					noins++;
					log.info("LCO_InvoiceWithholding saved:"+iwh.getTaxAmt());
				}
			} // while each applicable rule

		} // while type
		LCO_MInvoice.updateHeaderWithholding(getC_Invoice_ID(), get_TrxName());
		saveEx();

		return noins;
	}

	/**
	 *	Update Withholding in Header
	 *	@return true if header updated with withholding
	 */
	public static boolean updateHeaderWithholding(int C_Invoice_ID, String trxName)
	{
		//	Update Invoice Header
		String sql =
			"UPDATE C_Invoice "
			+ " SET WithholdingAmt="
				+ "(SELECT COALESCE(SUM(TaxAmt),0) FROM LCO_InvoiceWithholding iw WHERE iw.IsActive = 'Y' " +
						"AND iw.IsCalcOnPayment = 'N' AND C_Invoice.C_Invoice_ID=iw.C_Invoice_ID) "
			+ "WHERE C_Invoice_ID=?";
		int no = DB.executeUpdateEx(sql, new Object[] {C_Invoice_ID}, trxName);

		return no == 1;
	}	//	updateHeaderWithholding

	/*
	 * Set Withholding Amount without Logging (via direct SQL UPDATE)
	 */
	public static boolean setWithholdingAmtWithoutLogging(MInvoice inv, BigDecimal wamt) {
		DB.executeUpdateEx("UPDATE C_Invoice SET WithholdingAmt=? WHERE C_Invoice_ID=?",
				new Object[] {wamt, inv.getC_Invoice_ID()},
				inv.get_TrxName());
		return true;
	}

}	//	LCO_MInvoice
