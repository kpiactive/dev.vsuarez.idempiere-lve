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
package ve.net.dcs.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for LVE_VoucherWithholding
 *  @author iDempiere (generated) 
 *  @version Release 8.2
 */
@SuppressWarnings("all")
public interface I_LVE_VoucherWithholding 
{

    /** TableName=LVE_VoucherWithholding */
    public static final String Table_Name = "LVE_VoucherWithholding";

    /** AD_Table_ID=1000016 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name C_BPartner_ID */
    public static final String COLUMNNAME_C_BPartner_ID = "C_BPartner_ID";

	/** Set Business Partner .
	  * Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID);

	/** Get Business Partner .
	  * Identifies a Business Partner
	  */
	public int getC_BPartner_ID();

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException;

    /** Column name C_DocType_ID */
    public static final String COLUMNNAME_C_DocType_ID = "C_DocType_ID";

	/** Set Document Type.
	  * Document type or rules
	  */
	public void setC_DocType_ID (int C_DocType_ID);

	/** Get Document Type.
	  * Document type or rules
	  */
	public int getC_DocType_ID();

	public org.compiere.model.I_C_DocType getC_DocType() throws RuntimeException;

    /** Column name C_Invoice_ID */
    public static final String COLUMNNAME_C_Invoice_ID = "C_Invoice_ID";

	/** Set Invoice.
	  * Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID);

	/** Get Invoice.
	  * Invoice Identifier
	  */
	public int getC_Invoice_ID();

	public org.compiere.model.I_C_Invoice getC_Invoice() throws RuntimeException;

    /** Column name C_Payment_ID */
    public static final String COLUMNNAME_C_Payment_ID = "C_Payment_ID";

	/** Set Payment.
	  * Payment identifier
	  */
	public void setC_Payment_ID (int C_Payment_ID);

	/** Get Payment.
	  * Payment identifier
	  */
	public int getC_Payment_ID();

	public org.compiere.model.I_C_Payment getC_Payment() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name CreateFrom */
    public static final String COLUMNNAME_CreateFrom = "CreateFrom";

	/** Set Create lines from.
	  * Process which will generate a new document lines based on an existing document
	  */
	public void setCreateFrom (String CreateFrom);

	/** Get Create lines from.
	  * Process which will generate a new document lines based on an existing document
	  */
	public String getCreateFrom();

    /** Column name DateAcct */
    public static final String COLUMNNAME_DateAcct = "DateAcct";

	/** Set Account Date.
	  * Accounting Date
	  */
	public void setDateAcct (Timestamp DateAcct);

	/** Get Account Date.
	  * Accounting Date
	  */
	public Timestamp getDateAcct();

    /** Column name DateFrom */
    public static final String COLUMNNAME_DateFrom = "DateFrom";

	/** Set Date From.
	  * Starting date for a range
	  */
	public void setDateFrom (Timestamp DateFrom);

	/** Get Date From.
	  * Starting date for a range
	  */
	public Timestamp getDateFrom();

    /** Column name DateTo */
    public static final String COLUMNNAME_DateTo = "DateTo";

	/** Set Date To.
	  * End date of a date range
	  */
	public void setDateTo (Timestamp DateTo);

	/** Get Date To.
	  * End date of a date range
	  */
	public Timestamp getDateTo();

    /** Column name DateTrx */
    public static final String COLUMNNAME_DateTrx = "DateTrx";

	/** Set Transaction Date.
	  * Transaction Date
	  */
	public void setDateTrx (Timestamp DateTrx);

	/** Get Transaction Date.
	  * Transaction Date
	  */
	public Timestamp getDateTrx();

    /** Column name DocAction */
    public static final String COLUMNNAME_DocAction = "DocAction";

	/** Set Document Action.
	  * The targeted status of the document
	  */
	public void setDocAction (String DocAction);

	/** Get Document Action.
	  * The targeted status of the document
	  */
	public String getDocAction();

    /** Column name DocStatus */
    public static final String COLUMNNAME_DocStatus = "DocStatus";

	/** Set Document Status.
	  * The current status of the document
	  */
	public void setDocStatus (String DocStatus);

	/** Get Document Status.
	  * The current status of the document
	  */
	public String getDocStatus();

    /** Column name GenerateWithholding */
    public static final String COLUMNNAME_GenerateWithholding = "GenerateWithholding";

	/** Set Generate Withholding	  */
	public void setGenerateWithholding (String GenerateWithholding);

	/** Get Generate Withholding	  */
	public String getGenerateWithholding();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name IsSOTrx */
    public static final String COLUMNNAME_IsSOTrx = "IsSOTrx";

	/** Set Sales Transaction.
	  * This is a Sales Transaction
	  */
	public void setIsSOTrx (boolean IsSOTrx);

	/** Get Sales Transaction.
	  * This is a Sales Transaction
	  */
	public boolean isSOTrx();

    /** Column name IsWHUseCurrencyConvert */
    public static final String COLUMNNAME_IsWHUseCurrencyConvert = "IsWHUseCurrencyConvert";

	/** Set Withholding Use Currency Convert	  */
	public void setIsWHUseCurrencyConvert (boolean IsWHUseCurrencyConvert);

	/** Get Withholding Use Currency Convert	  */
	public boolean isWHUseCurrencyConvert();

    /** Column name LCO_WithholdingType_ID */
    public static final String COLUMNNAME_LCO_WithholdingType_ID = "LCO_WithholdingType_ID";

	/** Set Withholding Type	  */
	public void setLCO_WithholdingType_ID (int LCO_WithholdingType_ID);

	/** Get Withholding Type	  */
	public int getLCO_WithholdingType_ID();

    /** Column name LVE_VoucherWithholding_ID */
    public static final String COLUMNNAME_LVE_VoucherWithholding_ID = "LVE_VoucherWithholding_ID";

	/** Set Voucher Withholding	  */
	public void setLVE_VoucherWithholding_ID (int LVE_VoucherWithholding_ID);

	/** Get Voucher Withholding	  */
	public int getLVE_VoucherWithholding_ID();

    /** Column name LVE_VoucherWithholding_UU */
    public static final String COLUMNNAME_LVE_VoucherWithholding_UU = "LVE_VoucherWithholding_UU";

	/** Set LVE_VoucherWithholding_UU	  */
	public void setLVE_VoucherWithholding_UU (String LVE_VoucherWithholding_UU);

	/** Get LVE_VoucherWithholding_UU	  */
	public String getLVE_VoucherWithholding_UU();

    /** Column name LVE_WHBankAccount_ID */
    public static final String COLUMNNAME_LVE_WHBankAccount_ID = "LVE_WHBankAccount_ID";

	/** Set Withholding Bank Account	  */
	public void setLVE_WHBankAccount_ID (int LVE_WHBankAccount_ID);

	/** Get Withholding Bank Account	  */
	public int getLVE_WHBankAccount_ID();

	public org.compiere.model.I_C_BankAccount getLVE_WHBankAccount() throws RuntimeException;

    /** Column name LVE_WHPaymentDocType_ID */
    public static final String COLUMNNAME_LVE_WHPaymentDocType_ID = "LVE_WHPaymentDocType_ID";

	/** Set Payment Withholding Document Type	  */
	public void setLVE_WHPaymentDocType_ID (int LVE_WHPaymentDocType_ID);

	/** Get Payment Withholding Document Type	  */
	public int getLVE_WHPaymentDocType_ID();

	public org.compiere.model.I_C_DocType getLVE_WHPaymentDocType() throws RuntimeException;

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name VWH_DocAction */
    public static final String COLUMNNAME_VWH_DocAction = "VWH_DocAction";

	/** Set Document Action	  */
	public void setVWH_DocAction (String VWH_DocAction);

	/** Get Document Action	  */
	public String getVWH_DocAction();

    /** Column name WithholdingNo */
    public static final String COLUMNNAME_WithholdingNo = "WithholdingNo";

	/** Set Withholding No	  */
	public void setWithholdingNo (String WithholdingNo);

	/** Get Withholding No	  */
	public String getWithholdingNo();
}
