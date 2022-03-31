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
package org.globalqss.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Model for LCO_WithholdingType
 *  @author iDempiere (generated) 
 *  @version Release 8.2 - $Id$ */
public class X_LCO_WithholdingType extends PO implements I_LCO_WithholdingType, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20220331L;

    /** Standard Constructor */
    public X_LCO_WithholdingType (Properties ctx, int LCO_WithholdingType_ID, String trxName)
    {
      super (ctx, LCO_WithholdingType_ID, trxName);
      /** if (LCO_WithholdingType_ID == 0)
        {
			setC_DocType_ID (0);
			setIsSOTrx (false);
// N
			setLCO_WithholdingType_ID (0);
			setName (null);
			setType (null);
// Other
        } */
    }

    /** Load Constructor */
    public X_LCO_WithholdingType (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 2 - Client 
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
      StringBuilder sb = new StringBuilder ("X_LCO_WithholdingType[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_Charge getC_Charge() throws RuntimeException
    {
		return (org.compiere.model.I_C_Charge)MTable.get(getCtx(), org.compiere.model.I_C_Charge.Table_Name)
			.getPO(getC_Charge_ID(), get_TrxName());	}

	/** Set Charge.
		@param C_Charge_ID 
		Additional document charges
	  */
	public void setC_Charge_ID (int C_Charge_ID)
	{
		if (C_Charge_ID < 1) 
			set_Value (COLUMNNAME_C_Charge_ID, null);
		else 
			set_Value (COLUMNNAME_C_Charge_ID, Integer.valueOf(C_Charge_ID));
	}

	/** Get Charge.
		@return Additional document charges
	  */
	public int getC_Charge_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Charge_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getC_DocTypeDN() throws RuntimeException
    {
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_Name)
			.getPO(getC_DocTypeDN_ID(), get_TrxName());	}

	/** Set Document Type Debit Note.
		@param C_DocTypeDN_ID Document Type Debit Note	  */
	public void setC_DocTypeDN_ID (int C_DocTypeDN_ID)
	{
		if (C_DocTypeDN_ID < 1) 
			set_Value (COLUMNNAME_C_DocTypeDN_ID, null);
		else 
			set_Value (COLUMNNAME_C_DocTypeDN_ID, Integer.valueOf(C_DocTypeDN_ID));
	}

	/** Get Document Type Debit Note.
		@return Document Type Debit Note	  */
	public int getC_DocTypeDN_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocTypeDN_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getC_DocType() throws RuntimeException
    {
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_Name)
			.getPO(getC_DocType_ID(), get_TrxName());	}

	/** Set Document Type.
		@param C_DocType_ID 
		Document type or rules
	  */
	public void setC_DocType_ID (int C_DocType_ID)
	{
		if (C_DocType_ID < 0) 
			set_Value (COLUMNNAME_C_DocType_ID, null);
		else 
			set_Value (COLUMNNAME_C_DocType_ID, Integer.valueOf(C_DocType_ID));
	}

	/** Get Document Type.
		@return Document type or rules
	  */
	public int getC_DocType_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Counter.
		@param Counter 
		Count Value
	  */
	public void setCounter (int Counter)
	{
		throw new IllegalArgumentException ("Counter is virtual column");	}

	/** Get Counter.
		@return Count Value
	  */
	public int getCounter () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Counter);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Sales Transaction.
		@param IsSOTrx 
		This is a Sales Transaction
	  */
	public void setIsSOTrx (boolean IsSOTrx)
	{
		set_Value (COLUMNNAME_IsSOTrx, Boolean.valueOf(IsSOTrx));
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

	/** Set Use Currency Convert.
		@param IsUseCurrencyConvert Use Currency Convert	  */
	public void setIsUseCurrencyConvert (boolean IsUseCurrencyConvert)
	{
		set_Value (COLUMNNAME_IsUseCurrencyConvert, Boolean.valueOf(IsUseCurrencyConvert));
	}

	/** Get Use Currency Convert.
		@return Use Currency Convert	  */
	public boolean isUseCurrencyConvert () 
	{
		Object oo = get_Value(COLUMNNAME_IsUseCurrencyConvert);
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
			set_ValueNoCheck (COLUMNNAME_LCO_WithholdingType_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_LCO_WithholdingType_ID, Integer.valueOf(LCO_WithholdingType_ID));
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

	/** Set LCO_WithholdingType_UU.
		@param LCO_WithholdingType_UU LCO_WithholdingType_UU	  */
	public void setLCO_WithholdingType_UU (String LCO_WithholdingType_UU)
	{
		set_Value (COLUMNNAME_LCO_WithholdingType_UU, LCO_WithholdingType_UU);
	}

	/** Get LCO_WithholdingType_UU.
		@return LCO_WithholdingType_UU	  */
	public String getLCO_WithholdingType_UU () 
	{
		return (String)get_Value(COLUMNNAME_LCO_WithholdingType_UU);
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

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }

	/** ISLR = ISLR */
	public static final String TYPE_ISLR = "ISLR";
	/** IVA = IVA */
	public static final String TYPE_IVA = "IVA";
	/** Other = Other */
	public static final String TYPE_Other = "Other";
	/** Economic Activity (Municipal) = IAE */
	public static final String TYPE_EconomicActivityMunicipal = "IAE";
	/** IGTF = IGTF */
	public static final String TYPE_IGTF = "IGTF";
	/** Set Type.
		@param Type 
		Type of Validation (SQL, Java Script, Java Language)
	  */
	public void setType (String Type)
	{

		set_Value (COLUMNNAME_Type, Type);
	}

	/** Get Type.
		@return Type of Validation (SQL, Java Script, Java Language)
	  */
	public String getType () 
	{
		return (String)get_Value(COLUMNNAME_Type);
	}
}