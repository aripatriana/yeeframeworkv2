package com.yeeframework.automate.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

import com.yeeframework.automate.web.WebExchange;

public class QueryParseUtils {

	public static final String ROUND_BRACKET = "()";
	public static final String SQUARE_BRACKET = "[]";
	public static final String[] BRACKETS = new String[] {ROUND_BRACKET,SQUARE_BRACKET};
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static String[] parseArgs(String query, List<String> parameters, WebExchange webExchange) throws Exception {
		Object obj = webExchange.get("@system.REMOVE_STRING_QUOTE");
		Boolean removeQuote = false;
		if (obj != null) {
			removeQuote = new Boolean(obj.toString());
		}
		Map<String, List<String>> rounded = new HashMap<String, List<String>>();
		Map<String, List<String>> squared = new HashMap<String, List<String>>();
		for (String p : parameters) {
			if (p.contains(ROUND_BRACKET)) {
				String[] s = p.split("\\(\\)");
				if (s.length > 2)
					throw new Exception("Not valid argument");
				List<String> r = rounded.get(s[0]);
				if (r == null) r = new ArrayList<String>();
				if (s.length == 2)
					r.add(s[1].replace(".","").trim());
				rounded.put(s[0], r);
			} else if(p.contains(SQUARE_BRACKET)) {
				String[] s = p.split("\\" + SQUARE_BRACKET);
				if (s.length > 2)
					throw new Exception("Not valid argument");
				List<String> r = squared.get(s[0]);
				if (r == null) r = new ArrayList<String>();
				if (s.length == 2)
					r.add(s[1].replace(".","").trim());
				squared.put(s[0], r);
			} else {
				query = query.replace(p, DataTypeUtils.parse(webExchange.get(p)).toString());
			}
		}
		
		String[] parsedQuery = new String[] {query};
		
		// []
		if (!squared.isEmpty()) {
			for (Entry<String, List<String>> e : squared.entrySet()) {
				Object o = webExchange.get(e.getKey()+SQUARE_BRACKET);
				if (o != null && !ReflectionUtils.checkAssignableFrom(o.getClass(), List.class))
					throw new Exception("Argument value is not a list");
				
				if (o != null) {
					List<Object> l = (List<Object>) o;
					if (e.getValue() != null && !e.getValue().isEmpty()) {
						for (String v : e.getValue()) {
							List<Object> values = MapUtils.mapAsList((List<Map<String, Object>>) (List<?>)l, v);
							query = query.replace(e.getKey()+SQUARE_BRACKET + "." + v, MapUtils.listAsString(values, ","));
						}
					} else {
						query = query.replace(e.getKey()+SQUARE_BRACKET, MapUtils.listAsString(l, ","));
					}
				}
			}
			parsedQuery[0] = query;
		}
		
		// ()
		if (!rounded.isEmpty()) {
			int[] size = new int[rounded.size()];
			int i = 0;

			// get size
			for (String k : rounded.keySet()) {
				Object o = webExchange.get(k+SQUARE_BRACKET);
				if (o != null && !ReflectionUtils.checkAssignableFrom(o.getClass(), List.class))
					throw new Exception("Argument value is not a list");
				if (o != null) {
					List<Object> l = (List<Object>) o;
					size[i] = l.size();
				}
				i++;
			}
			
			// compare size
			if (i > 1) {
				int total = IntStream.of(size).sum();
				int average = total / i;
				if (average != size[0])
					throw new Exception("Size of argument is not match");
			}
			
			parsedQuery = new String[size[0]];
			
			for (int j=0; j<size[0]; j++) {
				String tempQuery = query;
				for (Entry<String, List<String>> e : rounded.entrySet()) {
					if (e.getValue() != null && !e.getValue().isEmpty()) {
						for (String s : e.getValue()) {
							if (removeQuote) {
								tempQuery = tempQuery.replace(e.getKey()+ROUND_BRACKET + "." + s, webExchange.get(e.getKey()+"[" + j + "]" + "." + s).toString());
							} else {
								tempQuery = tempQuery.replace(e.getKey()+ROUND_BRACKET + "." + s, StringUtils.quote(webExchange.get(e.getKey()+"[" + j + "]" + "." + s).toString()));
							}
						}
					} else {
						MapUtils.removeEquals(parameters, e.getKey()+ROUND_BRACKET);
						if (removeQuote) {
							tempQuery = tempQuery.replace(e.getKey()+ROUND_BRACKET, webExchange.get(e.getKey()+"[" + j + "]").toString());
						} else {
							tempQuery = tempQuery.replace(e.getKey()+ROUND_BRACKET, StringUtils.quote(webExchange.get(e.getKey()+"[" + j + "]").toString()));							
						}
					}
				}
				parsedQuery[j] = tempQuery;
			}
		}
		
		return parsedQuery;
	}
}
