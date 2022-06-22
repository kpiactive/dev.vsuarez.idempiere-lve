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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.FactsEventData;
import org.adempiere.base.event.IEventManager;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.base.event.LoginEventData;
import org.compiere.acct.Doc;
import org.compiere.acct.DocLine_Allocation;
import org.compiere.acct.DocTax;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MInvoiceTax;
import org.compiere.model.MLocation;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentAllocate;
import org.compiere.model.MPaymentTerm;
import org.compiere.model.MSysConfig;
import org.compiere.model.MTax;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.osgi.service.event.Event;

/**
 *	Validator or Localization Colombia (Withholdings)
 *
 *  @author Carlos Ruiz - globalqss - Quality Systems & Solutions - http://globalqss.com
 */
public class LCO_ValidatorWH extends AbstractEventHandler
{
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(LCO_ValidatorWH.class);

	/**
	 *	Initialize Validation
	 */
	@Override
	protected void initialize() {
		log.warning("");

		//	Tables to be monitored
		registerTableEvent(IEventTopics.PO_BEFORE_CHANGE, MInvoice.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_NEW, MInvoiceLine.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_CHANGE, MInvoiceLine.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_DELETE, MInvoiceLine.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_NEW, X_LCO_WithholdingCalc.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_CHANGE, X_LCO_WithholdingCalc.Table_Name);

		//	Documents to be monitored
		registerTableEvent(IEventTopics.DOC_BEFORE_PREPARE, MInvoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MInvoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_COMPLETE, MInvoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MPayment.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_COMPLETE, MPayment.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_COMPLETE, MAllocationHdr.Table_Name);
		registerTableEvent(IEventTopics.ACCT_FACTS_VALIDATE, MAllocationHdr.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_POST, MAllocationHdr.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_POST, MAllocationHdr.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_VOID, MAllocationHdr.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_REVERSECORRECT, MAllocationHdr.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_REVERSEACCRUAL, MAllocationHdr.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_DELETE, MAllocationLine.Table_Name);

		registerEvent(IEventTopics.AFTER_LOGIN);
	}	//	initialize

    /**
     *	Model Change of a monitored Table or Document
     *  @param event
     *	@exception Exception if the recipient wishes the change to be not accept.
     */
	@Override
	protected void doHandleEvent(Event event) {
		String type = event.getTopic();

		if (type.equals(IEventTopics.AFTER_LOGIN)) {
			log.info("Type: " + type);
			// on login set context variable #LCO_USE_WITHHOLDINGS
			LoginEventData loginData = (LoginEventData) event.getProperty(IEventManager.EVENT_DATA);
			boolean useWH = MSysConfig.getBooleanValue("LCO_USE_WITHHOLDINGS", true, loginData.getAD_Client_ID());
			Env.setContext(Env.getCtx(), "#LCO_USE_WITHHOLDINGS", useWH);
			return;
		}

		if (! MSysConfig.getBooleanValue("LCO_USE_WITHHOLDINGS", true, Env.getAD_Client_ID(Env.getCtx())))
			return;

		PO po = null;
		if (type.equals(IEventTopics.ACCT_FACTS_VALIDATE)) {
			FactsEventData fed = getEventData(event);
			po = fed.getPo();
		} else {
			po = getPO(event);
		}
		log.info(po.get_TableName() + " Type: "+type);
		String msg;

		// Model Events
		if (po instanceof MInvoice && type.equals(IEventTopics.PO_BEFORE_CHANGE)) {
			msg = clearInvoiceWithholdingAmtFromInvoice((MInvoice) po);
			if (msg != null)
				throw new RuntimeException(msg);
		}

		// when invoiceline is changed clear the withholding amount on invoice
		// in order to force a regeneration
		if (po instanceof MInvoiceLine &&
				(type.equals(IEventTopics.PO_BEFORE_NEW) ||
				 type.equals(IEventTopics.PO_BEFORE_CHANGE) ||
				 type.equals(IEventTopics.PO_BEFORE_DELETE)
				)
			)
		{
			msg = clearInvoiceWithholdingAmtFromInvoiceLine((MInvoiceLine) po, type);
			if (msg != null)
				throw new RuntimeException(msg);
		}

		if (po instanceof X_LCO_WithholdingCalc
				&& (type.equals(IEventTopics.PO_BEFORE_CHANGE) || type.equals(IEventTopics.PO_BEFORE_NEW))) {
			X_LCO_WithholdingCalc lwc = (X_LCO_WithholdingCalc) po;
			if (lwc.isCalcOnInvoice() && lwc.isCalcOnPayment())
				lwc.setIsCalcOnPayment(false);
		}

		// Document Events
		// before preparing a reversal invoice add the invoice withholding taxes
		if (po instanceof MInvoice
				&& type.equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			MInvoice inv = (MInvoice) po;
			if (inv.isReversal()) {
				int invid = inv.getReversal_ID();

				if (invid > 0) {
					MInvoice invreverted = new MInvoice(inv.getCtx(), invid, inv.get_TrxName());
					String sql =
						  "SELECT LCO_InvoiceWithholding_ID "
						 + " FROM LCO_InvoiceWithholding "
						+ " WHERE C_Invoice_ID = ? "
						+ " ORDER BY LCO_InvoiceWithholding_ID";
					try (PreparedStatement pstmt = DB.prepareStatement(sql, inv.get_TrxName());)
					{
						pstmt.setInt(1, invreverted.getC_Invoice_ID());
						ResultSet rs = pstmt.executeQuery();
						while (rs.next()) {
							MLCOInvoiceWithholding iwh = new MLCOInvoiceWithholding(inv.getCtx(), rs.getInt(1), inv.get_TrxName());
							MLCOInvoiceWithholding newiwh = new MLCOInvoiceWithholding(inv.getCtx(), 0, inv.get_TrxName());
							newiwh.setAD_Org_ID(iwh.getAD_Org_ID());
							newiwh.setC_Invoice_ID(inv.getC_Invoice_ID());
							newiwh.setLCO_WithholdingType_ID(iwh.getLCO_WithholdingType_ID());
							newiwh.setPercent(iwh.getPercent());
							newiwh.setTaxAmt(iwh.getTaxAmt().negate());
							newiwh.setTaxBaseAmt(iwh.getTaxBaseAmt().negate());
							newiwh.setC_Tax_ID(iwh.getC_Tax_ID());
							newiwh.setIsCalcOnPayment(iwh.isCalcOnPayment());
							newiwh.setIsActive(iwh.isActive());
							newiwh.setDateAcct(inv.getDateAcct());
							newiwh.setDateTrx(inv.getDateInvoiced());
							if (!newiwh.save())
								throw new RuntimeException("Error saving LCO_InvoiceWithholding docValidate");
						}
					} catch (Exception e) {
						log.log(Level.SEVERE, sql, e);
						throw new RuntimeException("Error creating LCO_InvoiceWithholding for reversal invoice");
					}
				} else {
					throw new RuntimeException("Can't get the number of the invoice reversed");
				}
			}
		}

		// before preparing invoice validate if withholdings has been generated
		if (po instanceof MInvoice
				&& type.equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			MInvoice inv = (MInvoice) po;
			if (inv.isReversal()) {
				// don't validate this for autogenerated reversal invoices
			} else {
				if (inv.get_Value("WithholdingAmt") == null) {
					MDocType dt = new MDocType(inv.getCtx(), inv.getC_DocTypeTarget_ID(), inv.get_TrxName());
					String genwh = dt.get_ValueAsString("GenerateWithholding");
					if (genwh != null) {

						if (genwh.equals("Y") && !inv.isSOTrx()) {
							// document type configured to compel generation of withholdings
							throw new RuntimeException(Msg.getMsg(inv.getCtx(), "LCO_WithholdingNotGenerated"));
						}

						if (genwh.equals("A")) {
							// document type configured to generate withholdings automatically
							LCO_MInvoice lcoinv = new LCO_MInvoice(inv.getCtx(), inv.getC_Invoice_ID(), inv.get_TrxName());
							try {
								lcoinv.recalcWithholdings(null);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		// after preparing invoice move invoice withholdings to taxes and recalc grandtotal of invoice
		if (po instanceof MInvoice && type.equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			msg = translateWithholdingToTaxes((MInvoice) po);
			if (msg != null)
				throw new RuntimeException(msg);
		}

		// after completing the invoice fix the dates on withholdings and mark the invoice withholdings as processed
		if (po instanceof MInvoice && type.equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			msg = completeInvoiceWithholding((MInvoice) po);
			if (msg != null)
				throw new RuntimeException(msg);
		}

		// before completing the payment - validate that writeoff amount must be greater than sum of payment withholdings
		if (po instanceof MPayment) {
			MPayment payment = (MPayment) po;
			if(type.equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
				msg = validateWriteOffVsPaymentWithholdings(payment);
				if (msg != null)
					throw new RuntimeException(msg);
			}
		}

		// after completing the allocation - complete the payment withholdings
		if (po instanceof MAllocationHdr && type.equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			MAllocationHdr allocation = (MAllocationHdr) po;
			msg = completePaymentWithholdings(allocation);
			if (msg != null)
				throw new RuntimeException(msg);
			msg = generatePaymentWithHolding(allocation);
			if (msg != null)
				throw new RuntimeException(msg);
		}

		// before posting the allocation - post the payment withholdings vs writeoff amount
		if (po instanceof MAllocationHdr && (type.equals(IEventTopics.ACCT_FACTS_VALIDATE))) {
			msg = accountingForInvoiceWithholdingOnPayment((MAllocationHdr) po, event);
			if (msg != null)
				throw new RuntimeException(msg);
		}

		// after completing the allocation - complete the payment withholdings
		if (po instanceof MAllocationHdr
				&& (type.equals(IEventTopics.DOC_AFTER_VOID) ||
					type.equals(IEventTopics.DOC_AFTER_REVERSECORRECT) ||
					type.equals(IEventTopics.DOC_AFTER_REVERSEACCRUAL))) {
			MAllocationHdr allocation = (MAllocationHdr) po;
			msg = reversePaymentWithholdings(allocation);
			if (msg != null)
				throw new RuntimeException(msg);
			msg = reverseDebitNote(allocation);
			if (msg != null)
				throw new RuntimeException(msg);
		}
		
		// After delete Allocation Line - Reverse Debit Note and Delete Invoice With Holding
		if(po instanceof MAllocationLine && type.equals(IEventTopics.PO_AFTER_DELETE)) {
			MAllocationLine allocationLine = (MAllocationLine) po;
			msg = reverseDebitNote(allocationLine.getParent());
			if (msg != null)
				throw new RuntimeException(msg);
		}

	}	//	doHandleEvent

	/**
	 * Reverse Debit Note Generated for IGTF
	 * @param allocation MAllocation
	 * @return null when no error
	 */
	private String reverseDebitNote(MAllocationHdr allocation) {
		MAllocationLine[] allocLines = allocation.getLines(false);
		MInvoice invoice = null;
		MPayment payment = null;
		for(MAllocationLine line : allocLines) {
			if(line.getC_Invoice() != null)
				invoice = (MInvoice) line.getC_Invoice();
			if(line.getC_Payment() != null)
				payment = (MPayment) line.getC_Payment();
		}
		if(invoice == null || payment == null)
			return null;
		ArrayList<Integer> chargeIGTF_IDs = getC_ChargeIGTF_ID(payment, invoice);
		if(chargeIGTF_IDs.size() == 0)
			return null;
		List<MInvoice> debitNotes = new Query(invoice.getCtx(), MInvoice.Table_Name, "LVE_InvoiceAffected_ID =? AND DocStatus = 'CO' AND C_Payment_ID =?",
				invoice.get_TrxName()).setOnlyActiveRecords(true).setParameters(invoice.getC_Invoice_ID(), payment.getC_Payment_ID()).list();
		for(MInvoice debitNote : debitNotes) {
			boolean haveIGTF = new Query(debitNote.getCtx(), MInvoiceLine.Table_Name, "C_Invoice_ID =? AND C_Charge_ID IN (" +chargeIGTF_IDs.toString().replace("[","" ).replace("]", "") + ")", 
					debitNote.get_TrxName()).setOnlyActiveRecords(true).setParameters(debitNote.getC_Invoice_ID()).match();
			if(haveIGTF) {
				try {
					debitNote.setDocAction(MInvoice.DOCACTION_Reverse_Correct);
					if(debitNote.processIt(MInvoice.DOCACTION_Reverse_Correct)) {
						debitNote.save();
						MLCOInvoiceWithholding invoiceWithHolding = new Query(debitNote.getCtx(), MLCOInvoiceWithholding.Table_Name, "TaxAmt =? AND DateAcct=?", 
								debitNote.get_TrxName()).setOnlyActiveRecords(true).setParameters(debitNote.getGrandTotal(), debitNote.getDateAcct()).first();
						invoiceWithHolding.delete(true);
					} else {
						debitNote.save();
						return "No se pudo Reversar la Nota de Debito IGTF " + debitNote.getDocumentNo() + " - " + debitNote.getProcessMsg();
					}
				} catch (Exception e) {
					return "No se pudo Reversar la Nota de Debito IGTF " + debitNote.getDocumentNo() + " - " + debitNote.getProcessMsg() + " - " + e.getLocalizedMessage();
				}
			}
		}
		return null;
	}

	/**
	 * Get C_Charge IGTF
	 * @param payment MPayment
	 * @param invoice MInvoice
	 * @return ArrayList<Integer> charges IGTF 
	 */
	private ArrayList<Integer> getC_ChargeIGTF_ID(MPayment payment, MInvoice invoice) {
		ArrayList<Integer> C_ChargeIGTF_IDs = new ArrayList<>();
		List<X_LCO_WithholdingRule> wrs = getWithHoldingRules(payment, invoice);
		for(X_LCO_WithholdingRule wr : wrs) {
			if(MLCOWithholdingType.TYPE_IGTF.equalsIgnoreCase(wr.getLCO_WithholdingType().getType())) {
				if(wr.getLCO_WithholdingType().isSOTrx() && wr.getLCO_WithholdingType().getC_Charge_ID() > 0)
					C_ChargeIGTF_IDs.add(wr.getLCO_WithholdingType().getC_Charge_ID());
			}
		}
		return C_ChargeIGTF_IDs;
	}

	/**
	 * Generate Payment With Holding
	 * @param payment MPayment
	 * @return null when no error
	 */
	private String generatePaymentWithHolding(MAllocationHdr allocation) {
		MAllocationLine[] allocLines = allocation.getLines(false);
		MInvoice invoice = null;
		MPayment payment = null;
		BigDecimal payAmt = BigDecimal.ZERO;
		int C_AllocationLine_ID = 0;
		for(MAllocationLine line : allocLines) {
			if(line.getC_Invoice() != null)
				invoice = (MInvoice) line.getC_Invoice();
			if(line.getC_Payment() != null) {
				payment = (MPayment) line.getC_Payment();
				payAmt = line.getAmount();
				C_AllocationLine_ID = line.getC_AllocationLine_ID();
			}
		}
		if(invoice == null || payment == null)
			return null;
		
		MDocType dt = (MDocType) invoice.getC_DocType();
		if("N".equalsIgnoreCase(dt.get_ValueAsString("GenerateWithholding")) || dt.get_ValueAsString("GenerateWithholding") == null)
			return null;
		
		List<X_LCO_WithholdingRule> wrs = getWithHoldingRules(payment, invoice);
		for(X_LCO_WithholdingRule wr : wrs) {
			// for each applicable rule
			// bring record for withholding calculation
			X_LCO_WithholdingCalc wc = (X_LCO_WithholdingCalc) wr.getLCO_WithholdingCalc();
			if (wc == null || wc.getLCO_WithholdingCalc_ID() == 0) {
				log.severe("Rule without calc " + wr.getLCO_WithholdingRule_ID());
				continue;
			}
			// bring record for tax
			MTax tax = new MTax(payment.getCtx(), wc.getC_Tax_ID(), payment.get_TrxName());

			log.info("WithholdingRule: "+wr.getLCO_WithholdingRule_ID()+"/"+wr.getName()
					+" BaseType:"+wc.getBaseType()
					+" Calc: "+wc.getLCO_WithholdingCalc_ID()+"/"+wc.getName()
					+" CalcOnInvoice:"+wc.isCalcOnInvoice()
					+" Tax: "+tax.getC_Tax_ID()+"/"+tax.getName());
			// calc base
			// apply rule to calc base
			BigDecimal base = payAmt;
			
			// if base between thresholdmin and thresholdmax inclusive
			// if thresholdmax = 0 it is ignored
			if (base != null &&
					base.compareTo(BigDecimal.ZERO) != 0 &&
					base.compareTo(wc.getThresholdmin()) >= 0 &&
					(wc.getThresholdMax() == null || wc.getThresholdMax().compareTo(BigDecimal.ZERO) == 0 || base.compareTo(wc.getThresholdMax()) <= 0) &&
					tax.getRate() != null) {
				if (tax.getRate().signum() == 0 && !wc.isApplyOnZero())
					continue;
				return generateDN(invoice, payment, wr, wc, tax, base, C_AllocationLine_ID, allocation.getDateAcct());
			}
		}
		
		return null;
	}

	/**
	 * Generate Debit Note for IGTF
	 * @param invoice MInvoice
	 * @param payment MPayment
	 * @param wc X_LCO_WithholdingCalc
	 * @param tax MTax
	 * @param base BigDecimal
	 * @param C_AllocationLine_ID int
	 * @param Date Acct of Allocation Header Timestamp
	 * @return null when no error
	 */
	private String generateDN(MInvoice invoice, MPayment payment, X_LCO_WithholdingRule wr, X_LCO_WithholdingCalc wc, MTax tax, BigDecimal base, int C_AllocationLine_ID, Timestamp dateAcct) {
		int C_DocTypeDN_ID = wr.getLCO_WithholdingType().getC_DocTypeDN_ID();
		int C_Charge_ID = wr.getLCO_WithholdingType().getC_Charge_ID();
		if(C_DocTypeDN_ID <=0)
			return "Tipo de Documento de Nota de Debito no configurado para Retencion IGTF";
		if(C_Charge_ID <=0)
			return "Cargo de Nota de Debito no configurado para Retencion IGTF";
		
		MInvoice debitNote = new MInvoice(payment.getCtx(), 0, payment.get_TrxName());
		debitNote.setClientOrg(payment.getAD_Client_ID(), payment.getAD_Org_ID());
		debitNote.setBPartner((MBPartner) payment.getC_BPartner());
		debitNote.setC_Currency_ID(payment.getC_Currency_ID());
		debitNote.setC_ConversionType_ID(payment.getC_ConversionType_ID());
		debitNote.setCurrencyRate(payment.getCurrencyRate());
		debitNote.setIsOverrideCurrencyRate(payment.isOverrideCurrencyRate());
		debitNote.set_ValueOfColumn("DivideRate", payment.get_Value("DivideRate"));
		debitNote.setIsSOTrx(invoice.isSOTrx());
		debitNote.setDateInvoiced(dateAcct);
		debitNote.setDateAcct(dateAcct);
		debitNote.setC_DocTypeTarget_ID(C_DocTypeDN_ID);
		debitNote.set_ValueOfColumn("LVE_InvoiceAffected_ID", invoice.getC_Invoice_ID());
		debitNote.setRelatedInvoice_ID(invoice.getC_Invoice_ID());
		debitNote.setAD_User_ID(invoice.getAD_User_ID());
		debitNote.setM_PriceList_ID(invoice.getM_PriceList_ID());
		debitNote.setSalesRep_ID(invoice.getSalesRep_ID());
		debitNote.setPaymentRule(invoice.getPaymentRule());
		debitNote.setC_PaymentTerm_ID(getC_PaymentTerm_ID(payment));
		debitNote.setDescription("Nota de Debito Generada por el Cobro: " + payment.getDocumentNo() + " por IGTF");
		debitNote.setC_Payment_ID(payment.getC_Payment_ID());
		BigDecimal amount = tax.calculateTax(base, false, payment.getC_Currency().getStdPrecision());
		if(debitNote.save()) {
			MInvoiceLine debitNoteLine = new MInvoiceLine(debitNote);
			debitNoteLine.setC_Charge_ID(C_Charge_ID);
			debitNoteLine.setQtyEntered(BigDecimal.ONE);
			debitNoteLine.setQtyInvoiced(BigDecimal.ONE);
			int C_Tax_ID = new Query(debitNote.getCtx(), MTax.Table_Name, "IsTaxExempt = 'Y'", debitNote.get_TrxName())
					.setOnlyActiveRecords(true).setClient_ID().firstId();
			debitNoteLine.setC_Tax_ID(C_Tax_ID);
			debitNoteLine.setPrice(amount);
			debitNoteLine.save();
		}
		try {
			debitNote.setDocAction(MInvoice.ACTION_Complete);
			if(debitNote.processIt(MInvoice.ACTION_Complete)) {
				debitNote.save();
				payment.addDescription("Se genero la Nota de Debito " + debitNote.getDocumentNo() + ", por el Monto de " + amount + ", por IGTF ");
				payment.save();
				addInvoiceWithHolding(debitNote, invoice, wr, wc, tax, amount, base, C_AllocationLine_ID);
			} else {
				debitNote.save();
				return "No se Completo Nota de Debito " + debitNote.getDocumentNo() + " por IGTF - Error: " + debitNote.getProcessMsg();
			}
		} catch (Exception e) {
			debitNote.save();
			return "No se Completo Nota de Debito " + debitNote.getDocumentNo() + " por IGTF - Error: " + debitNote.getProcessMsg() + " - " + e.getLocalizedMessage();
		}
		return null;
	}

	/**
	 * Get or Generate Payment Term for Debit Note
	 * @param payment MPayment
	 * @return int C_PaymentTerm_ID
	 */
	private int getC_PaymentTerm_ID(MPayment payment) {
		int C_PaymentTerm_ID = new Query(payment.getCtx(), MPaymentTerm.Table_Name, "Discount = 0 AND Discount2 = 0 AND IsValid = 'Y' AND PaymentTermUsage IN ('S', 'B')", payment.get_TrxName())
				.setClient_ID().setOnlyActiveRecords(true).firstId();
		if(C_PaymentTerm_ID <=0) {
			MPaymentTerm paymentTerm = new MPaymentTerm(payment.getCtx(), 0, payment.get_TrxName());
			paymentTerm.set_ValueOfColumn("AD_Client_ID", payment.getAD_Client_ID());
			paymentTerm.setAD_Org_ID(0);
			paymentTerm.setName("Contado para Nota de Debito");
			paymentTerm.setDescription("Termino de Pago para Notas de Debito");
			paymentTerm.setPaymentTermUsage(MPaymentTerm.PAYMENTTERMUSAGE_Both);
			paymentTerm.setIsValid(true);
			paymentTerm.save(null);
			C_PaymentTerm_ID = paymentTerm.getC_PaymentTerm_ID();
		}
		return C_PaymentTerm_ID;
	}

	/**
	 * Add Invoice With Holding
	 * @param debitNote MInvoice
	 * @param invoice MInvoice
	 * @param wr X_LCO_WithholdingRule
	 * @param wc X_LCO_WithholdingCalc 
	 * @param tax MTax
	 * @param amount BigDecimal
	 * @param base BigDecimal
	 * @param int C_AllocationLine_ID
	 */
	private void addInvoiceWithHolding(MInvoice debitNote, MInvoice invoice, X_LCO_WithholdingRule wr, X_LCO_WithholdingCalc wc, 
			MTax tax, BigDecimal amount, BigDecimal base, int C_AllocationLine_ID) {
		// insert new withholding record
		// with: type, tax, base amt, percent, tax amt, trx date, acct date, rule
		MLCOInvoiceWithholding iwh = new MLCOInvoiceWithholding(invoice.getCtx(), 0, invoice.get_TrxName());
		iwh.setAD_Org_ID(invoice.getAD_Org_ID());
		iwh.setC_Invoice_ID(invoice.getC_Invoice_ID());
		iwh.setDateAcct(debitNote.getDateAcct());
		iwh.setDateTrx(debitNote.getDateInvoiced());
		iwh.setIsCalcOnPayment(! wc.isCalcOnInvoice() );
		iwh.setIsTaxIncluded(false);
		iwh.setLCO_WithholdingRule_ID(wr.getLCO_WithholdingRule_ID());
		iwh.setLCO_WithholdingType_ID(wr.getLCO_WithholdingType_ID());
		iwh.setC_Tax_ID(tax.getC_Tax_ID());
		iwh.setPercent(tax.getRate());
		iwh.setProcessed(false);
		if (wc.getAmountRefunded() != null &&
				wc.getAmountRefunded().compareTo(Env.ZERO) > 0)
			amount = amount.subtract(wc.getAmountRefunded());
		iwh.setTaxAmt(amount);
		iwh.setTaxBaseAmt(base);
		iwh.set_ValueOfColumn("Subtrahend", wc.getAmountRefunded());
		iwh.setC_AllocationLine_ID(C_AllocationLine_ID);
		iwh.saveEx();
	}
	
	/**
	 * Get With Holding Rules
	 * @param payment MPayment
	 * @param invoice MInvoice
	 * @return List of X_LCO_WithholdingRule
	 */
	private List<X_LCO_WithholdingRule> getWithHoldingRules(MPayment payment, MInvoice invoice) {
		List<X_LCO_WithholdingRule> withHoldingRules = new ArrayList<>();
		// Fill variables normally needed
		// BP variables
		MBPartner bp = new MBPartner(payment.getCtx(), payment.getC_BPartner_ID(), payment.get_TrxName());
		int bp_isic_id = bp.get_ValueAsInt("LCO_ISIC_ID");
		int bp_taxpayertype_id = bp.get_ValueAsInt("LCO_TaxPayerType_ID");
		MBPartnerLocation mbpl = new MBPartnerLocation(invoice.getCtx(), invoice.getC_BPartner_Location_ID(), invoice.get_TrxName());
		MLocation bpl = MLocation.get(payment.getCtx(), mbpl.getC_Location_ID(), payment.get_TrxName());
		int bp_city_id = bpl.getC_City_ID();
		int bp_municipality_id = bpl.get_ValueAsInt("C_Municipality_ID");
		// OrgInfo variables
		MOrgInfo oi = MOrgInfo.get(payment.getCtx(), payment.getAD_Org_ID(), payment.get_TrxName());
		int org_isic_id = oi.get_ValueAsInt("LCO_ISIC_ID");
		int org_taxpayertype_id = oi.get_ValueAsInt("LCO_TaxPayerType_ID");
		MLocation ol = MLocation.get(payment.getCtx(), oi.getC_Location_ID(), payment.get_TrxName());
		int org_city_id = ol.getC_City_ID();
		int org_municipality_id = ol.get_ValueAsInt("C_Municipality_ID");

		StringBuilder sqlWhere = new StringBuilder("");
		sqlWhere.append("Type = 'IGTF'");
		sqlWhere.append(" AND IsSOTrx=? ");
		
		List<X_LCO_WithholdingType> wts = new Query(invoice.getCtx(), X_LCO_WithholdingType.Table_Name, sqlWhere.toString(), invoice.get_TrxName())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setParameters(invoice.isSOTrx())
				.list();
		
		for (X_LCO_WithholdingType wt : wts) {
			X_LCO_WithholdingRuleConf wrc = new Query(wt.getCtx(),
					X_LCO_WithholdingRuleConf.Table_Name,
					"LCO_WithholdingType_ID=?",
					wt.get_TrxName())
					.setOnlyActiveRecords(true)
					.setParameters(wt.getLCO_WithholdingType_ID())
					.first();
			if (wrc == null) {
				log.warning("No LCO_WithholdingRuleConf for LCO_WithholdingType = " + wt.getLCO_WithholdingType_ID());
				continue;
			}
			
			// look for applicable rules according to config fields (rule)
			StringBuffer wherer = new StringBuffer(" LCO_WithholdingType_ID=? AND ValidFrom <=? ");
			List<Object> paramsr = new ArrayList<Object>();
			paramsr.add(wt.getLCO_WithholdingType_ID());
			paramsr.add(payment.getDateAcct());
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
			if(wrc.isUseBPMunicipality()) {
				wherer.append(" AND LVE_BP_Municipaly_ID=? ");
				paramsr.add(bp_municipality_id);
				if (bp_municipality_id <= 0)
					log.warning("Possible Configuration error BP Municipality is used but not set");
			}
			if(wrc.isUseOrgMunicipality()) {
				wherer.append(" AND LVE_Org_Municipaly_ID=? ");
				paramsr.add(org_municipality_id);
				if (org_municipality_id <= 0)
					log.warning("Possible Configuration error Org Municipality is used but not set");
			}
			if(wrc.isUseTenderType()) {
				wherer.append(" AND TenderType=? ");
				paramsr.add(payment.getTenderType());
			}
			if(wrc.isUseCurrency()) {
				wherer.append(" AND C_Currency_ID=? ");
				paramsr.add(payment.getC_Currency_ID());
			}
			List<X_LCO_WithholdingRule> wrs = new Query(payment.getCtx(), X_LCO_WithholdingRule.Table_Name, wherer.toString(), payment.get_TrxName())
					.setOnlyActiveRecords(true)
					.setParameters(paramsr)
					.list();
			withHoldingRules.addAll(wrs);
		}
		return withHoldingRules;
	}

	private String clearInvoiceWithholdingAmtFromInvoice(MInvoice inv) {
		// Clear invoice withholding amount

		if (inv.is_ValueChanged("AD_Org_ID")
				|| inv.is_ValueChanged(MInvoice.COLUMNNAME_C_BPartner_ID)
				|| inv.is_ValueChanged(MInvoice.COLUMNNAME_C_DocTypeTarget_ID)) {

			boolean thereAreCalc;
			try {
				thereAreCalc = thereAreCalc(inv);
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Error looking for calc on invoice rules", e);
				return "Error looking for calc on invoice rules";
			}

			BigDecimal curWithholdingAmt = (BigDecimal) inv.get_Value("WithholdingAmt");
			if (thereAreCalc) {
				if (curWithholdingAmt != null) {
					inv.set_CustomColumn("WithholdingAmt", null);
				}
			} else {
				if (curWithholdingAmt == null) {
					inv.set_CustomColumn("WithholdingAmt", Env.ZERO);
				}
			}

		}

		return null;
	}

	private String clearInvoiceWithholdingAmtFromInvoiceLine(MInvoiceLine invline, String type) {

		if (   type.equals(IEventTopics.PO_BEFORE_NEW)
			|| type.equals(IEventTopics.PO_BEFORE_DELETE)
			|| (   type.equals(IEventTopics.PO_BEFORE_CHANGE)
				&& (   invline.is_ValueChanged("LineNetAmt")
					|| invline.is_ValueChanged("M_Product_ID")
					|| invline.is_ValueChanged("C_Charge_ID")
					|| invline.is_ValueChanged("IsActive")
					|| invline.is_ValueChanged("C_Tax_ID")
					)
				)
			)
		{
			// Clear invoice withholding amount
			MInvoice inv = invline.getParent();

			boolean thereAreCalc;
			try {
				thereAreCalc = thereAreCalc(inv);
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Error looking for calc on invoice rules", e);
				return "Error looking for calc on invoice rules";
			}

			BigDecimal curWithholdingAmt = (BigDecimal) inv.get_Value("WithholdingAmt");
			if (thereAreCalc) {
				if (curWithholdingAmt != null) {
					if (!LCO_MInvoice.setWithholdingAmtWithoutLogging(inv, null))
						return "Error saving C_Invoice clearInvoiceWithholdingAmtFromInvoiceLine";
				}
			} else {
				if (curWithholdingAmt == null) {
					if (!LCO_MInvoice.setWithholdingAmtWithoutLogging(inv, Env.ZERO))
						return "Error saving C_Invoice clearInvoiceWithholdingAmtFromInvoiceLine";
				}
			}
		}

		return null;
	}

	private boolean thereAreCalc(MInvoice inv) throws SQLException {
		boolean thereAreCalc = false;
		String sqlwccoi =
			"SELECT 1 "
			+ "  FROM LCO_WithholdingType wt, LCO_WithholdingCalc wc "
			+ " WHERE wt.LCO_WithholdingType_ID = wc.LCO_WithholdingType_ID";
		PreparedStatement pstmtwccoi = DB.prepareStatement(sqlwccoi, inv.get_TrxName());
		ResultSet rswccoi = null;
		try {
			rswccoi = pstmtwccoi.executeQuery();
			if (rswccoi.next())
				thereAreCalc = true;
		} catch (SQLException e) {
			throw e;
		} finally {
			DB.close(rswccoi, pstmtwccoi);
			rswccoi = null; pstmtwccoi = null;
		}
		return thereAreCalc;
	}

	private String validateWriteOffVsPaymentWithholdings(MPayment pay) {
		if (pay.getC_Invoice_ID() > 0) {
			// validate vs invoice of payment
			BigDecimal wo = pay.getWriteOffAmt();
			BigDecimal sumwhamt = Env.ZERO;
			sumwhamt = DB.getSQLValueBD(
					pay.get_TrxName(),
					"SELECT COALESCE (SUM (TaxAmt), 0) " +
					"FROM LCO_InvoiceWithholding " +
					"WHERE C_Invoice_ID = ? AND " +
					"IsActive = 'Y' AND " +
					"IsCalcOnPayment = 'Y' AND " +
					"Processed = 'N' AND " +
					"C_AllocationLine_ID IS NULL",
					pay.getC_Invoice_ID());
			if (sumwhamt == null)
				sumwhamt = Env.ZERO;
			MInvoice invoice = new MInvoice(pay.getCtx(), pay.getC_Invoice_ID(), pay.get_TrxName());
			if (invoice.isCreditMemo())
				sumwhamt = sumwhamt.negate();
			if (wo.compareTo(sumwhamt) < 0 && sumwhamt.compareTo(Env.ZERO) != 0)
				return Msg.getMsg(pay.getCtx(), "LCO_WriteOffLowerThanWithholdings");
		} else {
			// validate every C_PaymentAllocate
			String sql =
				"SELECT C_PaymentAllocate_ID " +
				"FROM C_PaymentAllocate " +
				"WHERE C_Payment_ID = ?";
			PreparedStatement pstmt = DB.prepareStatement(sql, pay.get_TrxName());
			ResultSet rs = null;
			try {
				pstmt.setInt(1, pay.getC_Payment_ID());
				rs = pstmt.executeQuery();
				while (rs.next()) {
					int palid = rs.getInt(1);
					MPaymentAllocate pal = new MPaymentAllocate(pay.getCtx(), palid, pay.get_TrxName());
					BigDecimal wo = pal.getWriteOffAmt();
					BigDecimal sumwhamt = Env.ZERO;
					sumwhamt = DB.getSQLValueBD(
							pay.get_TrxName(),
							"SELECT COALESCE (SUM (TaxAmt), 0) " +
							"FROM LCO_InvoiceWithholding " +
							"WHERE C_Invoice_ID = ? AND C_Payment_ID=? AND " +
							"IsActive = 'Y' AND " +
							"IsCalcOnPayment = 'Y' AND " +
							"Processed = 'N' AND " +
							"C_AllocationLine_ID IS NULL",
							pal.getC_Invoice_ID(), pay.getC_Payment_ID());
					if (sumwhamt == null)
						sumwhamt = Env.ZERO;
					MInvoice invoice = new MInvoice(pay.getCtx(), pal.getC_Invoice_ID(), pay.get_TrxName());
					if (invoice.isCreditMemo())
						sumwhamt = sumwhamt.negate();
					if (wo.compareTo(sumwhamt) < 0 && sumwhamt.compareTo(Env.ZERO) != 0)
						return Msg.getMsg(pay.getCtx(), "LCO_WriteOffLowerThanWithholdings") + ": WriteOffAmt=" + wo + ", RET=" + sumwhamt;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return e.getLocalizedMessage();
			} finally {
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
		}

		return null;
	}

	private String completePaymentWithholdings(MAllocationHdr ah) {
		MAllocationLine[] als = ah.getLines(true);
		for (int i = 0; i < als.length; i++) {
			MAllocationLine al = als[i];
			if (al.getC_Invoice_ID() > 0) {
				String sql =
					"SELECT LCO_InvoiceWithholding_ID " +
					"FROM LCO_InvoiceWithholding " +
					"LEFT JOIN LVE_VoucherWithholding ON LVE_VoucherWithholding.LVE_VoucherWithholding_ID = LCO_InvoiceWithholding.LVE_VoucherWithholding_ID " +
					"WHERE LCO_InvoiceWithholding.C_Invoice_ID = ? AND " +
					"LCO_InvoiceWithholding.IsActive = 'Y' AND " +
					"IsCalcOnPayment = 'Y' AND " +
					"LCO_InvoiceWithholding.Processed = 'N' AND " +
					"C_AllocationLine_ID IS NULL  " + 
					//"AND (LCO_InvoiceWithholding.C_Payment_ID = ? OR LCO_InvoiceWithholding.C_Payment_ID IS NULL)";
					"AND LCO_InvoiceWithholding.C_Payment_ID = ?";
				PreparedStatement pstmt = DB.prepareStatement(sql, ah.get_TrxName());
				ResultSet rs = null;
				try {
					pstmt.setInt(1, al.getC_Invoice_ID());
					pstmt.setInt(2, al.getC_Payment_ID());
					rs = pstmt.executeQuery();
					while (rs.next()) {
						int iwhid = rs.getInt(1);
						MLCOInvoiceWithholding iwh = new MLCOInvoiceWithholding(
								ah.getCtx(), iwhid, ah.get_TrxName());
						iwh.setC_AllocationLine_ID(al.getC_AllocationLine_ID());
						iwh.setDateAcct(ah.getDateAcct());
						iwh.setDateTrx(ah.getDateTrx());
						iwh.setProcessed(true);
						if (!iwh.save())
							return "Error saving LCO_InvoiceWithholding completePaymentWithholdings";
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return e.getLocalizedMessage();
				} finally {
					DB.close(rs, pstmt);
					rs = null; pstmt = null;
				}
			}
		}
		return null;
	}

	private String reversePaymentWithholdings(MAllocationHdr ah) {
		MAllocationLine[] als = ah.getLines(true);
		for (int i = 0; i < als.length; i++) {
			MAllocationLine al = als[i];
			if (al.getC_Invoice_ID() > 0) {
				String sql =
					"SELECT LCO_InvoiceWithholding_ID " +
					"FROM LCO_InvoiceWithholding " +
					"WHERE C_Invoice_ID = ? AND " +
					"IsActive = 'Y' AND " +
					"IsCalcOnPayment = 'Y' AND " +
					"Processed = 'Y' AND " +
					"C_AllocationLine_ID = ?";
				PreparedStatement pstmt = DB.prepareStatement(sql, ah.get_TrxName());
				ResultSet rs = null;
				try {
					pstmt.setInt(1, al.getC_Invoice_ID());
					pstmt.setInt(2, al.getC_AllocationLine_ID());
					rs = pstmt.executeQuery();
					while (rs.next()) {
						int iwhid = rs.getInt(1);
						MLCOInvoiceWithholding iwh = new MLCOInvoiceWithholding(
								ah.getCtx(), iwhid, ah.get_TrxName());
						iwh.setC_AllocationLine_ID(0);
						iwh.setProcessed(false);
						if (!iwh.save())
							return "Error saving LCO_InvoiceWithholding reversePaymentWithholdings";
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return e.getLocalizedMessage();
				} finally {
					DB.close(rs, pstmt);
					rs = null; pstmt = null;
				}
			}
		}
		return null;
	}

	/**
	 * Accounting like Doc_Allocation (Write off) vs (invoice withholding where iscalconpayment=Y)
	 * 20070807 - globalqss - instead of adding a new WriteOff post, find the
	 * current WriteOff and subtract from the posting
	 * @param ah
	 * @param event
	 * @return
	 */
	private String accountingForInvoiceWithholdingOnPayment(MAllocationHdr ah, Event event) {
		Doc doc = ah.getDoc();
		FactsEventData fed = getEventData(event);
		List<Fact> facts = fed.getFacts();

		// one fact per acctschema
		for (int i = 0; i < facts.size(); i++)
		{
			Fact fact = facts.get(i);
			MAcctSchema as = fact.getAcctSchema();

			MAllocationLine[] alloc_lines = ah.getLines(false);
			for (int j = 0; j < alloc_lines.length; j++) {
				BigDecimal tottax = new BigDecimal(0);

				MAllocationLine alloc_line = alloc_lines[j];
				DocLine_Allocation docLine = new DocLine_Allocation(alloc_line, doc);
				
				doc.setC_BPartner_ID(alloc_line.getC_BPartner_ID());

				int inv_id = alloc_line.getC_Invoice_ID();
				if (inv_id <= 0)
					continue;
				MInvoice invoice = null;
				invoice = new MInvoice (ah.getCtx(), alloc_line.getC_Invoice_ID(), ah.get_TrxName());
				if (invoice == null || invoice.getC_Invoice_ID() == 0)
					continue;
				doc.setDateAcct(ah.getDateAcct());
				docLine.setDateAcct(ah.getDateAcct());
				/*String sql = 
				  "SELECT i.C_Tax_ID, NVL(SUM(i.TaxBaseAmt),0) AS TaxBaseAmt, NVL(SUM(i.TaxAmt),0) AS TaxAmt, t.Name, t.Rate, t.IsSalesTax "
				 + " FROM LCO_InvoiceWithholding i, C_Tax t "
				+ " WHERE i.C_Invoice_ID = ? AND " +
						 "i.IsCalcOnPayment = 'Y' AND " +
						 "i.IsActive = 'Y' AND " +
						 "i.Processed = 'Y' AND " +
						 "i.C_AllocationLine_ID = ? AND " +
						 "i.C_Tax_ID = t.C_Tax_ID "
				+ "GROUP BY i.C_Tax_ID, t.Name, t.Rate, t.IsSalesTax";
				*/
			String sql = 
					  "SELECT i.C_Tax_ID,NVL(SUM(i.TaxBaseAmt),0) AS TaxBaseAmt, NVL(SUM(i.TaxAmt),0) AS TaxAmt, " +
					  "COALESCE(SUM( currencyconvert(i.TaxBaseAmt,ci.c_currency_id, 205, " +
					  "i.dateacct, ci.c_conversiontype_id, i.ad_client_id, i.ad_org_id) ),0) AS TaxBaseAmtVE, " +
					  "COALESCE(SUM(currencyconvert(i.TaxAmt ,ci.c_currency_id, 205, " +
					  "i.dateacct, ci.c_conversiontype_id, i.ad_client_id, i.ad_org_id)),0) AS TaxAmtVE, t.Name, t.Rate, t.IsSalesTax "
					 + " FROM LCO_InvoiceWithholding i, C_Tax t, C_Invoice ci, C_AllocationLine al "
					+ " WHERE i.C_Invoice_ID = ? AND " +
							 "i.IsCalcOnPayment = 'Y' AND " +
							 "i.IsActive = 'Y' AND " +
							 "i.Processed = 'Y' AND " +
							 "i.C_AllocationLine_ID = ? AND " +
							 "i.C_Tax_ID = t.C_Tax_ID AND " +
							 "i.C_Invoice_ID = ci.C_Invoice_ID AND " +
							 "i.C_AllocationLine_ID = al.C_AllocationLine_ID "
					+ "GROUP BY i.C_Tax_ID, t.Name, t.Rate, t.IsSalesTax ";
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try
				{
					pstmt = DB.prepareStatement(sql, ah.get_TrxName());
					pstmt.setInt(1, invoice.getC_Invoice_ID());
					pstmt.setInt(2, alloc_line.getC_AllocationLine_ID());
					rs = pstmt.executeQuery();
					while (rs.next()) {
						int tax_ID = rs.getInt(1);
						BigDecimal taxBaseAmt = rs.getBigDecimal(2);
						BigDecimal amount = rs.getBigDecimal(3);
						//String name = rs.getString(4);
						//BigDecimal rate = rs.getBigDecimal(5);
						//boolean salesTax = rs.getString(6).equals("Y") ? true : false;
//						BigDecimal taxBaseAmtVE = rs.getBigDecimal(4);
						//BigDecimal amountVE = rs.getBigDecimal(5);
						String name = rs.getString(6);
						BigDecimal rate = rs.getBigDecimal(7);
						boolean salesTax = rs.getString(8).equals("Y") ? true : false;
						DocTax taxLine = new DocTax(tax_ID, name, rate, 
								taxBaseAmt, amount, salesTax);
						
						/*if (amount != null && amount.signum() != 0)
						{
							FactLine tl = null;
							if (invoice.isSOTrx()) {
								tl = fact.createLine(null, taxLine.getAccount(DocTax.ACCTTYPE_TaxDue, as),
										as.getC_Currency_ID(), amount, null);
							} else {
								tl = fact.createLine(null, taxLine.getAccount(taxLine.getAPTaxType(), as),
										as.getC_Currency_ID(), null, amount);
							}
							if (tl != null)
								tl.setC_Tax_ID(taxLine.getC_Tax_ID());
							tottax = tottax.add(amount);
						}*/
						if (amount != null && amount.signum() != 0)
						{
							FactLine tl = null;
							if ((invoice.isSOTrx() && invoice.getC_DocTypeTarget().getDocBaseType().compareTo("ARI")==0)){
							//if ((invoice.isSOTrx() && invoice.getC_DocTypeTarget().getDocBaseType().compareTo("ARI")==0) || (!invoice.isSOTrx() && invoice.getC_DocTypeTarget().getDocBaseType().compareTo("APC")==0)) {
								tl = fact.createLine(docLine, taxLine.getAccount(DocTax.ACCTTYPE_TaxDue, as),
										ah.getC_Currency_ID(), amount, null);
							} 
							//** si es NC proveedor es un iva en compras
							else if (!invoice.isSOTrx() && invoice.getC_DocTypeTarget().getDocBaseType().compareTo("APC")==0){
								tl = fact.createLine(docLine, taxLine.getAccount(taxLine.getAPTaxType(), as),
										ah.getC_Currency_ID(), null, amount);
							} else {
								tl = fact.createLine(docLine, taxLine.getAccount(taxLine.getAPTaxType(), as),
										ah.getC_Currency_ID(), null, amount);
							}
							if (tl != null)
								tl.setC_Tax_ID(taxLine.getC_Tax_ID());
							tottax = tottax.add(amount);
						}
					}
				} catch (Exception e) {
					log.log(Level.SEVERE, sql, e);
					return "Error posting C_InvoiceTax from LCO_InvoiceWithholding";
				} finally {
					DB.close(rs, pstmt);
					rs = null; pstmt = null;
				}

				//	Write off		DR
				if (Env.ZERO.compareTo(tottax) != 0)
				{
					// First try to find the WriteOff posting record
					FactLine[] factlines = fact.getLines();
					boolean foundflwriteoff = false;
					for (int ifl = 0; ifl < factlines.length; ifl++) {
						FactLine fl = factlines[ifl];
						if (fl.getAccount().equals(doc.getAccount(Doc.ACCTTYPE_WriteOff, as))) {
							foundflwriteoff = true;
							// old balance = DB - CR
							BigDecimal balamt = fl.getAmtSourceDr().subtract(fl.getAmtSourceCr());
							// new balance = old balance +/- tottax
							BigDecimal newbalamt = Env.ZERO;
							if (invoice.isSOTrx())
								newbalamt = balamt.subtract(tottax);
							else if (!invoice.isSOTrx() && invoice.getC_DocTypeTarget().getDocBaseType().compareTo("APC")==0)
								newbalamt = balamt.subtract(tottax);
							else
								newbalamt = balamt.add(tottax);
							if (Env.ZERO.compareTo(newbalamt) == 0) {
								// both zeros, remove the line
								fact.remove(fl);
							} else if (Env.ZERO.compareTo(newbalamt) > 0) {
								fl.setAmtAcct(fl.getC_Currency_ID(), Env.ZERO, newbalamt);
								fl.setAmtSource(fl.getC_Currency_ID(), Env.ZERO, newbalamt);
								fl.convert();
							} else {
								fl.setAmtAcct(fl.getC_Currency_ID(), newbalamt, Env.ZERO);
								fl.setAmtSource(fl.getC_Currency_ID(), newbalamt, Env.ZERO);
								fl.convert();
							}
							break;
						}
					}

					if (! foundflwriteoff) {
						// Create a new line - never expected to arrive here as it must always be a write-off line
						DocLine_Allocation line = new DocLine_Allocation(alloc_line, doc);
						FactLine fl = null;
						if (invoice.isSOTrx()) {
							fl = fact.createLine (line, doc.getAccount(Doc.ACCTTYPE_WriteOff, as),
									ah.getC_Currency_ID(), null, tottax);
							fl.convert();
						} else {
							fl = fact.createLine (line, doc.getAccount(Doc.ACCTTYPE_WriteOff, as),
									ah.getC_Currency_ID(), tottax, null);
							fl.convert();
						}
						if (fl != null)
							fl.setAD_Org_ID(ah.getAD_Org_ID());
					}

				}

			}

		}

		return null;
	}

	private String completeInvoiceWithholding(MInvoice inv) {

		// Fill DateAcct and DateTrx with final dates from Invoice
		String upd_dates =
			"UPDATE LCO_InvoiceWithholding "
			 + "   SET DateAcct = "
			 + "          (SELECT DateAcct "
			 + "             FROM C_Invoice "
			 + "            WHERE C_Invoice.C_Invoice_ID = LCO_InvoiceWithholding.C_Invoice_ID), "
			 + "       DateTrx = "
			 + "          (SELECT DateInvoiced "
			 + "             FROM C_Invoice "
			 + "            WHERE C_Invoice.C_Invoice_ID = LCO_InvoiceWithholding.C_Invoice_ID) "
			 + " WHERE C_Invoice_ID = ? ";
		int noupddates = DB.executeUpdate(upd_dates, inv.getC_Invoice_ID(), inv.get_TrxName());
		if (noupddates == -1)
			return "Error updating dates on invoice withholding";

		// Set processed for isCalcOnInvoice records
		String upd_proc =
			"UPDATE LCO_InvoiceWithholding "
			 + "   SET Processed = 'Y' "
			 + " WHERE C_Invoice_ID = ? AND IsCalcOnPayment = 'N'";
		int noupdproc = DB.executeUpdate(upd_proc, inv.getC_Invoice_ID(), inv.get_TrxName());
		if (noupdproc == -1)
			return "Error updating processed on invoice withholding";

		return null;
	}

	private String translateWithholdingToTaxes(MInvoice inv) {
		BigDecimal sumit = new BigDecimal(0);

		MDocType dt = new MDocType(inv.getCtx(), inv.getC_DocTypeTarget_ID(), inv.get_TrxName());
		String genwh = dt.get_ValueAsString("GenerateWithholding");
		if (genwh == null || genwh.equals("N")) {
			// document configured to not manage withholdings - delete any
			String sqldel = "DELETE FROM LCO_InvoiceWithholding "
				+ " WHERE C_Invoice_ID = ?";
			PreparedStatement pstmtdel = null;
			try
			{
				// Delete previous records generated
				pstmtdel = DB.prepareStatement(sqldel,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE, inv.get_TrxName());
				pstmtdel.setInt(1, inv.getC_Invoice_ID());
				int nodel = pstmtdel.executeUpdate();
				log.config("LCO_InvoiceWithholding deleted="+nodel);
			} catch (Exception e) {
				log.log(Level.SEVERE, sqldel, e);
				return "Error creating C_InvoiceTax from LCO_InvoiceWithholding -delete";
			} finally {
				DB.close(pstmtdel);
				pstmtdel = null;
			}
			inv.set_CustomColumn("WithholdingAmt", Env.ZERO);

		} else {
			// translate withholding to taxes
			String sql =
				  "SELECT C_Tax_ID, NVL(SUM(TaxBaseAmt),0) AS TaxBaseAmt, NVL(SUM(TaxAmt),0) AS TaxAmt "
				 + " FROM LCO_InvoiceWithholding "
				+ " WHERE C_Invoice_ID = ? AND IsCalcOnPayment = 'N' AND IsActive = 'Y' "
				+ "GROUP BY C_Tax_ID";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, inv.get_TrxName());
				pstmt.setInt(1, inv.getC_Invoice_ID());
				rs = pstmt.executeQuery();
				while (rs.next()) {
					MInvoiceTax it = new MInvoiceTax(inv.getCtx(), 0, inv.get_TrxName());
					it.setAD_Org_ID(inv.getAD_Org_ID());
					it.setC_Invoice_ID(inv.getC_Invoice_ID());
					it.setC_Tax_ID(rs.getInt(1));
					it.setTaxBaseAmt(rs.getBigDecimal(2));
					it.setTaxAmt(rs.getBigDecimal(3).negate());
					sumit = sumit.add(rs.getBigDecimal(3));
					if (!it.save())
						return "Error creating C_InvoiceTax from LCO_InvoiceWithholding - save InvoiceTax";
				}
				BigDecimal actualamt = (BigDecimal) inv.get_Value("WithholdingAmt");
				if (actualamt == null)
					actualamt = new BigDecimal(0);
				if (actualamt.compareTo(sumit) != 0 || sumit.signum() != 0) {
					inv.set_CustomColumn("WithholdingAmt", sumit);
					// Subtract to invoice grand total the value of withholdings
					BigDecimal gt = inv.getGrandTotal();
					inv.setGrandTotal(gt.subtract(sumit));
					inv.saveEx();  // need to save here in order to let apply get the right total
				}

				if (sumit.signum() != 0) {
					// GrandTotal changed!  If there are payment schedule records they need to be recalculated
					// subtract withholdings from the first installment
					BigDecimal toSubtract = sumit;
					for (MInvoicePaySchedule ips : MInvoicePaySchedule.getInvoicePaySchedule(inv.getCtx(), inv.getC_Invoice_ID(), 0, inv.get_TrxName())) {
						if (ips.getDueAmt().compareTo(toSubtract) >= 0) {
							ips.setDueAmt(ips.getDueAmt().subtract(toSubtract));
							toSubtract = Env.ZERO;
						} else {
							toSubtract = toSubtract.subtract(ips.getDueAmt());
							ips.setDueAmt(Env.ZERO);
						}
						if (!ips.save()) {
							return "Error saving Invoice Pay Schedule subtracting withholdings";
						}
						if (toSubtract.signum() <= 0)
							break;
					}
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, sql, e);
				return "Error creating C_InvoiceTax from LCO_InvoiceWithholding - select InvoiceTax";
			} finally {
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
		}

		return null;
	}
	
	public class DocLine_Allocation_WH extends DocLine_Allocation {
		public DocLine_Allocation_WH (MAllocationLine line, Doc doc) {
			super (line, doc);
		}
		
		public void setCurrencyRate(BigDecimal currencyRate) {
			super.setCurrencyRate(currencyRate);
		}
	}

}	//	LCO_ValidatorWH
