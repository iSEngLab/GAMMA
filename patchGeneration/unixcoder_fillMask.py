import torch
from unixcoder import UniXcoder

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = UniXcoder("microsoft/unixcoder-base")
model.to(device)


def fill_mask(str, target_line):
    tokens_ids = model.tokenize([str],max_length=512,mode="<encoder-decoder>")
    source_ids = torch.tensor(tokens_ids).to(device)
    prediction_ids = model.generate(source_ids, decoder_only=False, beam_size=25, max_length=128)
    predictions = model.decode(prediction_ids)
    res=[]
    for prediction in predictions[0]:
        tmp=target_line.replace('<mask0>',prediction.replace('<mask0>','')).split('\n')
        output=''
        for line in tmp:
            output+=line
            output+=' '
        res.append(output)
    return res


def start():
    with open("context_d4j2.txt",'r') as f:
        lines=f.readlines()

        for i in range(0, len(lines)):
            if lines[i].startswith("line:"):
                line_no=int(lines[i][5:-1])
                start_line = i + 1
                end_line = i + 1
                while not lines[end_line].startswith("position:"):
                    end_line += 1
                context = ""
                target_line = ''
                for j in range(start_line, end_line):
                    context += lines[j]
                    if '<mask0>' in lines[j]:
                        target_line = lines[j]

                if "<mask0>" in context:
                    try:
                        print(context)
                        res = fill_mask(context,target_line)
                        for output in res:
                            # print(line_no)
                            # print(output)
                            with open("results/"+str(line_no)+'.txt', 'a') as f:
                                # f.write(lines[start_line - 1])
                                f.write(output)
                                # f.write(output[end_line - 1])
                    except Exception as e:
                        print(lines[i])


if __name__=='__main__':
    str="""
//if (dp.containsKey(i-1)) {
public static Integer lcs_length(String s, String t) {
// make a Counter
// pair? no! just hashtable to a hashtable.. woo.. currying
Map<Integer, Map<Integer,Integer>> dp = new HashMap<Integer,Map<Integer,Integer>>();
// just set all the internal maps to 0
for (int i=0; i < s.length(); i++) {
Map<Integer,Integer> initialize = new HashMap<Integer,Integer>();
dp.put(i, initialize);
for (int j=0; j < t.length(); j++) {
Map<Integer,Integer> internal_map = dp.get(i);
internal_map.put(j,0);
dp.put(i, internal_map);
}
}
// now the actual code
for (int i=0; i < s.length(); i++) {
for (int j=0; j < t.length(); j++) {
if (s.charAt(i) == t.charAt(j)) {
if (dp.containsKey(i-1)<mask0>) {
Map<Integer, Integer> internal_map = dp.get(i);
int insert_value = dp.get(i-1).get(j) + 1;
internal_map.put(j, insert_value);
dp.put(i,internal_map);
} else {
Map<Integer, Integer> internal_map = dp.get(i);
internal_map.put(j,1);
dp.put(i,internal_map);
}
}
}
}
if (!dp.isEmpty()) {
List<Integer> ret_list = new ArrayList<Integer>();
for (int i=0; i<s.length(); i++) {
ret_list.add(!dp.get(i).isEmpty() ? Collections.max(dp.get(i).values()) : 0);
}
return Collections.max(ret_list);
} else {
return 0;
}
}
    """

    res=fill_mask(str,'<mask0>;')
    for s in res:
        print(s)
    # start()