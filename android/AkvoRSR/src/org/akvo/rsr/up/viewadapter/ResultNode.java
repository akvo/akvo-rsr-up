package org.akvo.rsr.up.viewadapter;


public class ResultNode {
    public enum NodeType {
        RESULT,INDICATOR,PERIOD,DATA,COMMENT 
    }
    private NodeType mNodeType;
    private int mId;
    String mText;
    String mActualValue;

    /**
     * constructors
     */
    public ResultNode(NodeType itemType, int id, String text) {
        setNodeType(itemType);
        setId(id);
        mText = text;
    }

    public ResultNode(NodeType itemType, int id, String text, String av) {
        setNodeType(itemType);
        setId(id);
        setActualValue(av);
        mText = text;
    }

    public NodeType getNodeType() {
        return mNodeType;
    }

    public void setNodeType(NodeType t) {
        this.mNodeType = t;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getActualValue() {
        return mActualValue;
    }

    public void setActualValue(String a) {
        this.mActualValue = a;
    }

}
