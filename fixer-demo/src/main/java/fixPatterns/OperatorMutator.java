package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class OperatorMutator extends FixPattern{
	
	public List<String> generatePatch(Tree tree,String code) {
		//找到操作符节点
		List<Tree> operatorNodes=findOperatorNode(tree);
		List<String> result=new ArrayList();
		
		for(Tree operatorNode:operatorNodes) {
		int startPos=operatorNode.getPos();
		int endPos=startPos+operatorNode.getLen();
		String operator=code.substring(startPos,endPos);
		
		String res=code.substring(0,startPos)+"<mask0>"+code.substring(endPos);
		System.out.println(res);

		result.add(res);
		}
		
		//一次改多个操作符
//		StringBuilder sb=new StringBuilder(code);
//		int move=0;
//		for(Tree operatorNode:operatorNodes) {
//			sb.replace(operatorNode.getPos()+move,operatorNode.getPos()+move+operatorNode.getLen() , "<mask>");
//			move+=(6-operatorNode.getLen());
//		}
//		System.out.println(sb.toString());
//		result.add(sb.toString());

		
		return result;
	}
	
	private List<Tree> findOperatorNode(Tree tree) {
		List<Tree> res=new ArrayList();
		
		if(Checker.isOperator(tree.getType())) {
			res.add(tree);
		}
		for(Tree child:tree.getChildren()) {
			res.addAll(findOperatorNode(child));
		}
		return res;
	}

}
