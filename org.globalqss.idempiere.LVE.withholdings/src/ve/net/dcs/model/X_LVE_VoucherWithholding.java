/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package ve.net.dcs.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for LVE_VoucherWithholding
 *  @author iDempiere (generated) 
 *  @version Release 8.2 - $Id$ */
public class X_LVE_VoucherWithholding extends PO implements I_LVE_VoucherWithholding, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20210626L;

    /** Standard Constructor */
    public X_LVE_VoucherWithholding (Properties ctx, int LVE_VoucherWithholding_ID, String trxName)
    {
      super (ctx, LVE_VoucherWithholding_ID, trxName);
      /** if (LVE_VoucherWithholding_ID == 0)
        {
			setC_BPartner_ID (0);
			setDateTrx (new Timestamp( System.currentTimeMillis() ));
// @#Date@
			setIsSOTrx (false);
// N
			setLCO_WithholdingType_ID (0);
			setLVE_VoucherWithholding_ID (0);
			setLVE_WHBankAccount_ID (0);
			setLVE_WHPaymentDocType_ID (0);
			setProcessed (false);
        } */
    }

    /** Load Constructor */
    public X_LVE_VoucherWithholding (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder ("X_LVE_VoucherWithholding[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
    {
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_Name)
			.getPO(getC_BPartner_ID(), get_TrxName());	}

	/** Set Business Partner .
		@param C_BPartner_ID 
		Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1) 
			set_Value (COLUMNNAME_C_BPartner_ID, null);
		else 
			set_Value (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner .
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Invoice getC_Invoice() throws RuntimeException
    {
		return (org.compiere.model.I_C_Invoice)MTable.get(getCtx(), org.compiere.model.I_C_Invoice.Table_Name)
			.getPO(getC_Invoice_ID(), get_TrxName());	}

	/** Set Invoice.
		@param C_Invoice_ID 
		Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID)
	{
		if (C_Invoice_ID < 1) 
			set_Value (COLUMNNAME_C_Invoice_ID, null);
		else 
			set_Value (COLUMNNAME_C_Invoice_ID, Integer.valueOf(C_Invoice_ID));
	}

	/** Get Invoice.
		@return Invoice Identifier
	  */
	public int getC_Invoice_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Invoice_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Payment getC_Payment() throws RuntimeException
    {
		return (org.compiere.model.I_C_Payment)MTable.get(getCtx(), org.compiere.model.I_C_Payment.Table_Name)
			.getPO(getC_Payment_ID(), get_TrxName());	}

	/** Set Payment.
		@param C_Payment_ID 
		Payment identifier
	  */
	public void setC_Payment_ID (int C_Payment_ID)
	{
		if (C_Payment_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_Payment_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_Payment_ID, Integer.valueOf(C_Payment_ID));
	}

	/** Get Payment.
		@return Payment identifier
	  */
	public int getC_Payment_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Payment_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Create lines from.
		@param CreateFrom 
		Process which will generate a new document lines based on an existing document
	  */
	public void setCreateFrom (String CreateFrom)
	{
		set_Value (COLUMNNAME_CreateFrom, CreateFrom);
	}

	/** Get Create lines from.
		@return Process which will generate a new document lines based on an existing document
	  */
	public String getCreateFrom () 
	{
		return (String)get_Value(COLUMNNAME_CreateFrom);
	}

	/** Set Account Date.
		@param DateAcct 
		Accounting Date
	  */
	public void setDateAcct (Timestamp DateAcct)
	{
		set_Value (COLUMNNAME_DateAcct, DateAcct);
	}

	/** Get Account Date.
		@return Accounting Date
	  */
	public Timestamp getDateAcct () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateAcct);
	}

	/** Set Date From.
		@param DateFrom 
		Starting date for a range
	  */
	public void setDateFrom (Timestamp DateFrom)
	{
		set_Value (COLUMNNAME_DateFrom, DateFrom);
	}

	/** Get Date From.
		@return Starting date for a range
	  */
	public Timestamp getDateFrom () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateFrom);
	}

	/** Set Date To.
		@param DateTo 
		End date of a date range
	  */
	public void setDateTo (Timestamp DateTo)
	{
		set_Value (COLUMNNAME_DateTo, DateTo);
	}

	/** Get Date To.
		@return End date of a date range
	  */
	public Timestamp getDateTo () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateTo);
	}

	/** Set Transaction Date.
		@param DateTrx 
		Transaction Date
	  */
	public void setDateTrx (Timestamp DateTrx)
	{
		set_Value (COLUMNNAME_DateTrx, DateTrx);
	}

	/** Get Transaction Date.
		@return Transaction Date
	  */
	public Timestamp getDateTrx () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateTrx);
	}

	/** DocAction AD_Reference_ID=135 */
	public static final int DOCACTION_AD_Reference_ID=135;
	/** Complete = CO */
	public static final String DOCACTION_Complete = "CO";
	/** Approve = AP */
	public static final String DOCACTION_Approve = "AP";
	/** Reject = RJ */
	public static final String DOCACTION_Reject = "RJ";
	/** Post = PO */
	public static final String DOCACTION_Post = "PO";
	/** Void = VO */
	public static final String DOCACTION_Void = "VO";
	/** Close = CL */
	public static final String DOCACTION_Close = "CL";
	/** Reverse - Correct = RC */
	public static final String DOCACTION_Reverse_Correct = "RC";
	/** Reverse - Accrual = RA */
	public static final String DOCACTION_Reverse_Accrual = "RA";
	/** Invalidate = IN */
	public static final String DOCACTION_Invalidate = "IN";
	/** Re-activate = RE */
	public static final String DOCACTION_Re_Activate = "RE";
	/** <None> = -- */
	public static final String DOCACTION_None = "--";
	/** Prepare = PR */
	public static final String DOCACTION_Prepare = "PR";
	/** Unlock = XL */
	public static final String DOCACTION_Unlock = "XL";
	/** Wait Complete = WC */
	public static final String DOCACTION_WaitComplete = "WC";
	/** Set Document Action.
		@param DocAction 
		The targeted status of the document
	  */
	public void setDocAction (String DocAction)
	{

		set_Value (COLUMNNAME_DocAction, DocAction);
	}

	/** Get Document Action.
		@return The targeted status of the document
	  */
	public String getDocAction () 
	{
		return (String)get_Value(COLUMNNAME_DocAction);
	}

	/** DocStatus AD_Reference_ID=131 */
	public static final int DOCSTATUS_AD_Reference_ID=131;
	/** Drafted = DR */
	public static final String DOCSTATUS_Drafted = "DR";
	/** Completed = CO */
	public static final String DOCSTATUS_Completed = "CO";
	/** Approved = AP */
	public static final String DOCSTATUS_Approved = "AP";
	/** Not Approved = NA */
	public static final String DOCSTATUS_NotApproved = "NA";
	/** Voided = VO */
	public static final String DOCSTATUS_Voided = "VO";
	/** Invalid = IN */
	public static final String DOCSTATUS_Invalid = "IN";
	/** Reversed = RE */
	public static final String DOCSTATUS_Reversed = "RE";
	/** Closed = CL */
	public static final String DOCSTATUS_Closed = "CL";
	/** Unknown = ?? */
	public static final String DOCSTATUS_Unknown = "??";
	/** In Progress = IP */
	public static final String DOCSTATUS_InProgress = "IP";
	/** Waiting Payment = WP */
	public static final String DOCSTATUS_WaitingPayment = "WP";
	/** Waiting Confirmation = WC */
	public static final String DOCSTATUS_WaitingConfirmation = "WC";
	/** Set Document Status.
		@param DocStatus 
		The current status of the document
	  */
	public void setDocStatus (String DocStatus)
	{

		set_Value (COLUMNNAME_DocStatus, DocStatus);
	}

	/** Get Document Status.
		@return The current status of the document
	  */
	public String getDocStatus () 
	{
		return (String)get_Value(COLUMNNAME_DocStatus);
	}

	/** Set Generate Withholding.
		@param GenerateWithholding Generate Withholding	  */
	public void setGenerateWithholding (String GenerateWithholding)
	{
		set_Value (COLUMNNAME_GenerateWithholding, GenerateWithholding);
	}

	/** Get Generate Withholding.
		@return Generate Withholding	  */
	public String getGenerateWithholding () 
	{
		return (String)get_Value(COLUMNNAME_GenerateWithholding);
	}

	/** Set Sales Transaction.
		@param IsSOTrx 
		This is a Sales Transaction
	  */
	public void setIsSOTrx (boolean IsSOTrx)
	{
		set_ValueNoCheck (COLUMNNAME_IsSOTrx, Boolean.valueOf(IsSOTrx));
	}

	/** Get Sales Transaction.
		@return This is a Sales Transaction
	  */
	public boolean isSOTrx () 
	{
		Object oo = get_Value(COLUMNNAME_IsSOTrx);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Withholding Use Currency Convert.
		@param IsWHUseCurrencyConvert Withholding Use Currency Convert	  */
	public void setIsWHUseCurrencyConvert (boolean IsWHUseCurrencyConvert)
	{
		set_Value (COLUMNNAME_IsWHUseCurrencyConvert, Boolean.valueOf(IsWHUseCurrencyConvert));
	}

	/** Get Withholding Use Currency Convert.
		@return Withholding Use Currency Convert	  */
	public boolean isWHUseCurrencyConvert () 
	{
		Object oo = get_Value(COLUMNNAME_IsWHUseCurrencyConvert);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Withholding Type.
		@param LCO_WithholdingType_ID Withholding Type	  */
	public void setLCO_WithholdingType_ID (int LCO_WithholdingType_ID)
	{
		if (LCO_WithholdingType_ID < 1) 
			set_Value (COLUMNNAME_LCO_WithholdingType_ID, null);
		else 
			set_Value (COLUMNNAME_LCO_WithholdingType_ID, Integer.valueOf(LCO_WithholdingType_ID));
	}

	/** Get Withholding Type.
		@return Withholding Type	  */
	public int getLCO_WithholdingType_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LCO_WithholdingType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Voucher Withholding.
		@param LVE_VoucherWithholding_ID Voucher Withholding	  */
	public void setLVE_VoucherWithholding_ID (int LVE_VoucherWithholding_ID)
	{
		if (LVE_VoucherWithholding_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_LVE_VoucherWithholding_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_LVE_VoucherWithholding_ID, Integer.valueOf(LVE_VoucherWithholding_ID));
	}

	/** Get Voucher Withholding.
		@return Voucher Withholding	  */
	public int getLVE_VoucherWithholding_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LVE_VoucherWithholding_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set LVE_VoucherWithholding_UU.
		@param LVE_VoucherWithholding_UU LVE_VoucherWithholding_UU	  */
	public void setLVE_VoucherWithholding_UU (String LVE_VoucherWithholding_UU)
	{
		set_ValueNoCheck (COLUMNNAME_LVE_VoucherWithholding_UU, LVE_VoucherWithholding_UU);
	}

	/** Get LVE_VoucherWithholding_UU.
		@return LVE_VoucherWithholding_UU	  */
	public String getLVE_VoucherWithholding_UU () 
	{
		return (String)get_Value(COLUMNNAME_LVE_VoucherWithholding_UU);
	}

	public org.compiere.model.I_C_BankAccount getLVE_WHBankAccount() throws RuntimeException
    {
		return (org.compiere.model.I_C_BankAccount)MTable.get(getCtx(), org.compiere.model.I_C_BankAccount.Table_Name)
			.getPO(getLVE_WHBankAccount_ID(), get_TrxName());	}

	/** Set Withholding Bank Account.
		@param LVE_WHBankAccount_ID Withholding Bank Account	  */
	public void setLVE_WHBankAccount_ID (int LVE_WHBankAccount_ID)
	{
		if (LVE_WHBankAccount_ID < 1) 
			set_Value (COLUMNNAME_LVE_WHBankAccount_ID, null);
		else 
			set_Value (COLUMNNAME_LVE_WHBankAccount_ID, Integer.valueOf(LVE_WHBankAccount_ID));
	}

	/** Get Withholding Bank Account.
		@return Withholding Bank Account	  */
	public int getLVE_WHBankAccount_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LVE_WHBankAccount_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getLVE_WHPaymentDocType() throws RuntimeException
    {
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_Name)
			.getPO(getLVE_WHPaymentDocType_ID(), get_TrxName());	}

	/** Set Payment Withholding Document Type.
		@param LVE_WHPaymentDocType_ID Payment Withholding Document Type	  */
	public void setLVE_WHPaymentDocType_ID (int LVE_WHPaymentDocType_ID)
	{
		if (LVE_WHPaymentDocType_ID < 1) 
			set_Value (COLUMNNAME_LVE_WHPaymentDocType_ID, null);
		else 
			set_Value (COLUMNNAME_LVE_WHPaymentDocType_ID, Integer.valueOf(LVE_WHPaymentDocType_ID));
	}

	/** Get Payment Withholding Document Type.
		@return Payment Withholding Document Type	  */
	public int getLVE_WHPaymentDocType_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LVE_WHPaymentDocType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Processed.
		@param Processed 
		The document has been processed
	  */
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Processed.
		@return The document has been processed
	  */
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Document Action.
		@param VWH_DocAction Document Action	  */
	public void setVWH_DocAction (String VWH_DocAction)
	{
		set_Value (COLUMNNAME_VWH_DocAction, VWH_DocAction);
	}

	/** Get Document Action.
		@return Document Action	  */
	public String getVWH_DocAction () 
	{
		return (String)get_Value(COLUMNNAME_VWH_DocAction);
	}

	/** Set Withholding No.
		@param WithholdingNo Withholding No	  */
	public void setWithholdingNo (String WithholdingNo)
	{
		set_Value (COLUMNNAME_WithholdingNo, WithholdingNo);
	}

	/** Get Withholding No.
		@return Withholding No	  */
	public String getWithholdingNo () 
	{
		return (String)get_Value(COLUMNNAME_WithholdingNo);
	}
}