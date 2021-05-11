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

package ve.net.dcs.callout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.adempiere.base.IColumnCallout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.globalqss.model.I_LCO_TaxPayerType;
import org.globalqss.model.X_LCO_TaxIdType;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * Seniat Validator
 * 
 * @author Double Click Sistemas C.A. - http://dcs.net.ve
 * @author Saul Pina - spina@dcs.net.ve
 */
public class VWTSeniatValidator implements IColumnCallout {

	/**
	 * CONFIGURATION: 
	 * Name: URL_SENIAT 
	 * Description: Url para consulta del Rif del Seniat Configured 
	 * Value: http://contribuyente.seniat.gob.ve/getContribuyente/getrif?rif=
	 */
	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {

		String urlSeniat = MSysConfig.getValue("URL_SENIAT", MSysConfig.getValue("URL_SENIAT"), Env.getAD_Client_ID(Env.getCtx()));

		if (urlSeniat == null)
			return "URL del Seniat no se encuentra en el sistema";

		if (mTab.getValue("TaxID") == null)
			return "Número de identificación obligatorio";

		String taxid = mTab.getValue("TaxID").toString().toUpperCase().replaceAll("[\\-\\ ]", "");
		X_LCO_TaxIdType taxidType = new X_LCO_TaxIdType(Env.getCtx(), (int) mTab.getValue("LCO_TaxIdType_ID"), null);

		String file = null;

		//file = searchRif(urlSeniat, taxidType.getName() + taxid,taxidType.getName());
		file = searchRif(urlSeniat, taxid,taxidType.getName());

		if (file == null)
			return "Contribuyente no encontrado en Seniat";

		Map<String, String> data = readData(file);

		String name = "";
		if (data.get("numeroRif").matches("[VE][0-9]+"))
			name = data.get("Nombre").trim();
		else
			name = data.get("Nombre").replaceAll("\\(.+\\)", "").trim();
		
		if(MSysConfig.getValue("LVE_ReplaceTaxInfoSeniat","Y",(Integer)mTab.getValue("AD_Client_ID")).compareTo("Y")==0){
			mTab.setValue("TaxID", data.get("numeroRif"));
			mTab.setValue("Name", name);
		}else{
			mTab.setValue("LVE_nameSeniat", name);
			mTab.setValue("LVE_rifSeniat", data.get("numeroRif"));
		}

		int LCO_TaxPayerType_id = 0;
		String LCO_TaxPayerTypeName = "";

		if (data.get("AgenteRetencionIVA").equalsIgnoreCase("SI")) {
			LCO_TaxPayerTypeName = "CONTRIBUYENTE ESPECIAL";
		} else if (data.get("ContribuyenteIVA").equalsIgnoreCase("NO")) {
			LCO_TaxPayerTypeName = "EXONERADO";
		} else if (data.get("numeroRif").matches("[JG][0-9]+")) {
			LCO_TaxPayerTypeName = String.format("ORDINARIO JURIDICO %s%%", data.get("Tasa"));
		} else if (data.get("numeroRif").matches("[VE][0-9]+")) {
			LCO_TaxPayerTypeName = String.format("ORDINARIO NATURAL %s%%", data.get("Tasa"));
		}

		LCO_TaxPayerType_id = new Query(ctx, I_LCO_TaxPayerType.Table_Name, "trim(Name) = ? AND AD_Client_ID = ? ",null).setParameters(LCO_TaxPayerTypeName,(Integer)mTab.getValue("AD_Client_ID")).firstIdOnly();
		mTab.setValue("LCO_TaxPayerType_ID", LCO_TaxPayerType_id);

		return null;
	}

	public String addMinus(String string) {
		return String.format("%s-%s-%s", string.substring(0, 1), string.substring(1, string.length() - 1), string.substring(string.length() - 1));
	}

	public String searchRif(String url, String taxid, String type){
		if (url == null)
			return null;

		String file = null;
		for (int i = 1; i <= 2; i++) {
			file = readFile(url + taxid);

			if (file != null)
				return file;

		}
		
		if (type.equalsIgnoreCase("V") || type.equalsIgnoreCase("CI")){
			for (int i = 0; i <= 9; i++) {
				for (int j = 1; j <= 2; j++) {
					file = readFile(url + taxid + i);
					if (file != null) {
						return file;
					}
				}
			}
		}

		return null;
	}

	public String readFile(String url) {

		if (url == null)
			return null;

		try {
			URL urlInput = new URL(url);
			URLConnection con = urlInput.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("ISO-8859-1")));

			
			String temp = "";
			String file = "";
			while ((temp = buffer.readLine()) != null) {
				file = file + temp;
			}
			return file;
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Data: Nombre, AgenteRetencionIVA, ContribuyenteIVA, Tasa, numeroRif
	 */
	public Map<String, String> readData(String file) {

		if (file == null)
			return null;

		try {
			Map<String, String> data = new HashMap<String, String>();

			InputSource archivo = new InputSource();
			archivo.setCharacterStream(new StringReader(file));

			SAXBuilder builder = new SAXBuilder();

			Document document = (Document) builder.build(archivo);
			Element rootNode = document.getRootElement();

			List<Attribute> attributes = rootNode.getAttributes();
			List<Element> elements = rootNode.getChildren();

			for (int i = 0; i < attributes.size(); i++) {
				Attribute attribute = attributes.get(i);
				data.put(attribute.getName(), attribute.getValue());
			}

			for (int i = 0; i < elements.size(); i++) {
				Element element = elements.get(i);
				data.put(element.getName(), element.getValue());
			}

			return data;
		} catch (Exception e) {
			return null;
		}

	}

}
