/*******************************************************************************
 * Copyright © Squid Solutions, 2016
 *
 * This file is part of Open Bouquet software.
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * There is a special FOSS exception to the terms and conditions of the 
 * licenses as they are applied to this program. See LICENSE.txt in
 * the directory of this program distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Squid Solutions also offers commercial licenses with additional warranties,
 * professional functionalities or services. If you purchase a commercial
 * license, then it supersedes and replaces any other agreement between
 * you and Squid Solutions (above licenses and LICENSE.txt included).
 * See http://www.squidsolutions.com/EnterpriseBouquet/
 *******************************************************************************/
package com.squid.core.jdbc.vendor.greenplum.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.lang.StringUtils;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGobject;

import com.squid.core.jdbc.formatter.DataFormatter;
import com.squid.core.jdbc.formatter.DefaultJDBCDataFormatter;


public class PostgresqlJDBCDataFormatter
extends DefaultJDBCDataFormatter {

	public PostgresqlJDBCDataFormatter(DataFormatter formatter, Connection connection) {
		super(formatter, connection);
	}

	@Override
	public Object unboxJDBCObject(final Object column, final int colType) throws SQLException {
		if (colType==Types.CHAR && column!=null) {
			return StringUtils.stripEnd((String)column," ");
		} else if (column instanceof PGInterval) {
			return column.toString();
		} else if (column instanceof PGobject) {
			return column.toString();
		//} else if (column instanceof java.sql.Date) {
		//	return new Date(((java.sql.Date)column).getTime());
		}
		return column;
	}

	@Override
	public String formatJDBCObject(final Object column, final int colType) throws SQLException {
		if (column== null) {
			return "";
		}
		if (column instanceof PGInterval) {
			return (String) unboxJDBCObject(column, colType);
		} else if (column instanceof PGobject) {
			return (String) unboxJDBCObject(column, colType);
		}
		return super.formatJDBCObject(column,colType);
	}


}
