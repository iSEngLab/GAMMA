package fixer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import fixPatterns.*;

public class Fixer {
    public List<String> fix(String code) {

    	if(code.equals("}")) {
    		FixPattern fp=new StatementInserter();
    		return fp.generatePatch(null, code);
    	}
    	

	    ASTParser parser = ASTParser.newParser(AST.JLS8);
	    parser.setSource(code.toCharArray());
	    parser.setKind(ASTParser.K_STATEMENTS);
//	    parser.setKind(ASTParser.K_EXPRESSION);
	    
	    DemoVisitor visitor=new DemoVisitor();
	    parser.createAST(null).accept(visitor);
	    

	    List<Integer> typeList=getAllNodeTypes(visitor.getTree());
		List<Integer> distinctTypes = new ArrayList();
		for (Integer type : typeList) {
			if (!distinctTypes.contains(type) && !Checker.isBlock(type)) {
				distinctTypes.add(type);
			}
		}
		
//		matchPattern(visitor.getTree(),distinctTypes,code);
	    
//	    System.out.println(visitor.types);
//	    matchPattern(visitor.getTree());
		
		List<String> outputs=matchPattern(visitor.getTree(),distinctTypes,code);
//		outputs.add("");
	    
	    return outputs;
    }
    
	private List<String> matchPattern(Tree tree, List<Integer> types,String code) {
		FixPattern fp=null;
		List<String> res=new ArrayList();
		
//		if(code.equals("}")) {
//			fp=new StatementInserter();
//			fp.generatePatch(tree, code);
//		}
		
		if(!Checker.isMethodDeclaration(tree.type)) {
			boolean nullChecked = false;
			boolean typeChanged = false;
			boolean methodChanged = false;
			boolean operator = false;
			
			for(Integer type:types) {
				if(Checker.isCastExpression(type)) {
					if(!typeChanged) {
//					    System.out.println("ClassCastChecker");
					    fp=new ClassCastChecker();
					    res.addAll(fp.generatePatch(tree,code));
					    typeChanged=true;
					}
				}
				else if(Checker.isIfStatement(type) || Checker.isDoStatement(type) || Checker.isWhileStatement(type)) {
					if (Checker.isInfixExpression(tree.getChildren().get(0).getType()) 
							&& !operator) {
						operator = true;
//						System.out.println("OperatorMutator");
						fp = new OperatorMutator();
						res.addAll(fp.generatePatch(tree, code));
					}
					fp=new ConditionalExpressionMutator(2);
					fp.generatePatch(tree, code);
				}
				else if(Checker.isAssignment(type)) {
					System.out.println("OperatorMutator");
					fp = new OperatorMutator();
					res.addAll(fp.generatePatch(tree, code));
				}
				
				else if (Checker.isConditionalExpression(type)) {
					System.out.println("ConditionalExpressionMutator");
					fp = new ConditionalExpressionMutator(0);
					res.addAll(fp.generatePatch(tree, code));
				}
				else if (Checker.isCatchClause(type) || Checker.isVariableDeclarationStatement(type)) {
					if (!typeChanged) {
						System.out.println("DataTypeReplacer");
						fp = new DataTypeReplacer();
						res.addAll(fp.generatePatch(tree, code));
						typeChanged = true;
					}
			    }
				else if (Checker.isBooleanLiteral(type) || Checker.isNumberLiteral(type) || Checker.isCharacterLiteral(type)|| Checker.isStringLiteral(type) ||Checker.isNullLiteral(type)) {
					System.out.println("LiteralExpressionMutator");
					fp = new LiteralExpressionMutator();
					res.addAll(fp.generatePatch(tree, code));
				}
				else if (Checker.isMethodInvocation(type) || Checker.isConstructorInvocation(type) || Checker.isSuperConstructorInvocation(type)
						|| Checker.isClassInstanceCreation(type)) {
					if (!methodChanged) {
						System.out.println("MethodInvocationMutator");
						fp = new MethodInvocationMutator();
						res.addAll(fp.generatePatch(tree, code));
						methodChanged = true;
					}
				}
				else if(Checker.isInfixExpression(type)) {
					System.out.println("ConditionalExpressionMutator");
					fp=new ConditionalExpressionMutator(1);
					res.addAll(fp.generatePatch(tree, code));
				}
				else if(Checker.isArrayAccess(type)) {
					System.out.println("RangeChecker");
					fp=new RangeChecker();
					res.addAll(fp.generatePatch(tree, code));
				}
				else if(Checker.isReturnStatement(type)) {
					System.out.println("ReturnStatementMutator");
					fp=new ReturnStatementMutator();
					res.addAll(fp.generatePatch(tree, code));
				}
				else if(Checker.isSimpleName(type) || Checker.isQualifiedName(type)) {
					System.out.println("VariableReplacer");
					fp=new VariableReplacer();
					res.addAll(fp.generatePatch(tree, code));
				}
			}
			if(!nullChecked) {
				System.out.println("NullPointerChecker");
				fp=new NullPointerChecker();
				res.addAll(fp.generatePatch(tree, code));
			}
		}
		
		if(!code.contains("\n")) {
			System.out.println("StatementInserter");
			fp=new StatementInserter();
			res.addAll(fp.generatePatch(tree, code));
		}
		
//		System.out.println("StatementRemover");
//		fp=new StatementRemover();
//		res.addAll(fp.generatePatch(tree, code));
		
		System.out.println("StatementMover");
		fp=new StatementMover();
		res.addAll(fp.generatePatch(tree, code));
		
//		List<Integer> checkedTypes=new ArrayList();
		return res;

	}
	
	private List<Integer> getAllNodeTypes(Tree tree){
		List<Integer> nodeTypes = new ArrayList();
		nodeTypes.add(tree.getType());
		List<Tree> children = tree.getChildren();
		for (Tree child : children) {
			int childType = child.getType();
			if (Checker.isFieldDeclaration(childType) || 
					Checker.isMethodDeclaration(childType) ||
					Checker.isTypeDeclaration(childType) ||
					Checker.isStatement(childType)) break;
			nodeTypes.addAll(getAllNodeTypes(child));
		}
		return nodeTypes;
	}

	
}
