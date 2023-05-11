package fixPatterns;

import java.util.ArrayList;
import java.util.List;

import fixer.Tree;

public class StatementMover extends FixPattern{
	public List<String> generatePatch(Tree tree,String code) {
		List<String> res=new ArrayList();
		String fixed="";
		String[] lines=code.split(";");
		String firstLine=lines[lines.length-1];
		boolean hasBrace=false;
		int startPos=0;
		for(int i=0;i<firstLine.length();i++) {
			if(firstLine.charAt(i)=='}') {
				startPos=i+1;
				hasBrace=true;
				break;
			}
		}
		
		fixed+=firstLine.substring(startPos);
		fixed+=";";
		
		for(int i=0;i<lines.length-1;i++){
			fixed+=lines[i];
			fixed+=";";
		}
		if(hasBrace) {
			fixed+="}";
		}
		System.out.println(fixed);
		res.add(fixed);
		
		return res;
	}
}
