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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squid.core.database.impl.DataSourceReliable;
import com.squid.core.database.impl.DatabaseServiceException;
import com.squid.core.database.model.Column;
import com.squid.core.database.model.Schema;
import com.squid.core.database.model.Table;
import com.squid.core.database.statistics.ColumnStatistics;
import com.squid.core.database.statistics.DatabaseStatistics;
import com.squid.core.database.statistics.ObjectStatistics;

/**
 * extract table/column statistics for Redshift
 * @author sergefantino
 *
 */
public class PostgresqlStatistics extends DatabaseStatistics {

    static final Logger logger = LoggerFactory.getLogger(PostgresqlStatistics.class);

    public PostgresqlStatistics(DataSourceReliable ds) {
        super(ds);
    }

    /**
     * extract statistics for all tables in the schema
     * @param schema
     * @throws DatabaseServiceException 
     */
    protected void computeTablesStatistics(Schema schema, Connection connection) throws ExecutionException {
        try {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                String sql = "select relname, reltuples from pg_class left join pg_namespace on relnamespace=pg_namespace.oid where nspname='"+schema.getName()+"'";
                ResultSet resultset = statement.executeQuery(sql);
                while (resultset.next()) {
                    String relname = resultset.getString(1);
                    float reltuples = resultset.getFloat(2);
                    Table table = schema.findTable(relname);
                    if (table!=null) {
                        putStatistics(table,new ObjectStatistics(reltuples));
                    }
                }
            } finally {
                if (statement!=null) statement.close();
            }
        } catch (SQLException e) {
            logger.info(e.getLocalizedMessage());
        }
    }

    /**
     * extract statistics for the tables's columns
     * @param table
     * @param connection
     * @throws DatabaseServiceException 
     */
    protected void computeColumnsStatistics(Table table, Connection connection) throws ExecutionException {
        try {
            Statement statement = null;
            try {
                ObjectStatistics tableStatistics = getStatistics(table, connection);
                float tableSize = tableStatistics!=null?tableStatistics.getSize():-1;
                statement = connection.createStatement();
                String sql = "select attname, n_distinct from pg_stats where schemaname='"+table.getSchema().getName()+"' and tablename='"+table.getName()+"'";
                ResultSet resultset = statement.executeQuery(sql);
                while (resultset.next()) {
                    String attname = resultset.getString(1);
                    float n_distinct = resultset.getFloat(2);
                    Column col = table.findColumnByName(attname);
                    if (col!=null) {
                        /*
                         * If greater than zero, the estimated number of distinct values in the column. 
                         * If less than zero, the negative of the number of distinct values divided by the number of rows. 
                         * (The negated form is used when ANALYZE believes that the number of distinct values is likely to increase as the table grows; 
                         * the positive form is used when the column seems to have a fixed number of possible values.) 
                         * For example, -1 indicates a unique column in which the number of distinct values is the same as the number of rows.
                         */
                        float stats = n_distinct>0?n_distinct:(-n_distinct*tableSize);
                        putStatistics(col,new ColumnStatistics(stats));
                    }
                }
            } finally {
                if (statement!=null) statement.close();
            }
        } catch (SQLException e) {
            logger.info(e.getLocalizedMessage());
        }
    }

}
