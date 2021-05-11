package ve.net.dcs.callout;

import java.util.Properties;
import org.adempiere.base.IColumnCallout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MSysConfig;
import org.compiere.util.CLogger;
import org.globalqss.model.MLCOWithholdingType;


import ve.net.dcs.model.I_LVE_VoucherWithholding;

public class VWTSetDocumentNo implements IColumnCallout {

	private static CLogger log = CLogger.getCLogger(VWTSetDocumentNo.class);

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab,
			GridField mField, Object value, Object oldValue) {
		if (mField.getColumnName().equals(I_LVE_VoucherWithholding.COLUMNNAME_WithholdingNo)){
			return getDocNo(ctx, WindowNo, mTab, mField, value, oldValue);
		}
		if (mField.getColumnName().equals(I_LVE_VoucherWithholding.COLUMNNAME_LCO_WithholdingType_ID)){
			return SetDocTypeID(ctx, WindowNo, mTab, mField, value, oldValue);
		}
		return null;
	}
	
	
	private String getDocNo(Properties ctx, int windowNo, GridTab mTab,
			GridField mField, Object value, Object oldValue) {
		String DocumentNo= (String)value;
		int LVE_WithholdingNoLength=MSysConfig.getIntValue("LVE_WithholdingNoLength",0,(Integer)mTab.getValue("AD_Client_ID"));
		if (LVE_WithholdingNoLength!=0){
			if(DocumentNo != null){
				if (DocumentNo.length()>LVE_WithholdingNoLength){
					mTab.setValue(I_LVE_VoucherWithholding.COLUMNNAME_WithholdingNo, "");
					log.warning("El numero de retencion supera la cantidad de caracteres permitidos"+DocumentNo);
					return "Error:El numero de retencion supera la cantidad de caracteres permitidos, por favor corregir";
			}
			DocumentNo=String.format("%0" + String.valueOf(LVE_WithholdingNoLength) +"d", 
					Long.valueOf( DocumentNo.substring(DocumentNo.length()-LVE_WithholdingNoLength<=0 ? 0 : DocumentNo.length()-LVE_WithholdingNoLength,
						DocumentNo.length())));
			if(DocumentNo.length()>0){
				mTab.setValue(I_LVE_VoucherWithholding.COLUMNNAME_WithholdingNo, DocumentNo);
				//mTab.setValue(I_LVE_VoucherWithholding.COLUMNNAME_DocumentNo, DocumentNo);
			}
			}
		}
		return null;
	}
	
	private String SetDocTypeID(Properties ctx, int windowNo, GridTab mTab,
			GridField mField, Object value, Object oldValue) {
		if(value != null){
			Integer WHtype= (Integer)value;
			if(WHtype.intValue()>0){
				MLCOWithholdingType WithholdingType = new MLCOWithholdingType(ctx, WHtype, null);  
				mTab.setValue(I_LVE_VoucherWithholding.COLUMNNAME_C_DocType_ID, WithholdingType.get_ValueAsInt("C_Doctype_ID"));
				}
		}
		//	return 
		return null;
	}

}
