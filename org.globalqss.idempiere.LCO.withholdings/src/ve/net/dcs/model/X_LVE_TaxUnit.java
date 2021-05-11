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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for LVE_TaxUnit
 *  @author iDempiere (generated) 
 *  @version Release 1.0c - $Id$ */
public class X_LVE_TaxUnit extends PO implements I_LVE_TaxUnit, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20130913L;

    /** Standard Constructor */
    public X_LVE_TaxUnit (Properties ctx, int LVE_TaxUnit_ID, String trxName)
    {
      super (ctx, LVE_TaxUnit_ID, trxName);
      /** if (LVE_TaxUnit_ID == 0)
        {
			setLVE_TaxUnit_ID (0);
			setValidFrom (new Timestamp( System.currentTimeMillis() ));
			setValueNumber (Env.ZERO);
        } */
    }

    /** Load Constructor */
    public X_LVE_TaxUnit (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_LVE_TaxUnit[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	/** Set Tax Unit.
		@param LVE_TaxUnit_ID Tax Unit	  */
	public void setLVE_TaxUnit_ID (int LVE_TaxUnit_ID)
	{
		if (LVE_TaxUnit_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_LVE_TaxUnit_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_LVE_TaxUnit_ID, Integer.valueOf(LVE_TaxUnit_ID));
	}

	/** Get Tax Unit.
		@return Tax Unit	  */
	public int getLVE_TaxUnit_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LVE_TaxUnit_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set LVE_TaxUnit_UU.
		@param LVE_TaxUnit_UU LVE_TaxUnit_UU	  */
	public void setLVE_TaxUnit_UU (String LVE_TaxUnit_UU)
	{
		set_Value (COLUMNNAME_LVE_TaxUnit_UU, LVE_TaxUnit_UU);
	}

	/** Get LVE_TaxUnit_UU.
		@return LVE_TaxUnit_UU	  */
	public String getLVE_TaxUnit_UU () 
	{
		return (String)get_Value(COLUMNNAME_LVE_TaxUnit_UU);
	}

	/** Set Valid from.
		@param ValidFrom 
		Valid from including this date (first day)
	  */
	public void setValidFrom (Timestamp ValidFrom)
	{
		set_Value (COLUMNNAME_ValidFrom, ValidFrom);
	}

	/** Get Valid from.
		@return Valid from including this date (first day)
	  */
	public Timestamp getValidFrom () 
	{
		return (Timestamp)get_Value(COLUMNNAME_ValidFrom);
	}

	/** Set Valid to.
		@param ValidTo 
		Valid to including this date (last day)
	  */
	public void setValidTo (Timestamp ValidTo)
	{
		set_Value (COLUMNNAME_ValidTo, ValidTo);
	}

	/** Get Valid to.
		@return Valid to including this date (last day)
	  */
	public Timestamp getValidTo () 
	{
		return (Timestamp)get_Value(COLUMNNAME_ValidTo);
	}

	/** Set Value.
		@param ValueNumber 
		Numeric Value
	  */
	public void setValueNumber (BigDecimal ValueNumber)
	{
		set_Value (COLUMNNAME_ValueNumber, ValueNumber);
	}

	/** Get Value.
		@return Numeric Value
	  */
	public BigDecimal getValueNumber () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ValueNumber);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}