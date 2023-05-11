package fixer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

public class DemoVisitor extends ASTVisitor{
	public Deque<Tree> trees=new ArrayDeque();
	public Tree root;
	
	public boolean visit(ArrayAccess node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
    	Expression arrayExpression = node.getArray();
		Expression indexExpression = node.getIndex();
		arrayExpression.accept(this);
		indexExpression.accept(this);
		return false;
	}
	
	public void endVisit(ArrayAccess node) {
		popNode();
	}
	
	public boolean visit(Assignment node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		Expression leftHandExp = node.getLeftHandSide();
		leftHandExp.accept(this);
		String op = node.getOperator().toString();
		addNode(-1,leftHandExp.getStartPosition() + leftHandExp.getLength()+1, op.length(),op);
		popNode();
		Expression rightHandExp = node.getRightHandSide();
		rightHandExp.accept(this);
		return false;
	}

	@Override
	public void endVisit(Assignment node) {
		popNode();
	}

	
	@Override
	public boolean visit(BooleanLiteral node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return false;
	}

	@Override
	public void endVisit(BooleanLiteral node) {
		popNode();
	}
	
	@Override
	public boolean visit(PackageDeclaration node) {
		System.out.println("Package:"+node.getName());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return true;
	}
	@Override
	public boolean visit(ImportDeclaration node) {
		System.out.println("Import:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return true;
	}
	public boolean visit(VariableDeclaration node) {
		System.out.println("Package:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return true;
	}
	@Override
	public boolean visit(FieldDeclaration node) {
		for(Object obj:node.fragments()) {
			VariableDeclarationFragment v = (VariableDeclarationFragment)obj;
			System.out.println("Field:\t" + v.getName());
		}
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {

    	int startPosition = 0;
    	String returnTypeStr = "";
    	Type returnType = null;
    	if (node.isConstructor()) {
    		returnTypeStr = "=CONSTRUCTOR=";
    	} else {
    		returnType = node.getReturnType2();
    		returnTypeStr = ((returnType == null) ? "void" : returnType.toString());
    	}
		List<?> modifiers = node.modifiers();
		List<?> typeParameters = node.typeParameters();
		SimpleName methodName = node.getName();
		List<?> parameters = node.parameters();
		List<?> exceptionTypes = node.thrownExceptionTypes();
		List<Modifier> realModifiers = new ArrayList();
		
		String methodLabel = "";
		for (Object obj : modifiers) {
			IExtendedModifier modifier = (IExtendedModifier) obj;
			if (modifier.isModifier()) {
				methodLabel += obj.toString() + ", ";
				realModifiers.add((Modifier) modifier);
				if (startPosition == 0) {
					startPosition = ((Modifier) modifier).getStartPosition();
				}
			}
		}
		methodLabel += "@@" + returnTypeStr + ", ";
		if (typeParameters != null && typeParameters.size() > 0) {
			methodLabel += "@@tp:";
			for (Object obj : typeParameters) {
				methodLabel += obj.toString() + ", ";
			}
		}
		methodLabel += "MethodName:" + methodName + ", ";
		if (startPosition == 0) {
			startPosition = methodName.getStartPosition();
		}
		if (parameters == null || parameters.size() == 0) {
			methodLabel += "@@Argus:null";
		} else {
			methodLabel += "@@Argus:";
			for (Object obj : parameters) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
				String arguType = svd.getType().toString();
				String arguName = svd.getName().getFullyQualifiedName();
				methodLabel += arguType + "+" + arguName + "+";
			}
		}
		if (exceptionTypes != null && exceptionTypes.size() > 0) {
			methodLabel += "@@Exp:";
			for (Object obj : exceptionTypes) {
				methodLabel += obj.toString() + "+";
			}
		}
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),methodLabel);
//		push(node.getNodeType(), node.getClass().getSimpleName(), methodLabel, startPosition, nodeStartPosition + length - startPosition);
		
        /*
		 *  The visiting of the below elements (except modifiers and body) can be removed, 
		 *  because there is no any fix pattern can be mined from these elements.
		 *  Even though some fix patterns can be mined, they are not what we want.
		 */
        visitList(realModifiers);
		if (returnType != null) {
			returnType.accept(this);
		}
//		visitList(typeParameters);
		addNode(methodName.getNodeType(),methodName.getStartPosition(),methodName.getLength(),"MethodName:" + methodName.getFullyQualifiedName());
		popNode();
		visitList(parameters);
//		visitList(exceptionTypes);

		// The body can be null when the method declaration is from a interface
		Block methodBody = node.getBody();
		if (methodBody != null) {
//			push(8, "Block", "MethodBody", methodBody.getStartPosition(), methodBody.getLength());
//			methodBody.accept(this);
			List<?> stmts = methodBody.statements();
			visitList(stmts);
//			popNode();
		}
		return false;

	}
	
	public boolean visit(MethodInvocation node) {
		Expression exp = node.getExpression();
//		List<?> typeArguments = node.typeArguments();
		SimpleName methodName = node.getName();
		List<?> arguments = node.arguments();
//		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),"MethodName:" + methodName.getFullyQualifiedName() + ":" + arguments.toString());
		if (exp == null && arguments.size() == 0) {
			addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),"MethodName:" + methodName.getFullyQualifiedName() + ":" + arguments.toString());
		}else 
		{
			addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
			List<MethodInvocation> methods = new ArrayList();
			while (exp != null) {
				if (exp instanceof MethodInvocation) {
					MethodInvocation method = (MethodInvocation) exp;
					methods.add(0, method);
					exp = method.getExpression();
				} else {
					if (exp instanceof Name) {
						addNode(exp.getNodeType(),exp.getStartPosition(),exp.getLength(),"Name:" + exp.toString());
						popNode();
					} else {
						exp.accept(this);	
					}
					exp = null;
				}
			}
			for (MethodInvocation method : methods) {
				List<?> argumentsList = method.arguments();
				addNode(method.getNodeType(),method.getStartPosition(),method.getLength(),"MethodName:" + method.getName().getFullyQualifiedName() + ":" + argumentsList.toString());
				visitList(argumentsList);
				popNode();
			}
			addNode(42,methodName.getStartPosition(),node.getStartPosition() + node.getLength() - methodName.getStartPosition(),"MethodName:" + methodName.getFullyQualifiedName() + ":" + arguments.toString());
	    	visitList(arguments);
	    	popNode();
		}
		return false;
	}
	
	public void endVisit(MethodInvocation node) {
		popNode();
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		System.out.println("Class:\t" + node.getName());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return true;
	}
	
	public boolean visit(CastExpression node) {
		System.out.println("CastExpression:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		Type castType = node.getType();
		castType.accept(this);
		popNode();
		Expression exp = node.getExpression();
		exp.accept(this);
		return false;
	}
	
	public void endVisit(CastExpression node) {
		popNode();
	}
	
	public boolean visit(SimpleName node) {
		System.out.println("SimpleName:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return false;
	}
	
	public void endVisit(SimpleName node) {
		popNode();
	}
	
	public boolean visit(QualifiedName node) {
		System.out.println("QualifiedName:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		Name name = node.getQualifier();
		SimpleName simpleName = node.getName();
		name.accept(this);
		simpleName.accept(this);
		return false;
	}
	
	public void endVisit(QualifiedName node) {
		popNode();
	}
	
	public boolean visit(ClassInstanceCreation node) {
		System.out.println("ClassInstanceCreation:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return true;
	}
	
	public boolean visit(IfStatement node) {
		System.out.println("IfStatement:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		return true;
	}
	
	public boolean visit(ConditionalExpression node) {
		System.out.println("ConditionalExpression:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		Expression conditionalExp = node.getExpression();
		Expression thenExp = node.getThenExpression(); 
		Expression elseExp = node.getElseExpression();
		conditionalExp.accept(this);
		thenExp.accept(this);
		elseExp.accept(this);
		return false;
	}
	
	public void endVisit(ConditionalExpression node) {
		popNode();
	}
	
	public boolean visit(InfixExpression node) {
//		System.out.println("InfixExpression:"+node.toString());
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		
		Expression leftExp = node.getLeftOperand();
		leftExp.accept(this);
		
		String op = node.getOperator().toString();
		addNode(-1,leftExp.getStartPosition()+leftExp.getLength()+1,op.length(),op);
		popNode();
		
		Expression rightExp = node.getRightOperand();
		rightExp.accept(this);
		
		List<?> extendedOperands = node.extendedOperands();
		visitList(extendedOperands);
		
//		popNode();
		return false;
	}

	public void endVisit(InfixExpression node) {
		if(this.trees.size()!=0) {
		popNode();
		}
	}
	
    public boolean visit(VariableDeclarationStatement node) {
    	System.out.println("VariableDeclarationStatement:"+node.toString());
    	String nodeStr = node.toString();
    	nodeStr = nodeStr.substring(0, nodeStr.length() - 1);
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
        List<?> modifiers = node.modifiers();
        for (Object obj : modifiers) {
        	IExtendedModifier modifier = (IExtendedModifier) obj;
        	if (modifier.isModifier()) {
        		((Modifier)modifier).accept(this);
        	}
        }
        
    	Type type = node.getType();
    	type.accept(this);
    	List<?> fragments = node.fragments();
    	visitList(fragments);
        return false;
    }
    
//    public boolean visit(SimpleType node) {
//    	System.out.println("SimpleType:"+node.toString());
//    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.getName().getFullyQualifiedName());
//        return false;
//    }
    
    public boolean visit(NumberLiteral node) {
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
    	return false;
    }
    
    @Override
	public void endVisit(NumberLiteral node) {
		popNode();
	}
    
    public boolean visit(ReturnStatement node) {
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
    	Expression exp = node.getExpression();
    	if (exp != null) {
    		addNode(exp.getNodeType(),exp.getStartPosition(),exp.getLength(),exp.toString());
    		exp.accept(this);
    	} else {
    		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),"");
    	}
        return false;
    }
    
    public boolean visit(PrimitiveType node) {
    	System.out.println("PrimitiveType:"+node.toString());
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
        return false;
    }
    
    @Override
    public boolean visit(QualifiedType node) {
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
        return false;
    }

    @Override
    public boolean visit(SimpleType node) {
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.getName().getFullyQualifiedName());
        return false;
    }
    
    public boolean visit(Modifier node) {
    	System.out.println("Modifier:"+node.toString());
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
    	return false;
    }
    
	@Override
	public boolean visit(FieldAccess node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		Expression exp = node.getExpression();
		exp.accept(this);
		SimpleName identifier = node.getName();
		identifier.accept(this);
		return false;
	}

	@Override
	public void endVisit(FieldAccess node) {
		popNode();
	}
	
	@Override
	public boolean visit(NullLiteral node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),"null");
		return false;
	}

	@Override
	public void endVisit(NullLiteral node) {
		popNode();
	}
	
	@Override
	public boolean visit(StringLiteral node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.getEscapedValue());
		return false;
	}

	@Override
	public void endVisit(StringLiteral node) {
		popNode();
	}
	
	@Override
	public boolean visit(ThisExpression node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),"this");
		return false;
	}

	@Override
	public void endVisit(ThisExpression node) {
		popNode();
	}
	
    @Override
    public boolean visit(ConstructorInvocation node) {
        String nodeStr = node.toString();
        nodeStr = nodeStr.substring(0, nodeStr.length() - 1);
        addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),nodeStr);
//        List<?> typeArguments = node.typeArguments();
        List<?> arguments = node.arguments();
//        visitList(typeArguments);
        visitList(arguments);
        return false;
    }
    
    @Override
    public boolean visit(SuperConstructorInvocation node) {
//    	String nodeStr = node.toString();
//    	nodeStr = nodeStr.substring(0, nodeStr.length() - 1);
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
        visitList(node.arguments());
        return false;
    }
    
    @Override
    public boolean visit(TypeLiteral node) {
    	addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
        return false;
    }

    @Override
    public void endVisit(TypeLiteral node) {
        popNode();
    }
    
	@Override
	public boolean visit(InstanceofExpression node) {
		addNode(node.getNodeType(),node.getStartPosition(),node.getLength(),node.toString());
		Expression exp = node.getLeftOperand();
		exp.accept(this);
		addNode(-3,exp.getStartPosition()+exp.getLength()+1,10,"instanceof");
//		push(-3, "Instanceof", "instanceof", exp.getStartPosition() + exp.getLength() + 1, 10);
		popNode();
		Type type = node.getRightOperand();
		type.accept(this);
		return false;
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		popNode();
	}
	
	//TODO:visit while statements
	
//    @Override
//    public boolean visit(WhileStatement node) {
//        addNode(node.getNodeType(), node.getStartPosition(),node.getLength(),node.getExpression().toString());
//        return true;
//    }
//
//    @Override
//    public void endVisit(WhileStatement node) {
//        popNode();
//    }
    

	
	private void addNode(Integer type,int startPos,int len,String label) {
		Tree tree=new Tree(type,startPos,len,label);
		if(!trees.isEmpty()) {
			Tree parent=trees.peek();
			tree.setParentAndUpdateChildren(parent);
		}
		else {
			root=tree;
		}
		trees.push(tree);
	}
	
    protected void popNode() {
        trees.pop();
    }
	
//	public List<Tree> getNodes(){return nodes;}
	public Tree getTree() {
		return root;
	}
	
    private void visitList(List<?> list) {
        for (Object obj : list) {
        	ASTNode node = (ASTNode) obj;
            (node).accept(this);
        }
    }
	

}

 