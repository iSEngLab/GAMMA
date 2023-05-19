package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Checker;
import fixer.Tree;

public class ClassCastChecker extends FixPattern{
    public List<String> generatePatch(Tree tree,String code) {

        Tree castExp=findCastExpression(tree);
		String varName = castExp.getLabel();
		

		Tree castingType = castExp.getChildren().get(0);
		int castTypeStartPos = castingType.getPos();
		int castTypeEndPos = castTypeStartPos + castingType.getLen();
		String castTypeStr=code.substring(castTypeStartPos,castTypeEndPos);
		

		Tree castedExp = castExp.getChildren().get(1);
		int castedExpStartPos = castedExp.getPos();
		int castedExpEndPos = castedExpStartPos + castedExp.getLen();
		String castedExpStr = code.substring(castedExpStartPos, castedExpEndPos);
		int castedExpType = castedExp.getType();
		

		String fixedCodeStr1 = "";
		if (Checker.isSimpleName(castedExpType) || Checker.isFieldAccess(castedExpType) 
				|| Checker.isQualifiedName(castedExpType) || Checker.isSuperFieldAccess(castedExpType)) {
			// BC_UNCONFIRMED_CAST, PAR
			fixedCodeStr1 = "if (" + castedExpStr + " instanceof " + castTypeStr + ") { ";
		} else if (Checker.isComplexExpression(castedExpType)) {
			// PAR
			fixedCodeStr1 = "Object _tempVar = " + castedExpStr + "; " +
					"if (_temVar instanceof " + castTypeStr + ") { ";
			String fixedCodeStr2 = " } else { throw new IllegalArgumentException(\"Illegal argument: " + castedExpStr + "\"); } ";
//			generatePatch(suspCodeStartPos, endPosition, fixedCodeStr1, fixedCodeStr2);
			
			// BC_UNCONFIRMED_CAST_OF_RETURN_VALUE
			fixedCodeStr1 = "Object _tempVar = " + castedExpStr + "; " +
							"if (_temVar != null && _temVar instanceof " + castTypeStr + ") { ";
//			 this.getSuspiciousCodeStr().replace(castedExpStr, "_temVar");
		}
		
	
		String fixedCodeStr2 = " } else { throw new IllegalArgumentException(\"Illegal argument: " + castedExpStr + "\"); } ";
		System.out.println(fixedCodeStr1+code+fixedCodeStr2);
		List<String> res=new ArrayList();
		res.add(fixedCodeStr1+code+fixedCodeStr2);
		return res;
    	 
     }
     
     private Tree findCastExpression(Tree ast) {
    	 if(Checker.isCastExpression(ast.getType())) {
    		 return ast;
    	 }
    	 for(Tree child:ast.getChildren()) {
    		 Tree res=findCastExpression(child);
    		 if(res!=null) {
    			 return res;
    		 }
    	 }
    	 return null;
     }
}
