# GAMMA: Revisiting Template-based Automated Program Repair via Mask Prediction

## Running environment

The Python libraries transformers and pytorch are required to run the code.

```
pip install transformers
pip install pytorch
```

## Fix templates

Fix tempaltes are defined in the code in the repository *fixer-demo*. To get the buggy lines which are masked according to different templates, run **Main.java** under the path fixer-demo/src/main/java.

## Mask prediction

The code related to mask prediction task is in the repository *patchGeneration*. To generate patches for the bugs, first download the datasets Defects4J and QuixBugs from [here](https://github.com/rjust/defects4j) and [here](https://github.com/jkoppel/QuixBugs).

Then run **getContext.py** to access the context of the buggy code.

Finally run **unixcoder_repair.py**, **codebert_repair.py**, and **gpt_repair.py** respectively to fill the masks with UniXcoder, CodeBERT and ChatGPT.
