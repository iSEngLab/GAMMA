import java.io.IOException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvException;

import fixer.Fixer;


public class Main {
	public static void main(String[] args) throws IOException {
		//获取bugid
//		List<String> allBugs=FileReader.readByBufferedReader("");
//	    List<String> bugIds=new ArrayList();
//	    for(int i=0;i<allBugs.size();i+=1) {
//	    	bugIds.add(allBugs.get(i));
//	    }
	    
	    //获取每个bug对应的输入行
//	    List<String> paths=FileReader.readByBufferedReader("inputs/meta.txt");
//	    List<Integer> inputLineNos=new ArrayList();
//	    for(int i=0;i<bugIds.size();i++) {
//	    	String id=bugIds.get(i);
//	    	String project=id.split("_")[0];
//	    	String no=id.split("_")[1];
//	    	boolean find=false;
//	    	for(int j=0;j<paths.size();j++) {
//	    		if(project.equals(paths.get(j).split("\t")[0]) && no.equals(paths.get(j).split("\t")[1])) {
//	    			inputLineNos.add(j);
//	    			find=true;
////	    			System.out.println(j);
////	    			break;
//	    		}
//	    		else {
//	    			if(find) {
//	    				break;
//	    			}
//	    		}
//	    	}
//	    }
	    
	    //获取输入
//	    List<String> allInputs=FileReader.readByBufferedReader("/Users/zhangtongke/Desktop/patchGeneration/d4j2_rem.txt");
//	    List<String> myInputs=new ArrayList();
//	    for(int line=0;line<allInputs.size();line++) {
//	    	myInputs.add(line+"\t"+allInputs.get(line));
//	    }

		
		List<String> allInputs=FileReader.readByBufferedReader("inputs/quixbugs_meta.txt");
		List<String> myInputs=new ArrayList();
		for(int line=0;line<allInputs.size();line++) {
			myInputs.add(line+"\t"+allInputs.get(line).split("\t")[3]);
		}
		

		Fixer fixer=new Fixer();
		
		int count=0;
		List<String> fatals=new ArrayList();
	    for(int i=0;i<myInputs.size();i++) {
	    	String rawInput=myInputs.get(i).split("\t")[1];
//	    	System.out.println("rawInput:"+rawInput);
	    	String finalInput=rawInput;
	    	
	    	if(rawInput.contains("} else if ")) {
	    		finalInput=finalInput.substring(7);
	    	}
	    	
	    	if(rawInput.endsWith("{ ") || rawInput.endsWith("{")) {
	    		finalInput+="}";
	    	}
	    	
	    	if(rawInput.contains("if") && !rawInput.contains("{")) {
	    		finalInput+="{}";
	    	}
	    	
	    	List<String> allOutputs=new ArrayList();
	    	allOutputs.add("");
	    	allOutputs.add("<mask0>;");
	    	allOutputs.add("<mask0>; "+rawInput);
	    	try {
	    	allOutputs.addAll(fixer.fix(finalInput));
	    	}
	    	catch(Exception e) {
	    		System.out.println(i);
	    		fatals.add(myInputs.get(i));
//	    		FileWriter.writeNewLine("inputs/fatal.txt", "line:"+i+"\t");
	    	}
	    	
	    	count+=1;
	    	for(String output:allOutputs) {
		    	if(rawInput.contains("} else if")) {
		    		output="} else "+output;
		    	}
		    	
		    	if((rawInput.endsWith("{ ") || rawInput.endsWith("{"))&& output.endsWith("}")) {
		    		output=output.substring(0,output.length()-1);
		    	}
		    	
		    	if(rawInput.contains("if") && !rawInput.contains("{") && output.endsWith("}")) {
		    		output=output.substring(0,output.length()-2);
		    	}
		    	
		    	System.out.println(output);
	    		FileWriter.writeNewLine("inputs/inputLines_quixbugs.txt", "line:"+i+"\t"+output);
	    	}
	    	

	}
	    System.out.println(fatals.size());
	    for(String fatal:fatals) {
	    	System.out.println(fatal);
	    }
	}

	

}