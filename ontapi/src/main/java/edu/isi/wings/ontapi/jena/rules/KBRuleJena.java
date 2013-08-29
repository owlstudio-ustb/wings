package edu.isi.wings.ontapi.jena.rules;

import java.util.ArrayList;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import edu.isi.wings.ontapi.KBObject;
import edu.isi.wings.ontapi.rules.KBRule;
import edu.isi.wings.ontapi.rules.KBRuleClause;
import edu.isi.wings.ontapi.rules.KBRuleFunctor;
import edu.isi.wings.ontapi.rules.KBRuleObject;
import edu.isi.wings.ontapi.rules.KBRuleTriple;

public class KBRuleJena implements KBRule {
	transient Rule rule;
	
	String name;
	ArrayList<KBRuleClause> body;
	ArrayList<KBRuleClause> head;

	public KBRuleJena(Rule rule) {
		this.name = rule.getName();
		this.rule = rule;
		this.body = new ArrayList<KBRuleClause>();
		this.head = new ArrayList<KBRuleClause>();
		this.initialize();
	}
	
	public KBRuleJena(String ruleText) {
		this(Rule.parseRule(ruleText));
	}
	
	private void initialize() {
		for (ClauseEntry ce : this.rule.getBody()) {
			body.add(new KBRuleClauseJena(ce));
		}
		for (ClauseEntry ce : rule.getHead()) {
			head.add(new KBRuleClauseJena(ce));
		}
	}

	@Override
	public ArrayList<KBRuleClause> getRuleBody() {
		return this.body;
	}

	@Override
	public ArrayList<KBRuleClause> getRuleHead() {
		return this.head;
	}

	@Override
	public void setRuleBody(ArrayList<KBRuleClause> body) {
		this.body = body;
	}

	@Override
	public void setRuleHead(ArrayList<KBRuleClause> head) {
		this.head = head;
	}

	@Override
	public Object getInternalRuleObject() {
		return this.rule;
	}
	
	@Override
	public void resetInternalRuleObject() {
		ArrayList<ClauseEntry> head = new ArrayList<ClauseEntry>();
		ArrayList<ClauseEntry> body = new ArrayList<ClauseEntry>();
		for(KBRuleClause kbclause : this.head) {
			head.add(this.getClause(kbclause));
		}
		for(KBRuleClause kbclause : this.body) {
			body.add(this.getClause(kbclause));
		}
		this.rule = new Rule(this.name, head, body);
	}
	
	private ClauseEntry getClause(KBRuleClause kbclause) {
		if(kbclause.isFunctor()) {
			KBRuleFunctor kbf = kbclause.getFunctor();
			ArrayList<Node> args = new ArrayList<Node>();
			for(KBRuleObject kbarg : kbf.getArguments()) {
				args.add(this.getKBNode(kbarg));
			}
			return new Functor(kbf.getName(), args);
		}
		else if(kbclause.isTriple()) {
			KBRuleTriple kbtriple = kbclause.getTriple();
			Node subject = this.getKBNode(kbtriple.getSubject());
			Node predicate = this.getKBNode(kbtriple.getPredicate());
			Node object = this.getKBNode(kbtriple.getObject());
			if(subject != null && predicate != null && object != null)
				return new TriplePattern(subject, predicate, object);
		}
		return null;
	}
	
	private Node getKBNode(KBRuleObject kbobj) {
		if(kbobj.isVariable()) {
			return NodeFactory.createVariable(kbobj.getVariableName());
		}
		else {
			KBObject argobj = kbobj.getKBObject();
			if(argobj.isLiteral()) {
				RDFDatatype type = NodeFactory.getType(argobj.getDataType());
				return NodeFactory.createLiteral(argobj.getValue().toString(), type);
			}
			else
				return NodeFactory.createURI(argobj.getID());
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
