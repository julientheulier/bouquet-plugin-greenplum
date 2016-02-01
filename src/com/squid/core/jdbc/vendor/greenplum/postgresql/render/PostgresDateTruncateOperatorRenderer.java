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
package com.squid.core.jdbc.vendor.greenplum.postgresql.render;

import com.squid.core.domain.extensions.DateTruncateOperatorDefinition;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.DateTruncateOperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

/**
 * @author luatnn
 * @rev 2011-06-29 by jth: for PostgreSQL and GreenPlum, cast and DATE_TRUNCate
 *      to date if input is both timestamp and date
 */
public class PostgresDateTruncateOperatorRenderer extends DateTruncateOperatorRenderer {

	@Override
	protected String prettyPrintTwoArgs(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args) throws RenderingException {
		//ExtendedType[] extendedTypes = getExtendedPieces(piece);
		if(DateTruncateOperatorDefinition.WEEK.equals(args[1].replaceAll("'", ""))) {
	        return "CAST(DATE_TRUNC('week', "+args[0]+") AS DATE)";
	    } else if(DateTruncateOperatorDefinition.MONTH.equals(args[1].replaceAll("'", ""))) {
	        return "CAST(DATE_TRUNC('month', "+args[0]+") AS DATE)";
	    } else if(DateTruncateOperatorDefinition.YEAR.equals(args[1].replaceAll("'", ""))) {
	        return "CAST(DATE_TRUNC('year', "+args[0]+") AS DATE)";
	    } else {
	    	// Even if it's a date, we have to truncate it and transform to a date in order to have a good rollup between day & week
	    	// Bug is that weeks are not displayed in the result set
	        return "CAST(DATE_TRUNC('day', "+args[0]+") AS DATE)";
	    }
	}

}
