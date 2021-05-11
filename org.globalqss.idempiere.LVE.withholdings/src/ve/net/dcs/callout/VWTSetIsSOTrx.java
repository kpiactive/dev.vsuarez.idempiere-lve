/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

package ve.net.dcs.callout;

import java.util.Properties;

import org.adempiere.base.IColumnCallout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.globalqss.model.X_LCO_WithholdingType;

import ve.net.dcs.model.I_LVE_VoucherWithholding;

/**
 * Seniat Validator
 * 
 * @author Double Click Sistemas C.A. - http://dcs.net.ve
 * @author Saul Pina - spina@dcs.net.ve
 */
public class VWTSetIsSOTrx implements IColumnCallout {

	public String SetIsSOTrx(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value) {
		X_LCO_WithholdingType wTypeID = null;
		if(value != null){
			wTypeID = new X_LCO_WithholdingType(ctx, (Integer) value, null);
			mTab.setValue(I_LVE_VoucherWithholding.COLUMNNAME_IsSOTrx, wTypeID.isSOTrx());
		}
		return null;
	}

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
		if (mField.getColumnName().equals(I_LVE_VoucherWithholding.COLUMNNAME_LCO_WithholdingType_ID)){
			return SetIsSOTrx(ctx, WindowNo, mTab, mField, value);
		}
		return null;
		
	}

}
