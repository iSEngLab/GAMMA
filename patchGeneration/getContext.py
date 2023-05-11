from typing import List
import re
import javalang
from git.repo import Repo
import os
import csv


replaceString = re.compile("[\"].*?[\"]")
def get_end_line(start_line: int, file_back: List[str], upper_limit: int):
    file = file_back.copy()
    left_bracket = 0
    right_bracket = 0
    for i in range(start_line, upper_limit):
        anno_index = file[i].find('//')
        file[i] = file[i][:anno_index] if anno_index != -1 else file[i]
        file[i] = replaceString.sub("", file[i])  ##
        left_bracket += file[i].count('{')
        right_bracket += file[i].count('}')
        if right_bracket == left_bracket and right_bracket:
            return i
    if right_bracket == left_bracket and right_bracket == 0:
        return start_line
    else:
        return -1


def get_function_positions(tree, class_file: List[str]):
    position = []  # start from 0
    methods = []
    for x in tree.body:
        if isinstance(x, javalang.tree.ClassDeclaration):
            for y in x.body:
                if isinstance(y, javalang.tree.ClassDeclaration):
                    methods.extend(y.methods)
                elif isinstance(y, javalang.tree.MethodDeclaration) or isinstance(y,
                                                                                  javalang.tree.ConstructorDeclaration):
                    methods.append(y)
        elif isinstance(x, javalang.tree.MethodDeclaration) or isinstance(x, javalang.tree.ConstructorDeclaration):
            methods.append(x)
    if methods.__len__() == 0:
        methods.extend(tree.methods)
    for i, method in enumerate(methods):
        start_line = method.position.line - 1
        if i + 1 < methods.__len__():
            upper_limit = methods[i + 1].position.line - 1
        else:
            upper_limit = class_file.__len__() - 1
        end_line = get_end_line(start_line, class_file, upper_limit)
        if end_line == -1:
            continue
        position.append((start_line, end_line))
    position = list(set(position))
    position.sort()
    return position


def get_ast(functions: List[str]):
    func = annotate_unsupport_code(functions)
    tokens = javalang.tokenizer.tokenize("".join(func))
    parser = javalang.parser.Parser(tokens)
    tree = parser.parse_member_declaration()
    return tree


def annotate_unsupport_code(code: List[str]):
    for i, line in enumerate(code):
        if line.startswith("package ") or line.startswith("import "):
            code[i] = '//' + code[i]
    return code


def get_context(path,start_line_no,end_line_no,replaceStr):
    # path="defects4j-repair/"+path
    with open(path, 'r') as buggy_class:
        buggy_class = buggy_class.readlines()
    buggy_tree=get_ast(buggy_class)
    #获取所有函数的位置
    buggy_funtions_position = get_function_positions(buggy_tree, buggy_class)
    # 获取目标函数的位置
    target_position = buggy_funtions_position[0]
    for position in buggy_funtions_position:
        if position[0] + 1 <= start_line_no <= position[1] + 1:
            # print(position)
            target_position = position
            break
        # 替换buggy line,抠出目标函数上下文
    buggy_class[start_line_no-1] = replaceStr
    if end_line_no!=start_line_no:
        for i in range(start_line_no+1,end_line_no+1):
            buggy_class[i-1]=""
    context = ""
    for i in range(target_position[0], target_position[1] + 1):
        # 去注释
        if buggy_class[i].startswith("//") or buggy_class[i].startswith("\n"):
            continue

        startPos = 0
        while startPos < len(buggy_class[i]):
            if buggy_class[i][startPos] == ' ':
                startPos += 1
            else:
                break
        if buggy_class[i][startPos:].startswith("//"):
            continue
        context += buggy_class[i][startPos:]
    pos = str(target_position[0])+','+str(target_position[1]) #从0开始的行号
    context += "position:"
    context += pos
    context += "\n"
    return context


def generate_input():
    with open("inputLinesForCodebert.txt",'r') as f:
        input_lines=f.readlines()
    with open('meta.txt','r') as f:
        meta_lines=f.readlines()
    with open('rem.txt','r') as f:
        rem_lines=f.readlines()

    d4jpath=os.path.join("defects4j-repair")
    repo=Repo(d4jpath)
    for i in range(0,len(input_lines)):
        input=input_lines[i]
        if input.startswith("line:"):
            line_no=int(input.split("\t")[0][5:])
            bug_info=meta_lines[line_no]
            project_name=bug_info.split("\t")[0]
            bug_id=bug_info.split("\t")[1]
            #切换git分支
            git_branch=project_name+bug_id
            repo.git.checkout(git_branch)

            bug_path=bug_info.split("\t")[2]
            bug_path="defects4j-repair/"+bug_path
            start_line=int(bug_info.split("\t")[3])
            end_line=int(bug_info.split("\t")[4])
            if len(input.split("\t"))==1:
                input_line=""
            else:
                input_line=input.split("\t")[1]

            i+=1
            while not input_lines[i].startswith("line"):
                input_line+=input_lines[i]
                i+=1

            context=get_context(bug_path,start_line,end_line,input_line)
            # print(project_name+bug_id)
            # print(context)

            with open ("inputContextForCodebert.txt",'a') as f:
                f.write("line:"+str(line_no)+"\n")
                f.write("//"+rem_lines[line_no])
                f.write(context+"\n")


def generate_input_for_d4j2():
    f=open('d4j2_meta.txt')
    meta_lines = f.readlines()
    # with open('inputs/D4JMeta.csv') as f:
    #     meta_file=csv.reader(f)
    c=open('inputLines_d4j2.txt')
    input_lines = c.readlines()
    # with open('inputs/d4j2.0Inputs.txt') as f:
    #     input_lines=f.readlines()
    rem=open('d4j2_rem.txt')
    rem_lines=rem.readlines()

    last_bug=''
    for i in range(0,len(input_lines)):
        line_no=input_lines[i].split('\t')[0][5:]
        input_line=input_lines[i].split('\t')[1]
        bug_info = meta_lines[int(line_no)].split('\t')
        project_name = bug_info[0]
        bug_id = bug_info[1]
        path=bug_info[2]
        start_line=bug_info[3]
        end_line=bug_info[4]

        if not os.path.exists('tmp/'+project_name+bug_id+'_buggy'):
            os.system('rm -rf ' + last_bug)
            command = 'defects4j checkout -p '+project_name+' -v '+bug_id+'b -w tmp/'+project_name+bug_id+'_buggy'
            os.system(command)
            last_bug='tmp/' + project_name + bug_id + '_buggy'
        try:
            context = get_context('tmp/'+project_name+bug_id+'_buggy/'+path,
                                  int(start_line), int(end_line), input_line)
            print('line:'+line_no)
            # print('//'+rem_lines[int(line_no)])
            # print(context)
            with open('context_d4j2.txt','a') as file:
                file.write("line:"+line_no+"\n")
                file.write("//"+rem_lines[int(line_no)])
                file.write(context+"\n")
        except Exception as e:
            print(project_name+bug_id)




    f.close()
    c.close()
    rem.close()


def get_context_quixbugs():
    with open("inputLines_quixbugs.txt",'r') as f:
        buggy_lines=f.readlines()
    with open("quixbugs_meta.txt",'r') as f:
        meta_lines=f.readlines()

    for bug in buggy_lines:
        line_no=int(bug.split('\t')[0][5:])
        meta=meta_lines[line_no]
        bug_name=meta.split('\t')[0]
        with open("QuixBugs-master/java_programs/"+bug_name+".java",'r') as f:
            file_content=f.readlines()
        context=''
        for i in range(0,len(file_content)):
            if "public class" in file_content[i]:
                for j in range(i+1,len(file_content)-1):
                    line=file_content[j]
                    if j+1==int(meta.split('\t')[1]):
                        context+=bug.split('\t')[1]
                    elif int(meta.split('\t')[1]) < j+1 <= int(meta.split('\t')[2]):
                        continue
                    else:
                        if line=='\n' or line.startswith('//'):
                            continue
                        while line[0]==' ':
                            line=line[1:]
                        context+=line

        print("bugid:"+bug_name)
        print("//"+meta.split('\t')[3][:-1])
        print(context)

        with open('inputContext_quixbugs.txt','a') as f:
            f.write("bugid:"+bug_name+'\n')
            f.write("//"+meta.split('\t')[3])
            f.write(context)


def clear_file():
    with open("d4j2Context.txt",'w') as f:
        f.write("")


if __name__=='__main__':
    get_context_quixbugs()
    # clear_file()