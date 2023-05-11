package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Tree;
import fixer.Checker;

public class VariableReplacer extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		int type=tree.getType();
		if (Checker.isForStatement(type) || Checker.isEnhancedForStatement(type)
				|| Checker.isWhileStatement(type) || Checker.isDoStatement(type)) return null;
		List<Tree> suspVars=new ArrayList();
		getSuspiciousVariables(tree,suspVars);
		List<String> res=new ArrayList();
		for(Tree suspVar:suspVars) {
//			for(int i=1;i<=10;i++) {
//				String fixed=code.substring(0,suspVar.getPos());
//				for(int j=1;j<=i;j++) {
//					fixed+="<mask>";
//				}
//				fixed+=code.substring(suspVar.getPos()+suspVar.getLen());
//				res.add(fixed);
//				System.out.println(fixed);
//			}
			res.add(code.substring(0,suspVar.getPos())+"<mask0>"+code.substring(suspVar.getPos()+suspVar.getLen()));
//			String fixed=code.substring(0,suspVar.getPos())+"<mask>"+code.substring(suspVar.getPos()+suspVar.getLen());
//			res.add(fixed);
		}
		return res;
	}
	
	private void getSuspiciousVariables(Tree ast,List<Tree> varTrees){
		List<Tree> children=ast.getChildren();
		for(Tree child:children) {
			int childType=child.getType();
			if (Checker.isSimpleName(childType) || Checker.isQualifiedName(childType)) {
				int parentType = ast.getType();
				if ((Checker.isAssignment(parentType) || Checker.isVariableDeclarationFragment(parentType))
						&& ast.getChildPosition(child) == 0) {
					continue;
				}
				String varName = readVariableName(child);
				if (varName != null) {
					if (varTrees != null) varTrees.add(child);
//					if (!allSuspVariables.contains(varName)) allSuspVariables.add(varName);
				}
//				else getSuspiciousVariables(child, varTrees);
			}
			if(!child.getChildren().isEmpty()) {
				getSuspiciousVariables(child, varTrees);
			}
		}
	}
	
	public static String readVariableName(Tree simpleNameAst) {
		String label = simpleNameAst.getLabel();
		if (label.startsWith("MethodName:") || label.startsWith("ClassName:")) {
			return null;
		} else if (label.startsWith("Name:")) {
			label = label.substring(5);
			if (!label.contains(".")) {
				char firstChar = label.charAt(0);
				if (Character.isUpperCase(firstChar)) {
					return null;
				}
			}
		}
		return label;
	}
}
