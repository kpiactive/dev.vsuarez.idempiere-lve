package org.globalqss.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.globalqss.model.X_LCO_WithholdingType;

public class MLCOWithholdingType extends X_LCO_WithholdingType {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8360323787031736942L;

	public MLCOWithholdingType(Properties ctx, int LCO_WithholdingType_ID,
			String trxName) {
		super(ctx, LCO_WithholdingType_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MLCOWithholdingType(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	

}
