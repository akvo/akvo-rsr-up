package org.akvo.rsr.up.viewadapter;


public class ResultNode {
    public enum NodeType {
        RESULT,INDICATOR,PERIOD 
    }
    NodeType mNodeType;
    int mId;
    String mText;

    public ResultNode(NodeType itemType, int id, String text) {
        mNodeType = itemType;
        mId = id;
        mText = text;
    }
}
