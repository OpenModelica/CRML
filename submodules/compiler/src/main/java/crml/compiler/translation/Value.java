package crml.compiler.translation;

import java.util.ArrayList;
import java.util.List;

import crml.language.grammar.crmlParser;
import crml.language.grammar.crmlParser.AliasContext;

public class Value {
	public Value(List<Value> contents, String type) {
		this.contents_set=contents;
		this.type=type;
		this.isSet = false;
		this.setPath=null;
		this.aliasInfo = null;
	}

	public Value(String contents, String type) {
		this.contents=contents;
		this.type=type;
		this.isSet = false;
		this.setPath=null;
		this.aliasInfo = null;
	}
	
	public Value(List<Value> contents, String type, Boolean isSet, List<String> setPath) {
		this.contents_set=contents;
		this.type=type;
		this.isSet=isSet;
		this.setPath=setPath;
		this.aliasInfo = null;
	}

	public Value(String contents, String type, Boolean isSet, List<String> setPath) {
		this.contents=contents;
		this.type=type;
		this.isSet=isSet;
		this.setPath=setPath;
		this.aliasInfo = null;
	}

	public Value(String contents, String type, Boolean isSet, List<String> setPath, crmlParser.AliasContext aliasCtx) {
		this.contents=contents;
		this.type=type;
		this.isSet=isSet;
		this.setPath=setPath;
		this.aliasInfo = new AliasInfo(aliasCtx);
	}
	
	public Value(List<Value> contents, String type,  Boolean isSet) {
		this.contents_set=contents;
		this.type=type;
		this.isSet = isSet;
		this.setPath=new ArrayList<String>();
		this.aliasInfo = null;
	}

	public Value(String contents, String type,  Boolean isSet) {
		this.contents=contents;
		this.type=type;
		this.isSet = isSet;
		this.setPath=new ArrayList<String>();
		this.aliasInfo = null;
	}

	public Value(String contents, String type, crmlParser.AliasContext aliasCtx) {
		this.contents=contents;
		this.type=type;
		this.isSet=false;
		this.setPath=null;
		this.aliasInfo = new AliasInfo(aliasCtx);
	}

	public String toModelica(){
		if(!isSet | contents_set == null) return contents;

		String ret = "{";
		String separator = "";
		for(Value v : contents_set){
			ret += separator + v.toModelica();
			separator = ", ";
		}
		ret +="}";

		return ret;
	}

	public List<Value> getSetValue(){
		return contents_set;
	}

	public String getValue(){
		return contents;
	}

	public void setValue(String value) {
		this.contents = value;
	}

	public void setAliasInfo(crmlParser.AliasContext aliasCtx) {
		this.aliasInfo = new AliasInfo(aliasCtx);
	}
	
	private String contents; // single value
	private List<Value> contents_set; // parsed contents for a set
	String type; // computed type
	Boolean isSet;
	List<String> setPath;
	AliasInfo aliasInfo;
}

class AliasInfo {
	String alias;
	Range range;
	AliasContext context;

	public AliasInfo(crmlParser.AliasContext ctx) {
		if (ctx == null) {
			this.alias = null;
			this.range = null;
			this.context = null;
		}
		else {
			this.alias = ctx.getText();
			this.range = new Range(ctx.getStart().getCharPositionInLine(),
				ctx.getStop().getCharPositionInLine() + ctx.getStop().getText().length());
			this.context = ctx;
		}
	}
}