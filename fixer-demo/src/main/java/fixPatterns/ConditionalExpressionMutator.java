package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Tree;
import fixer.Checker;

public class ConditionalExpressionMutator extends FixPattern{
    private int type=0;
    
    public ConditionalExpressionMutator(int type) {
    	this.type=type;
    }
    
	public List<String> generatePatch(Tree tree,String code) {
		List<String> res=new ArrayList();
		List<String> removed=new ExpressionRemover().generatePatch(tree, code);
		if(removed!=null) {
		res.addAll(new ExpressionRemover().generatePatch(tree, code));
		}
		res.addAll(new ExpressionAdder().generatePatch(tree, code));
		return res;
	}
	
//	private List<Tree> getAllSuspNodes(Tree tree){
//		List<Tree> res=new ArrayList();
//		Tree suspExpTree;
//		if (Checker.isDoStatement(tree.getType())) {
//			List<Tree> children = tree.getChildren();
//			suspExpTree = children.get(children.size() - 1);
//		}else if (Checker.withBlockStatement(tree.getType())) {
//			suspExpTree = tree.getChildren().get(0);
//		}else {
//			suspExpTree = tree;
//		}
//		
//		int suspExpTreeType = suspExpTree.getType();
//		if (!Checker.isInfixExpression(suspExpTreeType)) {
//			if (Checker.isStatement(suspExpTreeType)) {
//				predicateExps.putAll(readConditionalExpressions(suspExpTree));
//			} else {
//				predicateExps.put(suspExpTree, 0);
//			}
//			return predicateExps;
//		}
//		
//		return null;
//	}
	
	public Tree getConditionalNode(Tree tree) {
		if(Checker.isIfStatement(tree.getType())) {
			return tree;
		}
		for(Tree child:tree.getChildren()) {
			Tree res=getConditionalNode(child);
			if(res!=null) {
				return res;
			}
		}
		return null;
	}
	
	public List<Tree> getInfixExpressionNode(Tree tree) {
		List<Tree> res=new ArrayList();
		
		if(Checker.isInfixExpression(tree.getType())) {
			res.add(tree);
		}
		for(Tree child:tree.getChildren()) {
//			Tree res=getInfixExpressionNode(child);
//			if(res!=null) {
//				return res;
//			}
			res.addAll(getInfixExpressionNode(child));
			
		}
		return res;
	}
	
	
	public class ExpressionRemover{
		public List<String> generatePatch(Tree tree, String code) {
			List<Tree> targetExpressions=getInfixExpressionNode(tree);
			if(targetExpressions.size()==0) {
				return null;
			}
			List<String> res=new ArrayList();
			for(Tree infixExpression:targetExpressions) {
			if(infixExpression.getChildren().size()!=3) {
				continue;
			}
			Tree left=infixExpression.get(0);
			Tree right=infixExpression.get(2);
			Tree operator=infixExpression.get(1);
			String res1=code.substring(0,left.getPos()+left.getLen())+code.substring(right.getPos()+right.getLen());
			System.out.println(res1);
			String res2=code.substring(0,left.getPos())+code.substring(right.getPos());
			System.out.println(res2);
			res.add(res1);
			res.add(res2);
			}
			return res;
		}
	}
	
	public class ExpressionAdder{
		public List<String> generatePatch(Tree tree,String code){
			List<Tree> targetExps=getInfixExpressionNode(tree);
			Tree conditionalNode=getConditionalNode(tree);
			if(conditionalNode!=null) {
			targetExps.add(getConditionalNode(tree).get(0));
			}
			if(targetExps.size()==0) {
				return null;
				//targetExps.add(tree.get(0));
			}
			List<String> res=new ArrayList();
			
			for(Tree targetExp:targetExps) {
//			if(targetExp==null) {
//				targetExp=tree.get(0);
//			}

			String initialCode=code.substring(0,targetExp.getPos()+targetExp.getLen());
//			for(int i=1;i<=10;i++) {
//				String fixedCode=initialCode;
//				for(int j=1;j<=i;j++) {
//					fixedCode+="<mask>";
//				}
//				fixedCode+=code.substring(targetExp.getPos()+targetExp.getLen());
//				res.add(fixedCode);
//				System.out.println(fixedCode);
//			}
			
			initialCode+="<mask0>";
			initialCode+=code.substring(targetExp.getPos()+targetExp.getLen());
			res.add(initialCode);
			}
			return res;
		}
	}
	
}
