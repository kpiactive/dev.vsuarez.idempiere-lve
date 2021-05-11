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

package ve.net.dcs.component;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.base.IModelFactory;
import org.compiere.model.MEntityType;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import ve.net.dcs.info.VWTPluginFeatures;

/**
 * Generic Model Factory for Voucher Withholding Tax
 * 
 * @author Double Click Sistemas C.A. - http://dcs.net.ve
 * @author Angel Parra 	-	aparra@dcs.net.ve
 * @author Saul Piña 	-	spina@dcs.net.ve
 */
public class VWTModelFactory implements IModelFactory {

	private final static CLogger log = CLogger.getCLogger(VWTModelFactory.class);
	private static CCache<String, Class<?>> cache = new CCache<String, Class<?>>("PO_Class", 20);
	
	private final static String prefixModel = "M";
	private final static String prefixModelDefault = "X_";
	
	@Override
	public Class<?> getClass(String tableName) {

		if (tableName == null)
			return null;

		Class<?> clazz = cache.get(tableName);

		if (clazz == null) {

			MTable table = MTable.get(Env.getCtx(), tableName);
			String entityType = table.getEntityType();
			
			Arrays.sort(VWTPluginFeatures.entityType);
			if (Arrays.binarySearch(VWTPluginFeatures.entityType, entityType)< 0)
				return null;

			MEntityType et = MEntityType.get(Env.getCtx(), entityType);
			String modelPackage = et.getModelPackage();

			String classNameFormat = "%s.%s%s";

			try {
				clazz = Class.forName(String.format(classNameFormat, modelPackage, prefixModel, tableName.replace("_", "")));
				cache.put(tableName, clazz);
			} catch (Exception e1) {
				try {
					clazz = Class.forName(String.format(classNameFormat, modelPackage, prefixModelDefault, tableName));
					cache.put(tableName, clazz);
				} catch (Exception e2) {
					if (log.isLoggable(Level.WARNING))
						log.warning(String.format("Plugin: %s -> Class not found for table: %s", VWTPluginFeatures.id, tableName));
				}
			}
		}

		return clazz;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {

		Class<?> clazz = getClass(tableName);
		if (clazz == null)
			return null;

		PO model = null;
		Constructor<?> constructor = null;

		try {
			constructor = clazz.getDeclaredConstructor(new Class[] { Properties.class, int.class, String.class });
			model = (PO) constructor.newInstance(new Object[] { Env.getCtx(), new Integer(Record_ID), trxName });
		} catch (Exception e) {
			if (log.isLoggable(Level.WARNING))
				log.warning(String.format("Plugin: %s -> Class can not be instantiated for table: %s", VWTPluginFeatures.id, tableName));
		}

		return model;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {

		Class<?> clazz = getClass(tableName);
		if (clazz == null)
			return null;

		PO model = null;
		Constructor<?> constructor = null;

		try {
			constructor = clazz.getDeclaredConstructor(new Class[] { Properties.class, ResultSet.class, String.class });
			model = (PO) constructor.newInstance(new Object[] { Env.getCtx(), rs, trxName });
		} catch (Exception e) {
			if (log.isLoggable(Level.WARNING))
				log.warning(String.format("Plugin: %s -> Class can not be instantiated for table: %s", VWTPluginFeatures.id, tableName));
		}

		return model;
	}

}
