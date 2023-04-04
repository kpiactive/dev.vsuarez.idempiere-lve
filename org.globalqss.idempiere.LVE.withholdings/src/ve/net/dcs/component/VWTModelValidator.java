package ve.net.dcs.component;

//import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
//import java.util.Vector;
//import java.util.logging.Level;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
//import org.compiere.acct.Doc;
//import org.compiere.acct.DocLine;
//import org.compiere.acct.DocTax;
//import org.compiere.acct.Fact;
//import org.compiere.acct.FactLine;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.MAllocationHdr;
//import org.compiere.model.MAcctSchema;
//import org.compiere.model.MAllocationHdr;
//import org.compiere.model.MAllocationLine;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MSequence;
import org.compiere.model.MSysConfig;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_C_BPartner;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.globalqss.model.MLCOInvoiceWithholding;
import org.globalqss.model.MLCOWithholdingType;
import org.globalqss.model.X_LCO_InvoiceWithholding;
import org.globalqss.model.X_LCO_TaxIdType;
import org.osgi.service.event.Event;

import ve.net.dcs.model.MLVEVoucherWithholding;

public class VWTModelValidator extends AbstractEventHandler {

	private static CLogger log = CLogger.getCLogger(VWTModelValidator.class);

	@Override
	protected void initialize() {
		registerTableEvent(IEventTopics.DOC_AFTER_COMPLETE, I_C_Invoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_POST, I_C_Invoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, I_C_Invoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_VOID, I_C_Invoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_REACTIVATE, I_C_Invoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_REVERSEACCRUAL, I_C_Invoice.Table_Name);
		registerTableEvent(IEventTopics.DOC_BEFORE_REVERSECORRECT, I_C_Invoice.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_NEW, I_C_BPartner.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_CHANGE, I_C_BPartner.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MLVEVoucherWithholding.Table_Name);
		registerTableEvent(IEventTopics.PO_BEFORE_NEW, MLVEVoucherWithholding.Table_Name);
		registerTableEvent(IEventTopics.DOC_AFTER_COMPLETE, MLVEVoucherWithholding.Table_Name);
	}

	@Override
	protected void doHandleEvent(Event event) {

		PO po = getPO(event);
		String type = event.getTopic();
		log.info(po.get_TableName() + " Type: " + type);
		
		if (po.get_TableName().equals(MLVEVoucherWithholding.Table_Name) ) {
			
			MLVEVoucherWithholding voucher = (MLVEVoucherWithholding) po;
			
			if(type.equals(IEventTopics.PO_AFTER_CHANGE)){
				if(validateWithholdingNo(voucher)){
					String sqlwhere = " LVE_VoucherWithholding_ID = ?";
					List<MLCOInvoiceWithholding> invoiceW = new Query(po.getCtx(), X_LCO_InvoiceWithholding.Table_Name, sqlwhere, po.get_TrxName()).setOnlyActiveRecords(true).setParameters(voucher.get_ID()).list();
					
					for (MLCOInvoiceWithholding mlcoInvoiceWithholding : invoiceW) {
						mlcoInvoiceWithholding.setDateAcct((Timestamp)voucher.get_Value("DateAcct"));
						mlcoInvoiceWithholding.setDateTrx(voucher.getDateTrx());
						mlcoInvoiceWithholding.saveEx();
					}
				}
			}
			if(type.equals(IEventTopics.PO_BEFORE_NEW)){
				validateWithholdingNo(voucher);
			}
			    
		
		}
		
		if(po.get_TableName().equals(I_C_BPartner.Table_Name)) {
			X_C_BPartner partner = (X_C_BPartner) po;
			String taxID = partner.getTaxID();
			X_LCO_TaxIdType taxIdType = null;
			if(partner.get_ValueAsInt("LCO_TaxIdType_ID") > 0)
				taxIdType = new X_LCO_TaxIdType(partner.getCtx(), partner.get_ValueAsInt("LCO_TaxIdType_ID"), partner.get_TrxName());
			if (type.equals(IEventTopics.PO_BEFORE_CHANGE) || type.equals(IEventTopics.PO_BEFORE_NEW)) {
				if(partner.is_ValueChanged(X_C_BPartner.COLUMNNAME_TaxID) && !Util.isEmpty(taxID, true) && taxIdType != null) {
					if(!taxIdType.isAllowRepeated()) {
						int value = 0;
						String sql = "SELECT 1 FROM C_BPartner WHERE LCO_taxIDType_ID = ? AND TaxID = ? AND C_BPartner_ID != ? AND AD_Client_ID =? AND AD_Org_ID =?";
						value = DB.getSQLValue(partner.get_TrxName(), sql, taxIdType.getLCO_TaxIdType_ID(), taxID, partner.getC_BPartner_ID(), partner.getAD_Client_ID(), partner.getAD_Org_ID());
						if (value > 0)
							throw new RuntimeException("Tercero Ya Existe");
					}
					if(!taxIdType.isAllowSpecialCharacters())
						if (taxID.replaceAll("[\\w\\-]+","").matches("[\\W\\s]+"))
							throw new RuntimeException("Caracteres no válidos en número de identificación");
				}
			} 
		} else if (po.get_TableName().equals(I_C_Invoice.Table_Name) && type.equals(IEventTopics.DOC_AFTER_COMPLETE)) {

			MInvoice invoice = (MInvoice) po;
			
			if (!invoice.isReversal()){
				String sqlwhere = " C_Invoice_ID = ? AND LVE_VoucherWithholding_ID IS NULL";
				List<MLCOInvoiceWithholding> invoiceW = new Query(po.getCtx(), X_LCO_InvoiceWithholding.Table_Name, sqlwhere, po.get_TrxName()).setOnlyActiveRecords(true).setParameters(invoice.get_ID()).setOrderBy("LCO_WithholdingType_ID").list();

				int LCO_WithholdingType_ID = 0;
				MLVEVoucherWithholding voucher = null;
				List<MLVEVoucherWithholding> listVoucher = new ArrayList<MLVEVoucherWithholding>();

				for (MLCOInvoiceWithholding iw : invoiceW) {
					if (LCO_WithholdingType_ID != iw.getLCO_WithholdingType_ID()) {
						LCO_WithholdingType_ID = iw.getLCO_WithholdingType_ID();
						voucher = new MLVEVoucherWithholding(po.getCtx(), 0, po.get_TrxName());
						voucher.setAD_Org_ID(po.getAD_Org_ID());
						voucher.set_ValueOfColumn("AD_Client_ID", po.getAD_Client_ID());
						voucher.set_ValueOfColumn("DateAcct", invoice.getDateAcct());
						voucher.setDateTrx(invoice.getDateAcct());
						voucher.setC_BPartner_ID(invoice.getC_BPartner_ID());
						voucher.setLCO_WithholdingType_ID(LCO_WithholdingType_ID);
						voucher.setC_Invoice_ID(iw.getC_Invoice_ID());
						voucher.setIsSOTrx(invoice.isSOTrx());
						voucher.saveEx();
						listVoucher.add(voucher);
					}

					iw.set_ValueOfColumn("LVE_VoucherWithholding_ID", voucher.get_ID());
					iw.saveEx();
				}

				for (MLVEVoucherWithholding v : listVoucher) {
					try {
						v.setDocAction(DocAction.ACTION_Complete);
						if(!v.processIt(DocAction.ACTION_Complete))
							addErrorMessage(event, "No se Completo Comprobante de Retencion - " + v.getProcessMsg());
						else
							v.setProcessed(true);
					} catch (Exception e) {
						addErrorMessage(event, "No se Completo Comprobante de Retencion - " + e.getLocalizedMessage());
					}
					v.saveEx();
				}
			}
			

		} else if (po.get_TableName().equals(I_C_Invoice.Table_Name) && (type.equals(IEventTopics.DOC_BEFORE_VOID) || type.equals(IEventTopics.DOC_BEFORE_REACTIVATE) || type.equals(IEventTopics.DOC_BEFORE_REVERSEACCRUAL) || type.equals(IEventTopics.DOC_BEFORE_REVERSECORRECT))) {
			MInvoice invoice = (MInvoice) po;
			
			List<MLCOInvoiceWithholding> list = new Query(invoice.getCtx(), MLCOInvoiceWithholding.Table_Name, " C_Invoice_ID = ? ", invoice.get_TrxName()).setParameters(invoice.getC_Invoice_ID()).setOrderBy(MLCOInvoiceWithholding.COLUMNNAME_C_Invoice_ID).list();
			
			if (list.size() > 0){
				for (MLCOInvoiceWithholding mlcoInvoiceWithholding : list) {
					if (mlcoInvoiceWithholding.get_ValueAsInt("LVE_VoucherWithholding_ID") > 0) {
						MLVEVoucherWithholding vw = new MLVEVoucherWithholding(invoice.getCtx(), mlcoInvoiceWithholding.get_ValueAsInt("LVE_VoucherWithholding_ID"), invoice.get_TrxName());
						if (vw.getDocStatus().equalsIgnoreCase(DocAction.STATUS_Completed) || vw.getDocStatus().equalsIgnoreCase(DocAction.STATUS_Drafted)) {
							throw new AdempiereException("RETENCIÓN. "+vw.getWithholdingNo()+", Debe Anular primero la Retención Asociada");
						}
						mlcoInvoiceWithholding.deleteEx(true);
					}
				}
			}
		}
		else if (po.get_TableName().equals(I_C_Invoice.Table_Name) && type.equals(IEventTopics.DOC_BEFORE_COMPLETE)) {

			String msgExistCN = Msg.translate(Env.getCtx(), "AlreadyExists") + ": " + Msg.getElement(Env.getCtx(), "LVE_controlNumber");
//			String msgExistINo = Msg.translate(Env.getCtx(), "AlreadyExists") + ": " + Msg.getElement(Env.getCtx(), "LVE_POInvoiceNo");
			String msgMandataryCN = Msg.translate(Env.getCtx(), "FillMandatory") + ": " + Msg.getElement(Env.getCtx(), "LVE_controlNumber");
			String msgMandataryINo = Msg.translate(Env.getCtx(), "FillMandatory") + ": " + Msg.getElement(Env.getCtx(), "LVE_POInvoiceNo");
			String msgSeqNotFound = Msg.translate(Env.getCtx(), "SequenceDocNotFound") + " " + Msg.getElement(Env.getCtx(), "LVE_ControlNoSequence_ID");

			String where = "AD_Org_ID=? AND C_BPartner_ID=? AND C_Invoice_ID!=? AND IsSOTrx=? AND DocStatus IN ('CO','CL') ";
			MInvoice invoice = (MInvoice) po;
			MDocType docType = (MDocType) invoice.getC_DocType();

			if (invoice.getReversal_ID() == 0)
				if(MSysConfig.getValue("LVE_ValidateControlNumber", "Y", invoice.getAD_Client_ID()).compareTo("Y")==0 && docType.get_ValueAsBoolean("isControlNoDocument")) {
					if (invoice.isSOTrx()) {
						String controlSequence = null;
						if (invoice.get_Value("LVE_controlNumber") == null) {
							if (docType.get_Value("LVE_ControlNoSequence_ID") == null)
								throw new AdempiereException(msgSeqNotFound);
							
							MSequence seq = new MSequence(Env.getCtx(), (int) docType.get_Value("LVE_ControlNoSequence_ID"), po.get_TrxName());
							controlSequence = MSequence.getDocumentNoFromSeq(seq, po.get_TrxName(), invoice);
							Query query = new Query(Env.getCtx(), MInvoice.Table_Name, where + "AND LVE_controlNumber=?", po.get_TrxName());
							while (query.setParameters(invoice.getAD_Org_ID(), invoice.getC_BPartner_ID(), invoice.get_ID(), invoice.isSOTrx(), controlSequence).count() > 0) {
								seq.setCurrentNext(seq.getCurrentNext() + 1);
								seq.saveEx();
								controlSequence = MSequence.getDocumentNoFromSeq(seq, po.get_TrxName(), invoice);
							}
							invoice.set_ValueOfColumn("LVE_controlNumber", controlSequence);
							invoice.saveEx();
						} else {
							boolean existCN = new Query(Env.getCtx(), MInvoice.Table_Name, where + "AND LVE_controlNumber=?", po.get_TrxName()).setParameters(invoice.getAD_Org_ID(), invoice.getC_BPartner_ID(), invoice.get_ID(), invoice.isSOTrx(), invoice.get_Value("LVE_controlNumber")).count() > 0;
							if (existCN) {
								throw new AdempiereException(msgExistCN);
							}
						}
					} else {
						if (invoice.get_Value("LVE_controlNumber") == null) {
							throw new AdempiereException(msgMandataryCN);
						} else if (invoice.get_Value("LVE_POInvoiceNo") == null) {
							throw new AdempiereException(msgMandataryINo);
						} else {
							boolean existCN = new Query(Env.getCtx(), MInvoice.Table_Name, where + "AND LVE_controlNumber=? AND LVE_POInvoiceNo=?", po.get_TrxName()).setParameters(invoice.getAD_Org_ID(), invoice.getC_BPartner_ID(), invoice.get_ID(), invoice.isSOTrx(), invoice.get_Value("LVE_controlNumber"), invoice.get_Value("LVE_POInvoiceNo")).count() > 0;
							if (existCN) {
								throw new AdempiereException(msgExistCN);
							}
						}
					}
				}
		} else if(po.get_TableName().equals(I_C_Invoice.Table_Name) && type.equals(IEventTopics.DOC_AFTER_POST)) {
			MInvoice invoice = (MInvoice) po;
			MAllocationHdr[] allocations = MAllocationHdr.getOfInvoice(invoice.getCtx(), invoice.getC_Invoice_ID(), invoice.get_TrxName());
			for(MAllocationHdr allocation : allocations) {
				if(!allocation.isPosted()) {
					String ignoreError = DocumentEngine.postImmediate(allocation.getCtx(), allocation.getAD_Client_ID(), allocation.get_Table_ID(), allocation.get_ID(), true, allocation.get_TrxName());
					if (!Util.isEmpty(ignoreError, true)) {
						log.warning("Error posting " + allocation + ". Error="+ignoreError);
					} else {
						allocation.load(allocation.get_TrxName());
					}
				}
			}
		}

	}

	private boolean validateWithholdingNo(MLVEVoucherWithholding voucher) {
		
		MLCOWithholdingType withHoldingType = new MLCOWithholdingType(voucher.getCtx(), voucher.getLCO_WithholdingType_ID(), voucher.get_TrxName());	
		PreparedStatement pst = null;
		boolean isValidate = false;
		String sql  = "";
		if(withHoldingType.isSOTrx())
			sql = "Select LVE_VoucherWithholding_ID from LVE_VoucherWithholding where  withholdingno= '"+voucher.getWithholdingNo()+"' AND LCO_WithholdingType_ID="+voucher.getLCO_WithholdingType_ID()+" AND C_Bpartner_ID ="+voucher.getC_BPartner_ID()+" AND LVE_VoucherWithholding_ID != "+voucher.getLVE_VoucherWithholding_ID()+" AND docstatus = 'CO'";

		else
			sql = "Select LVE_VoucherWithholding_ID from LVE_VoucherWithholding where  withholdingno= '"+voucher.getWithholdingNo()+"' AND LCO_WithholdingType_ID="+voucher.getLCO_WithholdingType_ID()+" AND LVE_VoucherWithholding_ID != "+voucher.getLVE_VoucherWithholding_ID()+" AND docstatus = 'CO' AND AD_Org_ID = "+voucher.getAD_Org_ID();	
		
		try {
			 pst = DB.prepareStatement(sql, null);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				isValidate = true;
			}
			else{ 
				String msj = "El nro de retención "+voucher.getWithholdingNo()+ " ya existe para este tipo de retención"; 
				if(withHoldingType.isSOTrx())
					msj = msj + " y este tercero";
			    throw new RuntimeException(msj); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	finally{
    		DB.close(pst);
    		pst = null;
    	}	

		return isValidate;
	}

	
	
}