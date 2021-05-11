/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Contributor(s): Victor Suárez www.dcs.net.ve
 *****************************************************************************/

package ve.net.dcs.process;

import java.io.File;
import java.io.FileOutputStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MTable;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ve.net.dcs.model.X_LVE_generateXML;

/**
 * Localizacion Venezuela
 * LVE_GenerateXmlSeniat
 * @author Victor Suarez  -  vsuarez@dcs.net.ve  - Double Click Sistemas C.A.
 * 2012
 *
 */
public class LVE_GenerateXmlSeniat extends SvrProcess {

	/**
	 * 
	 */
	public LVE_GenerateXmlSeniat() {
		// TODO Auto-generated constructor stub
	}
	
	/**Organization                  */
	private int	p_AD_Org_ID = 0;	
	/** ValidFrom               	*/
	private Timestamp 	p_ValidFrom=null;
	/** ValidTo                 	*/
	private Timestamp   p_ValidTo=null;
	/** Record_ID               */
	private int p_Record_ID = 0;
	/** X_LVE_generateXML       */
	private int p_LVE_generateXML_ID = 0;
	
	/* (non-Javadoc)
	 * @see org.compiere.process.SvrProcess#prepare()
	 */
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
		p_Record_ID = getRecord_ID();
		p_LVE_generateXML_ID = p_Record_ID;
		X_LVE_generateXML generateXML = new X_LVE_generateXML(getCtx(), p_LVE_generateXML_ID, get_TrxName());
		
		p_AD_Org_ID = generateXML.getAD_Org_ID();
		p_ValidFrom = generateXML.getValidFrom();
		p_ValidTo = generateXML.getValidTo();
		
		log.log(Level.INFO, "*********  Prepare  **********");
		log.log(Level.INFO, "Parameters: " + "AD_Org_ID: " + p_AD_Org_ID + ", ValidFrom: " + p_ValidFrom + ", ValidTo: " + p_ValidTo + ", Record_ID: " + p_Record_ID);
		
	}

	/* (non-Javadoc)
	 * @see org.compiere.process.SvrProcess#doIt()
	 */
	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		
		String sql="";
		Element root=new Element("RelacionRetencionesISLR");
		
		String dia, mes , anio;
		Calendar date = new GregorianCalendar();
		dia = Integer.toString(date.get(Calendar.DATE));
		mes = Integer.toString(date.get(Calendar.MONTH) + 1);
		anio = Integer.toString(date.get(Calendar.YEAR));
		String fecha = dia + mes + anio;
		
		String nombreArch="XML_Seniat";
		String fileNameXML=nombreArch + fecha + ".xml";
		
		FileOutputStream file=new FileOutputStream(fileNameXML);
	   
		sql=("SELECT *, to_char(fecha,'DD/MM/YYYY') as fechaoperacion "
				+ " FROM lve_xmlislr " 
				+ " WHERE " 
				+ " lve_xmlislr.org = '" + p_AD_Org_ID + "' AND "
				+ " (lve_xmlislr.fecha BETWEEN '" + p_ValidFrom + "' AND '"+ p_ValidTo +"')" 
				);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
	try {
		pstmt = DB.prepareStatement(sql, null);
		rs = pstmt.executeQuery();
		
		while (rs.next())
		{
				root.setAttribute("RifAgente", rs.getString(8).trim());
				root.setAttribute("Periodo", rs.getString(9).trim());
			    		    
		//  Creamos un hijo para el root
				 Element detalleRetencion=new Element("DetalleRetencion");
				 if (rs.getString(1) != null)
					 detalleRetencion.addContent(new Element("RifRetenido").setText(rs.getString(1).trim()));
				 else
					 detalleRetencion.addContent(new Element("RifRetenido").setText("Vacio"));
				 
				 if (rs.getString(2) != null)
					 detalleRetencion.addContent(new Element("NumeroFactura").setText(rs.getString(2).trim()));
				 else
					 detalleRetencion.addContent(new Element("NumeroFactura").setText("Vacio"));
				 
				 if (rs.getString(3) != null)
					 detalleRetencion.addContent(new Element("NumeroControl").setText(rs.getString(3).trim()));
				 else
					 detalleRetencion.addContent(new Element("NumeroControl").setText("Vacio"));
				 
				 if (rs.getString(11) != null)
					 detalleRetencion.addContent(new Element("FechaOperacion").setText(rs.getString(11).trim()));
				 else
					 detalleRetencion.addContent(new Element("FechaOperacion").setText("Vacio"));
			    
				 if (rs.getString(4) != null)
					 detalleRetencion.addContent(new Element("CodigoConcepto").setText(rs.getString(4).trim()));
				 else
					 detalleRetencion.addContent(new Element("CodigoConcepto").setText("Vacio"));
				
			    detalleRetencion.addContent(new Element("MontoOperacion").setText(rs.getString(5).trim()));
			    detalleRetencion.addContent(new Element("PorcentajeRetencion").setText(rs.getString(6).trim()));
			    
			    root.addContent(detalleRetencion);
		    			      
			//   //Agregamos al root
			}
		}	
		catch ( Exception e )
        {
            System.out.println(e.getMessage());
        }
		
		
		pstmt.close();
		
	 //Creamos el documento
		Document doc=new Document(root);
		
		try{	
		      Format format = Format.getPrettyFormat();
		      format.setEncoding("ISO-8859-1");
		      log.log(Level.INFO, "Format XML");
		      XMLOutputter out=new XMLOutputter();
		      out.setFormat(format);
		      out.output(doc,file);
		      file.flush();
		      file.close();
			
		    }catch(Exception e){e.printStackTrace();}
		
		File archivoXML=new File(fileNameXML);
		
		if (!rs.equals(null))
		{
			int  AD_Table_ID = MTable.getTable_ID(X_LVE_generateXML.Table_Name);
			log.log(Level.INFO, "AD_Table_ID: " + AD_Table_ID + " - Record_ID: " + p_Record_ID);
			MAttachment attach =  MAttachment.get(getCtx(),AD_Table_ID,p_Record_ID);
		
			if (attach == null ) {
				log.info("attach == null: ");
				log.log(Level.INFO, "AD_Table_ID: " + AD_Table_ID + " - Record_ID: " + p_Record_ID);
				attach = new  MAttachment(getCtx(),AD_Table_ID ,p_Record_ID,get_TrxName());
				attach.addEntry(archivoXML);
				attach.save();
				log.info("attach.save");
			} else {
				log.info("attach != null: ");
				log.log(Level.INFO, "AD_Table_ID: " + AD_Table_ID + " - Record_ID: " + p_Record_ID);
				int index = (attach.getEntryCount()-1);
				MAttachmentEntry entry = attach.getEntry(index) ;
				String renamed = nombreArch + fecha + "_old" + ".xml";
				entry.setName(renamed);
				attach.save();
				//agrega el nuevo archivo ya q el anterior ha sido renombrado
				attach.addEntry(archivoXML);
				attach.save();
				}
			
			return "Archivo Generado y Anexado:  -> " + fileNameXML + ", Refrescar Ventana y revisar en Anexos.	";
			
		} else
			return "El Archivo no pudo ser Generado porque no hay retenciones ISLR para este periodo, desde: " + p_ValidFrom + ", hasta: " + p_ValidTo + ".";

			
		}
		
}
