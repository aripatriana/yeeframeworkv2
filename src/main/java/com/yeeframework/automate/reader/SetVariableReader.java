package com.yeeframework.automate.reader;

import com.yeeframework.automate.entry.QueryEntry;
import com.yeeframework.automate.entry.SetVarEntry;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.util.DataTypeUtils;
import com.yeeframework.automate.util.StringUtils;

public class SetVariableReader {
	
	private String variable;
	
	public SetVariableReader(String variable) {
		this.variable = variable;
	}
	
	public SetVarEntry read() throws ScriptInvalidException {
		SetVarEntry ae = new SetVarEntry();
		ae.setScript(variable);
		if (variable.trim().endsWith(";"))
			throw new ScriptInvalidException("Invalid character, semicolon not allowed in an arguments " + variable);
		
		if (!variable.contains("=")) 
			throw new ScriptInvalidException("Missing equation (=) in an arguments " + variable);
		
		variable = StringUtils.replaceCharForward(variable, '=', "#");
		String[] var = variable.split("#");
		if (!var[0].startsWith("@"))
			var[0] = "@system." + var[0];

		if (!var[0].startsWith("@system.")) 
			throw new ScriptInvalidException("Set variable only support for @session. in an arguments " + variable);

		ae.setVariable(var[0]);
		if (DataTypeUtils.checkType(var[1], DataTypeUtils.TYPE_OF_ARGUMENT) || DataTypeUtils.isPrimitiveDataTypes(var[1])) {
			ae.setValue(var[1]);
		} else if (var[1].toLowerCase().trim().startsWith("select")){
			QueryReader qr = new QueryReader(var[1]);
			QueryEntry qe = qr.read();
			
			if (qe.getColumns().size() == 0 || qe.getColumns().size() > 1)
				throw new ScriptInvalidException("Invalid select query in an arguments " + variable + " must be a single column result"); 
			ae.setQuery(qe);
		}
		

		return ae;
	}
	

}
