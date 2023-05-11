package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class LiteralExpressionMutator extends FixPattern{
	private List<Tree> suspCons=new ArrayList();
	
	public List<String> generatePatch(Tree tree,String code) {
		identifyPotentialBuggyExpressions(tree);
		List<String> res=new ArrayList();
		for (Tree suspCon : suspCons) {
//			for(int i=1;i<=10;i++) {
//				String temp=code.substring(0,suspCon.getPos());
//				for(int j=1;j<=i;j++) {
//					temp+="<mask>";
//				}
//				temp+=code.substring(suspCon.getPos()+suspCon.getLen());
//				System.out.println(temp);
//				res.add(temp);
//			}
			
			res.add(code.substring(0,suspCon.getPos())+"<mask0>"+code.substring(suspCon.getPos()+suspCon.getLen()));
			
//            String fixed=code.substring(0,suspCon.getPos())+"<mask>"+code.substring(suspCon.getPos()+suspCon.getLen());
//            System.out.println(fixed);
//            res.add(fixed);
		}
		return res;
	}
	
	private void identifyPotentialBuggyExpressions(Tree suspCodeTree) {
		List<Tree> children = suspCodeTree.getChildren();
		for (Tree child : children) {
			int childType = child.getType();
			if (Checker.isSimpleName(childType) && child.getLabel().startsWith("MethodName:")) {
				identifyPotentialBuggyExpressions(child);
			} else if (Checker.isStringLiteral(childType) || Checker.isBooleanLiteral(childType) || Checker.isCharacterLiteral(childType) ||Checker.isNullLiteral(childType)) {
				suspCons.add(child);
			} else if (Checker.isNumberLiteral(childType)) {
				if (Checker.isMethodInvocation(suspCodeTree.getType())) continue;
				if (Checker.isArrayAccess(suspCodeTree.getType())) continue;
				suspCons.add(child);
			} else if (Checker.isComplexExpression(childType)) {
				identifyPotentialBuggyExpressions(child);
			} else if (Checker.isStatement(childType)) break;
		}
	}

}
