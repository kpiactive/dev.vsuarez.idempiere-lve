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

/** Generated Model for LVE_generateTXT
 *  @author iDempiere (generated) 
 *  @version Release 2.0 - $Id$ */
public class X_LVE_generateTXT extends PO implements I_LVE_generateTXT, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20140221L;

    /** Standard Constructor */
    public X_LVE_generateTXT (Properties ctx, int LVE_generateTXT_ID, String trxName)
    {
      super (ctx, LVE_generateTXT_ID, trxName);
      /** if (LVE_generateTXT_ID == 0)
        {
			setLVE_generateTXT_ID (0);
			setLVE_TypeOperation (null);
// C
			setValidFrom (new Timestamp( System.currentTimeMillis() ));
			setValidTo (new Timestamp( System.currentTimeMillis() ));
        } */
    }

    /** Load Constructor */
    public X_LVE_generateTXT (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 6 - System - Client 
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
      StringBuffer sb = new StringBuffer ("X_LVE_generateTXT[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Generate TXT Seniat.
		@param GenerateTxtSeniat Generate TXT Seniat	  */
	public void setGenerateTxtSeniat (String GenerateTxtSeniat)
	{
		set_Value (COLUMNNAME_GenerateTxtSeniat, GenerateTxtSeniat);
	}

	/** Get Generate TXT Seniat.
		@return Generate TXT Seniat	  */
	public String getGenerateTxtSeniat () 
	{
		return (String)get_Value(COLUMNNAME_GenerateTxtSeniat);
	}

	/** Set Generate TXT Seniat.
		@param LVE_generateTXT_ID Generate TXT Seniat	  */
	public void setLVE_generateTXT_ID (int LVE_generateTXT_ID)
	{
		if (LVE_generateTXT_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_LVE_generateTXT_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_LVE_generateTXT_ID, Integer.valueOf(LVE_generateTXT_ID));
	}

	/** Get Generate TXT Seniat.
		@return Generate TXT Seniat	  */
	public int getLVE_generateTXT_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LVE_generateTXT_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set LVE_generateTXT_UU.
		@param LVE_generateTXT_UU LVE_generateTXT_UU	  */
	public void setLVE_generateTXT_UU (String LVE_generateTXT_UU)
	{
		set_Value (COLUMNNAME_LVE_generateTXT_UU, LVE_generateTXT_UU);
	}

	/** Get LVE_generateTXT_UU.
		@return LVE_generateTXT_UU	  */
	public String getLVE_generateTXT_UU () 
	{
		return (String)get_Value(COLUMNNAME_LVE_generateTXT_UU);
	}

	/** Sales = V */
	public static final String LVE_TYPEOPERATION_Sales = "V";
	/** Purchase = C */
	public static final String LVE_TYPEOPERATION_Purchase = "C";
	/** Set Type Operation.
		@param LVE_TypeOperation Type Operation	  */
	public void setLVE_TypeOperation (String LVE_TypeOperation)
	{

		set_Value (COLUMNNAME_LVE_TypeOperation, LVE_TypeOperation);
	}

	/** Get Type Operation.
		@return Type Operation	  */
	public String getLVE_TypeOperation () 
	{
		return (String)get_Value(COLUMNNAME_LVE_TypeOperation);
	}

	/** Set Valid from.
		@param ValidFrom 
		Valid from including this date (first day)
	  */
	public void setValidFrom (Timestamp ValidFrom)
	{
		set_ValueNoCheck (COLUMNNAME_ValidFrom, ValidFrom);
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
		set_ValueNoCheck (COLUMNNAME_ValidTo, ValidTo);
	}

	/** Get Valid to.
		@return Valid to including this date (last day)
	  */
	public Timestamp getValidTo () 
	{
		return (Timestamp)get_Value(COLUMNNAME_ValidTo);
	}
}