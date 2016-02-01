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

import com.squid.core.database.metadata.GenericMetadataSupport;

public class PostgresqlMetadataSupport extends GenericMetadataSupport {
    
    /**
     * check if the database support UTF-8 surrogate Characters - for sure Redshift doesn't like it
     * @return
     */
    public boolean handleSurrogateCharacters() {
        return false;
    }
    
    private String[] systemSchemas = new String[]{"information_schema","pg_catalog","pg_internal"};
    
    @Override
    public boolean isSystemSchema(String name) {
    	for (int i=0;i<systemSchemas.length;i++) {
    		if (systemSchemas[i].equalsIgnoreCase(name)) return true;
    	}
    	// else
    	return false;
    }

}
