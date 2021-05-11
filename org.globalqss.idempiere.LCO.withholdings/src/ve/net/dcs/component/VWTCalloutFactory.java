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

package ve.net.dcs.component;

import org.adempiere.base.IColumnCallout;
import org.adempiere.base.IColumnCalloutFactory;
import org.compiere.model.I_C_BPartner;
import org.compiere.util.CLogger;

import ve.net.dcs.callout.CalloutVoucherWithholding;
import ve.net.dcs.callout.VWTSeniatValidator;
import ve.net.dcs.callout.VWTSetDocumentNo;
import ve.net.dcs.callout.VWTSetIsSOTrx;
import ve.net.dcs.model.I_LVE_VoucherWithholding;

/**
 * Seniat Validator
 * 
 * @author Double Click Sistemas C.A. - http://dcs.net.ve
 * @author Saul Pina - spina@dcs.net.ve
 */
public class VWTCalloutFactory implements IColumnCalloutFactory {

	private static CLogger log = CLogger.getCLogger(VWTCalloutFactory.class);

	public VWTCalloutFactory() {

	}

	@Override
	public IColumnCallout[] getColumnCallouts(String tableName, String columnName) {
		log.info("VWTCalloutFactory");
		if (tableName.equalsIgnoreCase(I_C_BPartner.Table_Name)) {
			if (columnName.equalsIgnoreCase("LVE_SeniatValidator"))
				return new IColumnCallout[] { new VWTSeniatValidator() };
		} else if (tableName.equalsIgnoreCase(I_LVE_VoucherWithholding.Table_Name)) {
			if (columnName.equalsIgnoreCase(I_LVE_VoucherWithholding.COLUMNNAME_LCO_WithholdingType_ID))
				return new IColumnCallout[] { new VWTSetIsSOTrx(), new VWTSetDocumentNo(), new CalloutVoucherWithholding()};
			if (columnName.equalsIgnoreCase(I_LVE_VoucherWithholding.COLUMNNAME_WithholdingNo))
				return new IColumnCallout[] { new VWTSetDocumentNo() };

		}
		return null;
	}

}
