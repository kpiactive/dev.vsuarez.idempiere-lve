package ve.net.dcs.callout;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.base.IColumnCallout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MSequence;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.globalqss.model.MLCOWithholdingType;

import ve.net.dcs.model.I_LVE_VoucherWithholding;

public class CalloutVoucherWithholding implements IColumnCallout{

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab,
			GridField mField, Object value, Object oldValue) {
			if (mField.getColumnName().equals(I_LVE_VoucherWithholding.COLUMNNAME_LCO_WithholdingType_ID)){
				return docType(ctx, WindowNo, mTab, mField, value);
			}
			return null;
	}

	public String docType (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if(value != null){
			MLCOWithholdingType wtype=new MLCOWithholdingType(ctx,(Integer)value,null);		
			Integer C_DocType_ID = wtype.get_ValueAsInt("C_DocType_ID");		
			if (C_DocType_ID == null || C_DocType_ID.intValue() == 0)
				return "";
			String sql = "SELECT d.HasCharges,d.IsDocNoControlled," // 1..2
					+ "d.DocBaseType, " // 3
					+ "s.AD_Sequence_ID " //4
					+ "FROM C_DocType d "
					+ "LEFT OUTER JOIN AD_Sequence s ON (d.DocNoSequence_ID=s.AD_Sequence_ID) "
					+ "WHERE C_DocType_ID=?";		//	1
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, null);
				pstmt.setInt(1, C_DocType_ID.intValue());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					//	Charges - Set Context
					Env.setContext(ctx, WindowNo, "HasCharges", rs.getString("HasCharges"));
					//	DocumentNo
					if (rs.getString("IsDocNoControlled").equals("Y"))
					{
						int AD_Sequence_ID = rs.getInt("AD_Sequence_ID");
						mTab.setValue("DocumentNo", MSequence.getPreliminaryNo(mTab, AD_Sequence_ID));
					}
					//  DocBaseType - Set Context
					//	String s = rs.getString("DocBaseType");
					//	Env.setContext(ctx, WindowNo, "DocBaseType", s);
				}
			}
			catch (SQLException e){
				return e.getLocalizedMessage();
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
		}
		return "";
	}	//	docType
	

}
