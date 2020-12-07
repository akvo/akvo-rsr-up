package org.akvo.rsr.up.viewadapter;


public class ResultNode {
    public enum NodeType {
        RESULT,INDICATOR,PERIOD,DATA,COMMENT 
    }
    private NodeType mNodeType;
    private int mId;
    private int mImageRes;
    String mText;
    String mActualValue;
    boolean mLocked;

    /**
     * constructors
     */
    public ResultNode(NodeType itemType, int id, String text, int ir) {
        setNodeType(itemType);
        setId(id);
        setImageRes(ir);
        setText(text);
    }

    public ResultNode(NodeType itemType, int id, String text, int ir, String av, boolean locked) {
        setNodeType(itemType);
        setId(id);
        setImageRes(ir);
        setActualValue(av); //Must accompany a data update
        setText(text);
        mLocked = locked;
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

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getActualValue() {
        return mActualValue;
    }

    public void setActualValue(String a) {
        this.mActualValue = a;
    }

    public int getImageRes() {
        return mImageRes;
    }

    public void setImageRes(int imageRes) {
        mImageRes = imageRes;
    }

}
