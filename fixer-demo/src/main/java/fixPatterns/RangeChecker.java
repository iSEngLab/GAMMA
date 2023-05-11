package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class RangeChecker extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		return new ArrayRangeChecker().generatePatch(tree, code);
	}
	
	private class ArrayRangeChecker{
		protected List<String> generatePatch(Tree tree, String code){
			List<Tree> AllSuspArrays=identifyArray(tree);
			List<String> res=new ArrayList();
			for(Tree arrayNode:AllSuspArrays) {
				String arrayName=arrayNode.get(0).getLabel();
				String arrayIndex=arrayNode.get(1).getLabel();
				String fixed="if ("+arrayIndex+"<"+arrayName+".length){ ";
				fixed+=code.substring(arrayNode.getPos(),arrayNode.getPos()+arrayNode.getLen());
				fixed+="}";
				System.out.println(fixed);
				res.add(fixed);
			}
			return res;
		}
		
		private List<Tree> identifyArray(Tree ast){
			List<Tree> res=new ArrayList();
			if(Checker.isArrayAccess(ast.getType())) {
				res.add(ast);
			}
			List<Tree> children=ast.getChildren();
			for (Tree child : children) {
				int type = child.getType();
				if (Checker.isStatement(type)) break;
				else if (Checker.isArrayAccess(type)) {
					Tree arrayExp = child.get(0);
					Tree indexExp = child.get(1);
					if (Checker.isComplexExpression(arrayExp.getType())) {
						res.addAll(identifyArray(arrayExp));
					}
					res.add(child);
				} else if (Checker.isComplexExpression(type)) {
					res.addAll(identifyArray(child));
				} else if (Checker.isSimpleName(type) && child.getLabel().startsWith("MethodName:")) {
					res.addAll(identifyArray(child));
				}
			}
			return res;
		}
	}
}
