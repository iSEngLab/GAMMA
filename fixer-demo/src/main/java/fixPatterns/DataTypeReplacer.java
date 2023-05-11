package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class DataTypeReplacer extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		Tree typeNode = findPrimitiveType(tree);
		
//		int i=0;
//		while(tree.getChildren(i))
		if(typeNode==null) {
			return null;
		}
		
		String res=code.substring(0,typeNode.getPos())+"<mask0> "+code.substring(typeNode.getPos()+typeNode.getLen());
		System.out.println(res);
		
		List<String> result=new ArrayList();
		result.add(res);
		return result;
	}
	
	private Tree findPrimitiveType(Tree tree) {
		if(Checker.isPrimitiveType(tree.getType()) || Checker.isSimpleType(tree.getType()) || Checker.isQualifiedType(tree.getType())) {
			return tree;
		}
		for(Tree child:tree.getChildren()) {
			Tree res=findPrimitiveType(child);
			if(res!=null) {
				return res;
			}
		}
		return null;
	}
}
