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

import java.util.List;

import com.squid.core.database.impl.DataSourceReliable;
import com.squid.core.database.metadata.IMetadataEngine;
import com.squid.core.database.model.DatabaseProduct;
//import com.squid.core.db.engine.model.session.IExecutionItem;
import com.squid.core.domain.extensions.date.AddMonthsOperatorDefinition;
import com.squid.core.domain.extensions.date.DateTruncateOperatorDefinition;
import com.squid.core.domain.extensions.date.DateTruncateShortcutsOperatorDefinition;
import com.squid.core.domain.extensions.date.extract.ExtractOperatorDefinition;
import com.squid.core.domain.extensions.date.operator.DateOperatorDefinition;
import com.squid.core.domain.extensions.string.PosStringOperatorDefinition;
import com.squid.core.domain.extensions.string.SubstringOperatorDefinition;
import com.squid.core.domain.extensions.string.regex.RegexpOperatorDefinition;
import com.squid.core.domain.maths.RandOperatorDefinition;
import com.squid.core.domain.maths.SinhCoshTanhOperatorDefintion;
import com.squid.core.domain.operators.AggregateOperatorDefinition;
import com.squid.core.domain.operators.CoVarPopOperatorDefinition;
import com.squid.core.domain.operators.IntrinsicOperators;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.domain.operators.StdevPopOperatorDefinition;
import com.squid.core.sql.db.features.IGroupingSetSupport;
import com.squid.core.sql.db.features.IMetadataForeignKeySupport;
import com.squid.core.sql.db.features.IRollupStrategySupport;
import com.squid.core.sql.db.render.AddMonthsAsIntervalOperatorRenderer;
import com.squid.core.sql.db.render.DateAddSubOperatorRenderer;
import com.squid.core.sql.db.render.DateEpochOperatorRenderer;
import com.squid.core.sql.db.render.MetatdataSearchFeatureSupport;
import com.squid.core.sql.db.render.OrderedAnalyticOperatorRenderer;
import com.squid.core.sql.db.templates.DefaultJDBCSkin;
import com.squid.core.sql.db.templates.DefaultSkinProvider;
import com.squid.core.sql.db.templates.ISkinProvider;
import com.squid.core.sql.db.templates.SkinRegistry;
import com.squid.core.sql.render.ISkinFeatureSupport;
import com.squid.core.sql.render.SQLSkin;
import com.squid.core.sql.render.ZeroIfNullFeatureSupport;
import com.squid.core.sql.statements.SelectStatement;

public class PostgresSkinProvider extends DefaultSkinProvider {

	private static final ZeroIfNullFeatureSupport zeroIfNull = new ANSIZeroIfNullFeatureSupport();

	public PostgresSkinProvider() {
		// registerOperatorRender("com.sodad.domain.operator.density.PERCENTILE", new PercentileRenderer());
		registerOperatorRender("com.sodad.domain.operator.density.EQWBUCKET", new WidthBucketRenderer());
		//
		registerOperatorRender(PosStringOperatorDefinition.STRING_POSITION, new PosStringRenderer());
		registerOperatorRender(SubstringOperatorDefinition.STRING_SUBSTRING, new SubStringRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.SUM), new OrderedAnalyticOperatorRenderer());
		//
		registerOperatorRender(RandOperatorDefinition.RAND, new PostgresRandOperatorRenderer());
		registerOperatorRender(SinhCoshTanhOperatorDefintion.SINH, new MySQLPosgresSinhOperatorRenderer());
		registerOperatorRender(SinhCoshTanhOperatorDefintion.COSH, new MySQLPosgresCoshOperatorRenderer());
		registerOperatorRender(SinhCoshTanhOperatorDefintion.TANH, new MySQLPosgresTanhOperatorRenderer());
		//
		registerOperatorRender(DateOperatorDefinition.DATE_ADD, new PostgresDateAddSubOperatorRenderer(DateAddSubOperatorRenderer.OperatorType.ADD));
		registerOperatorRender(DateOperatorDefinition.DATE_SUB, new PostgresDateAddSubOperatorRenderer(DateAddSubOperatorRenderer.OperatorType.SUB));
		registerOperatorRender(AddMonthsOperatorDefinition.ADD_MONTHS, new AddMonthsAsIntervalOperatorRenderer());
		registerOperatorRender(DateOperatorDefinition.DATE_MONTHS_BETWEEN, new MonthsBetweenRenderer());
		registerOperatorRender(ExtractOperatorDefinition.EXTRACT_DAY_OF_WEEK, new PostgresExtractOperatorRenderer("DOW"));
		registerOperatorRender(ExtractOperatorDefinition.EXTRACT_DAY_OF_YEAR, new PostgresExtractOperatorRenderer("DOY"));
		registerOperatorRender(DateOperatorDefinition.FROM_UNIXTIME, new PostgresDateEpochOperatorRenderer(DateEpochOperatorRenderer.FROM));
		registerOperatorRender(DateOperatorDefinition.TO_UNIXTIME, new PostgresDateEpochOperatorRenderer(DateEpochOperatorRenderer.TO));
		registerOperatorRender(DateTruncateOperatorDefinition.DATE_TRUNCATE, new PostgresDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.HOURLY_ID, new PostgresDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.DAILY_ID, new PostgresDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.WEEKLY_ID, new PostgresDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.MONTHLY_ID, new PostgresDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.QUARTERLY_ID, new PostgresDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.YEARLY_ID, new PostgresDateTruncateOperatorRenderer());
		//
		// See Ticket #1620
		// registerOperatorRender(IntervalOperatorDefinition.INTERVAL_DAY, new PostgresIntervalOperatorRenderer("DAY"));
		// registerOperatorRender(IntervalOperatorDefinition.INTERVAL_MONTH, new PostgresIntervalOperatorRenderer("MONTH"));
		// registerOperatorRender(IntervalOperatorDefinition.INTERVAL_YEAR, new PostgresIntervalOperatorRenderer("YEAR"));
		// registerOperatorRender(IntervalOperatorDefinition.INTERVAL_HOUR, new PostgresIntervalOperatorRenderer("HOUR"));
		// registerOperatorRender(IntervalOperatorDefinition.INTERVAL_MINUTE, new PostgresIntervalOperatorRenderer("MINUTE"));
		// registerOperatorRender(IntervalOperatorDefinition.INTERVAL_SECOND, new PostgresIntervalOperatorRenderer("SECOND"));
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.VARIANCE), new VarStdevRenderer());
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.VAR_SAMP), new VarStdevRenderer());
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.STDDEV_POP), new VarStdevRenderer());
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.STDDEV_SAMP), new VarStdevRenderer());
		registerOperatorRender(CoVarPopOperatorDefinition.getExtendedId(IntrinsicOperators.COVAR_POP), new CoVarRenderer());
		registerOperatorRender(AggregateOperatorDefinition.getExtendedId(IntrinsicOperators.AVG), new PostgresAvgRenderer());
		//
		registerOperatorRender(RegexpOperatorDefinition.REGEXP_COUNT, new PostgresRegexpOperatorRenderer("REGEXP_COUNT"));
		registerOperatorRender(RegexpOperatorDefinition.REGEXP_INSTR, new PostgresRegexpOperatorRenderer("REGEXP_INSTR"));

	}

	@Override
	public double computeAccuracy(DatabaseProduct product) {
		try {
			if (product != null) {
				if (IMetadataEngine.POSTGRESQL_NAME.equalsIgnoreCase(product.getProductName())) {
					return PERFECT_MATCH;
				} else {
					return NOT_APPLICABLE;
				}
			} else {
				return NOT_APPLICABLE;
			}
		} catch (Exception e) {
			return NOT_APPLICABLE;
		}
	}

	@Override
	public SQLSkin createSkin(DatabaseProduct product) {
		return new PostgresSQLSkin(this, product);
	}

	@Override
	public ISkinFeatureSupport getFeatureSupport(DefaultJDBCSkin skin, String featureID) {
		if (featureID == DataSourceReliable.FeatureSupport.GROUPBY_ALIAS) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		}
		if (featureID == SelectStatement.SampleFeatureSupport.SELECT_SAMPLE) {
			return SAMPLE_SUPPORT;
		}
		if (featureID == ZeroIfNullFeatureSupport.ID) {
			return zeroIfNull;
		}
		if (featureID == IMetadataForeignKeySupport.ID) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		} else if (featureID == MetatdataSearchFeatureSupport.METADATA_SEARCH_FEATURE_ID) {
			return METADATA_SEARCH_SUPPORT;
		} else if (featureID == IGroupingSetSupport.ID) {
			return IGroupingSetSupport.IS_NOT_SUPPORTED;
		} else if (featureID == DataSourceReliable.FeatureSupport.AUTOCOMMIT) {
			return ISkinFeatureSupport.IS_NOT_SUPPORTED;
		} else if (featureID.equals(IRollupStrategySupport.ID)) {
			return IRollupStrategySupport.OPTIMIZE_USING_WITH_STRATEGY;
		}
		// else
		return super.getFeatureSupport(skin, featureID);
	}

	private SelectStatement.SampleFeatureSupport SAMPLE_SUPPORT = new SelectStatement.SampleFeatureSupport() {

		@Override
		public boolean isCountSupported() {
			return false;
		}

		@Override
		public boolean isPercentageSupported() {
			return true;
		}

	};

	private MetatdataSearchFeatureSupport METADATA_SEARCH_SUPPORT = new MetatdataSearchFeatureSupport() {

		@Override
		public String createTableSearch(List<String> schemas, String tableName, boolean isCaseSensitive) {
			StringBuilder sqlCode = new StringBuilder();
			sqlCode.append("SELECT  N.NSPNAME AS SCHEMANAME, C.RELNAME AS TABLENAME, D.DESCRIPTION");
			sqlCode.append(CR_LF);
			// from table
			sqlCode.append(" FROM PG_CLASS C");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT JOIN PG_NAMESPACE N ON N.OID = C.RELNAMESPACE");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT JOIN PG_TABLESPACE T ON T.OID = C.RELTABLESPACE");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT OUTER JOIN PG_DESCRIPTION D ON D.OBJOID=C.RELFILENODE AND D.OBJSUBID=0");
			sqlCode.append(CR_LF);
			// where condition
			sqlCode.append(" WHERE C.RELKIND = 'r'::\"char\"");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND N.NSPNAME NOT IN ('INFORMATION_SCHEMA','PG_CATALOG')");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND N.NSPNAME IN (" + getGroupSchemaNames(schemas) + ")");
			sqlCode.append(CR_LF);
			// group by
			sqlCode.append(" GROUP BY N.NSPNAME, C.RELFILENODE, C.RELNAME, D.DESCRIPTION");
			sqlCode.append(CR_LF);
			sqlCode.append(" HAVING (" + applyCaseSensitive("C.RELNAME", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + " OR "
					+ applyCaseSensitive("D.DESCRIPTION", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + ")");
			sqlCode.append(CR_LF);
			// order by
			sqlCode.append(" ORDER BY N.NSPNAME, C.RELNAME");
			return sqlCode.toString();
		}

		@Override
		public String createColumnSearch(List<String> schemas, String tableName, String columnName, boolean isCaseSensitive) {
			StringBuilder sqlCode = new StringBuilder();
			sqlCode.append("SELECT N.NSPNAME AS SCHEMANAME, C.RELNAME, ATTNAME, DESCRIPTION");
			sqlCode.append(CR_LF);
			// from table
			sqlCode.append(" FROM PG_CLASS C");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT JOIN PG_ATTRIBUTE A ON C.OID = A.ATTRELID");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT JOIN PG_NAMESPACE N ON N.OID = C.RELNAMESPACE");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT OUTER JOIN PG_DESCRIPTION ON (OBJOID = C.OID) AND (ATTNUM = OBJSUBID)");
			sqlCode.append(CR_LF);
			// where condition
			sqlCode.append(" WHERE A.ATTNUM > 0");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND C.RELFILENODE IN");
			sqlCode.append(CR_LF);
			// sub select
			sqlCode.append(" (SELECT  C.RELFILENODE");
			sqlCode.append(CR_LF);
			sqlCode.append(" FROM PG_CLASS C");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT JOIN PG_NAMESPACE N ON N.OID = C.RELNAMESPACE");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT JOIN PG_TABLESPACE T ON T.OID = C.RELTABLESPACE");
			sqlCode.append(CR_LF);
			sqlCode.append(" LEFT OUTER JOIN PG_DESCRIPTION D ON D.OBJOID=C.RELFILENODE AND D.OBJSUBID=0");
			sqlCode.append(CR_LF);
			sqlCode.append(" WHERE C.RELKIND = 'r'::\"char\"");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND N.NSPNAME NOT IN ('INFORMATION_SCHEMA','PG_CATALOG')");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND N.NSPNAME IN (" + getGroupSchemaNames(schemas) + ")");
			sqlCode.append(CR_LF);
			sqlCode.append(" GROUP BY C.RELFILENODE, C.RELNAME, D.DESCRIPTION");
			if (tableName != null) {
				sqlCode.append(CR_LF);
				sqlCode.append(" HAVING (" + applyCaseSensitive("C.RELNAME", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + " OR "
						+ applyCaseSensitive("D.DESCRIPTION", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + "))");
			} else {
				sqlCode.append(")");
			}
			// end sub select
			sqlCode.append(CR_LF);
			// group by
			sqlCode.append(" GROUP BY  N.NSPNAME, C.RELNAME, ATTNAME, DESCRIPTION");
			sqlCode.append(CR_LF);
			sqlCode.append(" HAVING (" + applyCaseSensitive("ATTNAME", isCaseSensitive) + " LIKE " + applyCaseSensitive(columnName, isCaseSensitive) + " OR "
					+ applyCaseSensitive("DESCRIPTION", isCaseSensitive) + " LIKE " + applyCaseSensitive(columnName, isCaseSensitive) + ")");
			sqlCode.append(CR_LF);
			// order by
			sqlCode.append(" ORDER BY N.NSPNAME, C.RELNAME, ATTNAME");
			return sqlCode.toString();
		}

	};

	@Override
	public String getSkinPrefix(DatabaseProduct product) {
		return "postgresql";
	}

	@Override
	public ISkinProvider getParentSkinProvider() {
		return SkinRegistry.INSTANCE.findSkinProvider(DefaultSkinProvider.class);
	}

}
