package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class StatementRemover extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		List<String> res=new ArrayList();
		res.add("");
		

		Tree ifStatement=findIfStatement(tree);
		if(ifStatement!=null) {
			Tree toBeChanged=tree.get(0);
			String temp=code.substring(0,toBeChanged.getPos())+"true"+code.substring(toBeChanged.getPos()+
					toBeChanged.getLen());
			System.out.println(temp);
			res.add(temp);
		}
		return res;
	}
	
	private Tree findIfStatement(Tree tree) {
		if(Checker.isIfStatement(tree.getType())) {
			return tree;
		}
		for(Tree child:tree.getChildren()) {
			Tree temp=findIfStatement(child);
			if(temp!=null) {
				return temp;
			}
		}
		return null;
	}

}
