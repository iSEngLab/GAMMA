package fixer;
import java.util.ArrayList;
import java.util.List;

public class Tree {
    public Integer type;
    public int startPos;
    public int len;
    public String label;
    public List<Tree> children=new ArrayList();
    public Tree parent;
    
    Tree(Integer type,int startPos,int len,String label){
    	this.type=type;
    	this.startPos=startPos;
    	this.len=len;
    	this.label=label;
    }
    
    public void setParentAndUpdateChildren(Tree parent) {
        if (this.parent != null)
            this.parent.getChildren().remove(this);
        this.parent = parent;
        if (this.parent != null)
            parent.getChildren().add(this);
    }
    
    public List<Tree> getChildren(){return children;}
    
    public Integer getType() {return type;}
    
    public String getLabel() {return label;}
    
    public int getPos() {return startPos;}
    
    public int getLen() {return len;}
    
    public Tree get(int n) {
    	return children.get(n);
    }
    
    public int getChildPosition(Tree child) {
    	return getChildren().indexOf(child);
    }
   
}
