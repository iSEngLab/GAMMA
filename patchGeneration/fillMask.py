from transformers import RobertaConfig,RobertaTokenizer,RobertaForMaskedLM,pipeline
import math
import torch

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = RobertaForMaskedLM.from_pretrained("microsoft/codebert-base-mlm")
tokenizer = RobertaTokenizer.from_pretrained("microsoft/codebert-base-mlm")
model.to(device)

fill_mask = pipeline('fill-mask',
                     model=model,
                     tokenizer=tokenizer)
fill_mask.device = torch.device("cuda:0")



def fillMask(code, maskNum):
    res=[]

    if maskNum==1:
        outputs=fill_mask(code)

        for output in outputs:
            res.append(output)
#            print(output)
        return res

    outputs=fill_mask(code)[0]
    for output in outputs:
        output['tempJointScore']=math.log(output['score'])

    for i in range(1,maskNum):
        newOutputs=[]
        for j in range(0,250):
            output=outputs[j]
            if i!=maskNum-1:
                tempOutputs=fill_mask(output['sequence'][3:-4])[0]
            else:
                tempOutputs=fill_mask(output['sequence'][3:-4])
            for o in tempOutputs:
                o['tempJointScore']=(output['tempJointScore']*i+math.log(o['score']))/(i+1)
            newOutputs.extend(tempOutputs)
        newOutputs.sort(key=lambda k: (k.get('tempJointScore', 0)), reverse=True)
        outputs=newOutputs

    for i in range(0,250):
#        print(outputs[i])
        res.append(outputs[i])
    return res


def read_file_and_fill_mask():
    with open("inputContextForCodebert.txt",'r') as f:
        lines=f.readlines()

    for i in range(0,len(lines)):
        if lines[i].startswith("line:"):
            line_no=lines[i][5:-1]
            start_line=i+1
            end_line=i+1
            while not lines[end_line].startswith("position:"):
                end_line+=1
            context=""
            target_line_no=-1
            for j in range(start_line,end_line-1):
                context+=lines[j]
                if '<mask>' in lines[j]:
                    target_line_no=j

            if "<mask>" in context:
                mask_num=context.count("<mask>")
                try:
                    res=fillMask(context,mask_num)
                    for output in res:
                        print(output['sequence'].split('\n')[target_line_no])
                except Exception as e:
                    print(e)
                    print(lines[i])


if __name__=='__main__':
    read_file_and_fill_mask()
