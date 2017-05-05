package com.squid.core.jdbc.vendor.greenplum;

import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.jdbc.vendor.greenplum.postgresql.render.PostgresJSONOperatorRenderer;
import com.squid.core.sql.db.render.OperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class GreenplumJSONOperatorRenderer extends PostgresJSONOperatorRenderer
implements
OperatorRenderer {

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args) throws RenderingException {

		if (opDef.getName().equals("JSON_ARRAY_LENGTH")) {
			String txt = "ARRAY_LENGTH("+ args[0] +", 1)";
			return txt;
		} else {
			return super.prettyPrint(skin, piece, opDef, args);
		}
	}

}
