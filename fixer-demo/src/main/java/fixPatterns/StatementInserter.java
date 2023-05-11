package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Tree;

public class StatementInserter extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		List<String> res=new ArrayList();
		
		//加return
		if(code.equals("}")) {
//			for(int i=1;i<=10;i++) {
//				String temp="return";
//				for(int j=1;j<=i;j++) {
//					temp+="<mask>";
//				}
//				temp+=";\n";
//				temp+=code;
//				res.add(temp);
//				System.out.println(temp);
//			}
			res.add("return; "+code);
			res.add("return <mask0>; "+code);
			return res;
		}
		
		//直接替换成一行全是mask
//		for(int i=1;i<=20;i++) {
//			String temp="";
//			for(int j=1;j<=i;j++) {
//				temp+="<mask>";
//			}
//			temp+=";";
//			res.add(temp);
//			System.out.println(temp);
//		}
//		res.add("<mask>;");
		
		//加if
//		for(int i=1;i<=10;i++) {
//			String temp="if(";
//			for(int j=1;j<=i;j++) {
//				temp+="<mask>";
//			}
//			temp+="){\n";
//			temp+=code;
//			temp+="\n}";
//			res.add(temp);
//			System.out.println(temp);
//		}
		res.add("if(<mask0>){ "+code+"}");
		
		//加try catch
		String temp="try{";
		temp+=code;
		temp+="}catch(Exception e){ }";
		res.add(temp);

		
		return res;
	}
}
