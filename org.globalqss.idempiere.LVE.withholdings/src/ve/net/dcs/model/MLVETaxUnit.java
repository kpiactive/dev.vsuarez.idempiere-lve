package ve.net.dcs.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.util.DB;

public class MLVETaxUnit extends X_LVE_TaxUnit {

	private static final long serialVersionUID = -8016947226671538059L;

	public MLVETaxUnit(Properties ctx, int LVE_TaxUnit_ID, String trxName) {
		super(ctx, LVE_TaxUnit_ID, trxName);
	}

	public MLVETaxUnit(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public static Integer taxUnit(String trx_name, int org, Timestamp _From, Timestamp _To) {
		Timestamp From = (Timestamp) _From.clone();
		Timestamp To = (Timestamp) _To.clone();
		Integer value = 0;

		String sQuery = "" + "SELECT tu.valuenumber " + "FROM   lve_taxunit tu " + "WHERE  ad_org_id = ? AND" + "(? BETWEEN validfrom AND validto OR ((validfrom<=?) AND (? <= validto OR validto IS NULL)))";
		value = DB.getSQLValue(trx_name, sQuery, new Object[] { org, From, From, To });
		return value;
	}

	public static Timestamp firstDayOfMonth(Timestamp date) {
		Timestamp firstDay = (Timestamp) date.clone();
		firstDay.setDate(1);
		return firstDay;
	}

}
