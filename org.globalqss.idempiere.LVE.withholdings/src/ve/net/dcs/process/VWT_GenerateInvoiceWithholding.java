package ve.net.dcs.process;

import java.util.logging.Level;

import org.compiere.model.MInvoice;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import ve.net.dcs.model.MLVEVoucherWithholding;
import ve.net.dcs.model.VWT_MInvoice;

public class VWT_GenerateInvoiceWithholding extends SvrProcess {
	
	/** The Record						*/
	private int		p_Record_ID = 0;

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;			
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_Record_ID =  getRecord_ID(); 
		
	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		
		int cnt = 0;
		
		if (p_Record_ID != 0){
			MLVEVoucherWithholding voucher = new MLVEVoucherWithholding(getCtx(), p_Record_ID, get_TrxName());
			MInvoice[] invoices = VWT_MInvoice.getOfBPartnerDateFromDateTo(getCtx(), voucher, get_TrxName());
			
			for (MInvoice mInvoice : invoices) {	
				log.info("Fecha Factura: "+mInvoice.getDateAcct());
				
				VWT_MInvoice invoice = new VWT_MInvoice(getCtx(), mInvoice.getC_Invoice_ID(), get_TrxName());
				cnt += invoice.recalcWithholdings(voucher);
			}
		}
		
		return "Retenciones Generadas: "+cnt;
	}

}
