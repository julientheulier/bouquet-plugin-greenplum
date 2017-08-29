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

import java.io.IOException;
import java.sql.Types;

import com.squid.core.database.model.DatabaseProduct;
import com.squid.core.database.model.Table;
import com.squid.core.domain.CustomTypes;
import com.squid.core.domain.IDomain;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.IntrinsicOperators;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.FromTablePiece;
import com.squid.core.sql.db.templates.DefaultJDBCSkin;
import com.squid.core.sql.db.templates.ISkinProvider;
import com.squid.core.sql.render.DelegateSamplingDecorator;
import com.squid.core.sql.render.ISamplingDecorator;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;


public class PostgresSQLSkin extends DefaultJDBCSkin {

	public PostgresSQLSkin(ISkinProvider provider, DatabaseProduct product) {
		super(provider, product);
	}

	@Override
	protected void initFormat() {
		super.initFormat();
		setIdentifier_quote("\"");
		setLiteral_quote("\'");
	}

	@Override
	public String overrideTemplateID(String templateID) {
		// TODO Auto-generated method stub
		return super.overrideTemplateID(templateID);
	}

	@Override
	public String getToken(int token) throws RenderingException {
		switch (token) {
			default:
				return super.getToken(token);
		}
	}

	@Override
	public ISamplingDecorator createSamplingDecorator(DelegateSamplingDecorator sampling) {
		return new SamplingPiece(sampling);
	}

	@Override
	protected String render(SQLSkin skin,FromTablePiece piece) throws RenderingException, IOException {
		String render = "";
		final Table table = piece.getTable();
		if (table==null) {
			throw new RenderingException("table definition is null");
		}
		//
		if (piece.getSamplingDecorator()!=null) {
			// sampling version
			// select Y.x from (select * from table where random()<.y) as Y
			//
			render += " ( select * from ";
			if (table.getSchema()!=null&&!table.getSchema().isNullSchema()) {
				render += skin.quoteSchemaIdentifier(table.getSchema());
				render += ".";
			}
			render += skin.quoteTableIdentifier(table);
			// sampling
			if (piece.getSamplingDecorator().getMode()==ISamplingDecorator.FRACTION) {
				render += " where random()<"+piece.getSamplingDecorator().getPercent()/100;
			} else {
				render += " limit "+piece.getSamplingDecorator().getSize();
			}
			render += " ) as "+ piece.getAlias();
		} else {
			if (table.getSchema()!=null&&!table.getSchema().isNullSchema()) {
				render += skin.quoteSchemaIdentifier(table.getSchema());
				render += ".";
			}
			render += skin.quoteTableIdentifier(table);
			//
			// alias
			render += " "+piece.getAlias();
		}
		//
		// joining
		render += renderJoinDecorator(skin,piece);
		return render;
	}

	@Override
	public String quoteConstant(Object value, IDomain domain) {
		if (domain.isInstanceOf(IDomain.BOOLEAN)) {
			if (value instanceof Boolean) {
				return (Boolean)value?"true":"false";
			} else if (value instanceof Double && Math.floor((Double)value)==(Double)value) {
				return new Integer(((Double)value).intValue()).toString();
			} else {
				return value.toString();
			}
		} else {
			return super.quoteConstant(value, domain);
		}
	}

	@Override
	public ExtendedType createExtendedType(IDomain domain, int dataType,
			String format, int size, int precision) {
		if (dataType!=Types.NULL) {
			return new ExtendedType(domain, dataType, precision, size);
		} else {
			return new ExtendedType(domain, computeDataType(domain, dataType, format, size, precision), precision, size);
		}
	}

	protected int computeDataType(IDomain domain, int dataType, String format, int size, int precision) {
		if (domain.isInstanceOf(IDomain.NUMERIC)) {
			if (precision==0) {
				if (size<=2) {
					return java.sql.Types.SMALLINT;
				} else if (size<=4) {
					return java.sql.Types.INTEGER;
				} else if (size<=8) {
					return java.sql.Types.BIGINT;
				} else {
					return java.sql.Types.NUMERIC;
				}
			} else if (precision>0) {
				return java.sql.Types.NUMERIC;
			} else {
				if (size<=6) {
					return java.sql.Types.REAL;
				} else {
					return java.sql.Types.DOUBLE;
				}
			}
		} else
			if (domain.isInstanceOf(IDomain.STRING)) {
				if (size>5) {
					return java.sql.Types.VARCHAR;
				} else {
					return java.sql.Types.CHAR;
				}
			} else
				if (domain.isInstanceOf(IDomain.TIMESTAMP)) {
					return java.sql.Types.TIMESTAMP;
				} else
					if (domain.isInstanceOf(IDomain.DATE)) {
						if (format!=null && format.contains("HH")) {
							// support timestamp
							return java.sql.Types.TIMESTAMP;
						} else {
							return java.sql.Types.DATE;
						}
					} else
						if (domain.isInstanceOf(IDomain.TIME)) {
							return java.sql.Types.TIME;
						} else {
							return java.sql.Types.NULL;
						}
	}

	@Override
	public String getTypeName(int SQLType) {
		switch (SQLType) {
			case Types.DOUBLE:
				return "DOUBLE PRECISION";
			case Types.DECIMAL:
				return "NUMERIC";// DECIMAL = NUMERIC
			default:
				return super.getTypeName(SQLType);
		}
	}

	@Override
	public String getTypeDefinition(ExtendedType type) {
		if (type==null) {
			return "NULL";
		}
		switch (type.getDataType()) {
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return getTypeName(type.getDataType());
			case CustomTypes.INTERVAL:
				return "INTERVAL";
			case Types.TINYINT:
				return "SMALLINT";
			default:
				if (type.getName().equalsIgnoreCase("nvarchar")) {
					return "VARCHAR("+type.getSize()+")";
				} else if (type.getName().equalsIgnoreCase("nchar")) {
					return "CHAR("+type.getSize()+")";
				}
				return super.getTypeDefinition(type);
		}
	}

	@Override
	public String render(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args) throws RenderingException {
		if (opDef.getId()==IntrinsicOperators.MODULO) {
			return opDef.prettyPrint("MOD", OperatorDefinition.PREFIX_POSITION, args, true);
		} else if (opDef.getId()==IntrinsicOperators.CONCAT) {
			return opDef.prettyPrint("||", OperatorDefinition.INFIX_POSITION, args, true);
		} else {
			return super.render(skin, piece, opDef, args);
		}
	}

	@Override
	public String fullyQualified(Table table) {
		String res = "";
		if (table.getSchema()!=null&&!table.getSchema().isNullSchema() //&& !table.isGlobalTemporary()
				) {
			res += quoteSchemaIdentifier(table.getSchema())+".";
		}
		res += quoteTableIdentifier(table);
		return res;
	}

}
