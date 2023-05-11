package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class ReturnStatementMutator extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
//		Tree returnNode=findReturnNode(tree);
		List<String> res=new ArrayList();
//		for (int i=1;i<=10;i++) {
//			String fixed="return";
//			for(int j=1;j<=i;j++) {
//				fixed+="<mask>";
//			}
//			fixed+=";";
//			System.out.println(fixed);
//			res.add(fixed);
//		}
		res.add("return <mask0>;");
		
		//后面加一串东西
//		for(int i=1;i<=10;i++) {
//			String temp=code;
//			for(int j=1;j<=i;j++) {
//				code+="<mask>";
//			}
//			res.add(code);
//		}
		return res;
	}
	
	private Tree findReturnNode(Tree tree) {
		if(Checker.isReturnStatement(tree.getType())) {
			return tree;
		}
		for(Tree child:tree.getChildren()) {
			Tree res=findReturnNode(child);
			if(res!=null) {
				return res;
			}
		}
		return null;
	}
}
