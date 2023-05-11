package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class MethodInvocationMutator extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		List<String> res=new ArrayList();
		
		MethodNameMutator mnm=new MethodNameMutator();
		res.addAll(mnm.generatePatch(tree, code));
		
		ArgumentMutator am=new ArgumentMutator();
		res.addAll(am.generatePatch(tree, code));
		return res;
	}
	
	public List<Tree> findMethodInvocationNode(Tree tree) {
		List<Tree> res=new ArrayList();
		
		if(tree.getLabel().startsWith("MethodName") || Checker.isConstructorInvocation(tree.getType())
				|| Checker.isSuperConstructorInvocation(tree.getType())
				|| Checker.isClassInstanceCreation(tree.getType())) {
			res.add(tree);
		}
   	 for(Tree child:tree.getChildren()) {
		 res.addAll(findMethodInvocationNode(child));
	 }
	 return res;
	}
	
	
	private class MethodNameMutator{
		public List<String> generatePatch(Tree tree,String code) {
			List<Tree> methodInvocationNodes=findMethodInvocationNode(tree);
			List<String> result=new ArrayList();
			for(Tree methodInvocationNode:methodInvocationNodes) {
			if(Checker.isConstructorInvocation(methodInvocationNode.getType()) || 
					Checker.isSuperConstructorInvocation(methodInvocationNode.getType())
					|| Checker.isClassInstanceCreation(methodInvocationNode.getType())) {
				continue;
			}
			
			//改方法名
			int methodNameLen=methodInvocationNode.getLabel().split(":")[1].length();
			int startPos=methodInvocationNode.getPos();
//			for(int i=1;i<=10;i++) {
//				String res=code.substring(0,startPos);
//				for(int j=1;j<=i;j++) {
//					res+="<mask>";
//				}
//				res+=code.substring(startPos+methodNameLen);
//				System.out.println(res);
//				result.add(res);
//			}
			result.add(code.substring(0,startPos)+"<mask0>"+code.substring(startPos+methodNameLen));
			}
			
			return result;
		}
	}
	
	private class ArgumentMutator{
		public List<String> generatePatch(Tree tree,String code){
			List<String> res=new ArrayList();
			List<Tree> methodInvocations=findMethodInvocationNode(tree);
			
			for(Tree methodInvocation:methodInvocations) {
				if(Checker.isClassInstanceCreation(methodInvocation.getType())) {
					methodInvocation=methodInvocation.get(0);
				}
				
			for(Tree child:methodInvocation.getChildren()) {
				int startPos=child.getPos();
				int endPos=child.getPos()+child.getLen();
//				for(int i=1;i<=10;i++) {
//					String toFix=code.substring(0,startPos);
//					for(int j=1;j<=i;j++) {
//						toFix+="<mask>";
//					}
//					toFix+=code.substring(endPos);
//					res.add(toFix);
//				}
				res.add(code.substring(0,startPos)+"<mask0>"+code.substring(endPos));
//				System.out.println(res);
			}

			//改两个参数 直接排列组合
		    List<Tree> allChildren=methodInvocation.getChildren();
//		    for(int i=0;i<allChildren.size()-1;i++) {
//		    	for(int j=i+1;j<allChildren.size();j++) {
//		    		int firstPos=allChildren.get(i).getPos();
//		    		int firstLen=allChildren.get(i).getLen();
//		    		int secondPos=allChildren.get(j).getPos();
//		    		int secondLen=allChildren.get(j).getLen();
//		    		String temp=code.substring(0,firstPos)+"<mask><mask>"
//		    				+code.substring(firstPos+firstLen,secondPos)
//		    				+"<mask><mask>"
//		    				+code.substring(secondPos+secondLen);
//		    		System.out.println(temp);
//		    		res.add(temp);
//		    	}
//		    }

		    
		    //减少参数
//		    if(allChildren.size()==1) {
//		    	Tree child=allChildren.get(0);
//		    	String temp=code.substring(0,child.getPos())+code.substring(child.getPos()+child.getLen());
//		    	System.out.println(temp);
//		    	res.add(temp);
//		    }
//		    else {
			    for(int i=0;i<allChildren.size();i++) {
			    	Tree child=allChildren.get(i);
			    	String temp;
//			    	if(i!=allChildren.size()-1) {
			    	    temp=code.substring(0,child.getPos())+code.substring(child.getPos()+child.getLen()+1);
//			    	}
//			    	else {
//			    		temp=code.substring(0,child.getPos()-1)+code.substring(child.getPos()+child.getLen());
//			    	}
			    	System.out.println(temp);
			    	res.add(temp);
			    }
//		    }
			    
			//增加参数
            //在最后加参数
			if(allChildren.size()!=0) {
//				for(int i=1;i<=10;i++) {
//					Tree lastArg=allChildren.get(allChildren.size()-1);
//					String temp=code.substring(0,lastArg.getPos()+lastArg.getLen())+",";
//					for(int j=1;j<=i;j++) {
//						temp+="<mask>";
//					}
//					temp+=code.substring(lastArg.getPos()+lastArg.getLen());
//					System.out.println(temp);
//					res.add(temp);
//				}
				Tree lastArg=allChildren.get(allChildren.size()-1);
				res.add(code.substring(0,lastArg.getPos()+lastArg.getLen())+",<mask0>"+code.substring(lastArg.getPos()+lastArg.getLen()));
				
				//在最前面加参数
//				for(int i=1;i<=10;i++) {
//					Tree firstArg=allChildren.get(0);
//					String temp=code.substring(0,firstArg.getPos());
//					for(int j=1;j<=i;j++) {
//						temp+="<mask>";
//					}
//					temp+=",";
//					temp+=code.substring(firstArg.getPos());
//					System.out.println(temp);
//					res.add(temp);
//				}
				Tree firstArg=allChildren.get(0);
				res.add(code.substring(0,firstArg.getPos())+"<mask0>,"+code.substring(firstArg.getPos()));
			}
			else {
				//无参数的函数，加参数
//				for(int i=1;i<=10;i++) {
//					String temp=code.substring(methodInvocation.getPos()+methodInvocation.getLen()-1);
//					for(int j=1;j<=i;j++) {
//						temp+="<mask>";
//					}
//					temp+=code.substring(methodInvocation.getPos()+methodInvocation.getLen()-1);
//				}
				res.add(code.substring(methodInvocation.getPos()+methodInvocation.getLen()-1)+"<mask0>"+
                code.substring(methodInvocation.getPos()+methodInvocation.getLen()-1));

			}
			}

			
			
			return res;
		}
	}
}
