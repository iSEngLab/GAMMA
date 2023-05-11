package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class NullPointerChecker extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		List<Tree> suspVars=getSuspVar(tree);
		if(suspVars==null) {
			return null;
		}
		
		List<String> res=new ArrayList();
		
		for(Tree suspVar:suspVars) {
			if(suspVar.getLabel().startsWith("Name:")){
				res.addAll(checkNull(code,suspVar.getLabel().substring(5)));

		        
		    }
			else if(suspVar.getLabel().startsWith("MethodName:")) {
				continue;
			}
			else {
				res.addAll(checkNull(code,suspVar.getLabel()));
			}
			
			
		}
		System.out.println(res);
		
		return res;
	}
	
	private List<String> checkNull(String code,String varName){
		List<String> res=new ArrayList();
		res.add("if("+varName+"!=null){ "+code+" }");
		
    	res.add("if("+varName+"==null){return;} "+code);
//        for(int i=1;i<=10;i++) {
//        	String temp="if("+varName+"==null){\n"+"return ";
//        	for(int j=1;j<=i;j++) {
//        		temp+="<mask>";
//        	}
//        	temp+=";\n}\n";
//        	temp+=code;
//        	System.out.println(temp);
//        	res.add(temp);
//        }
    	res.add("if("+varName+"==null){return <mask0>;} "+code);
        
        res.add("if("+varName+"==null) continue; "+code);
       
        res.add("if("+varName+"==null){ throw new IllegalArgumentException();}"+code);
        
//        res.add("if("+varName+"==null){\n"+varName+"=<mask>;\n}");
        
        res.add("if("+varName+"==null){ "+code+" }");
        
//        for(int i=1;i<=10;i++) {
//        	String temp="if("+varName+"==null){\n"+varName+"=";
//        	for(int j=1;j<=i;j++) {
//        		temp+="<mask>";
//        	}
//        	temp+=";\n}\n";
//        	temp+=code;
//        	System.out.println(temp);
//        	res.add(temp);
//        }
        res.add("if("+varName+"==null){ "+varName+"=<mask0>; } "+code);
        
        
        return res;
	}
	
	private List<Tree> getSuspVar(Tree ast) {
		List<Tree> res=new ArrayList();
   	 if(ast.getLabel().startsWith("Name:") || Checker.isSimpleName(ast.getType())
   			 || Checker.isArrayAccess(ast.getType())) {
		 res.add(ast);
	 }
	 for(Tree child:ast.getChildren()) {
//		 Tree res=getSuspVar(child);
//		 if(res!=null) {
//			 return res;
//		 }
		 res.addAll(getSuspVar(child));
	 }
	 return res;
	}

}
