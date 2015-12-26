package edu.cpp.iipl.crawlers.amazon.model;

import java.util.Date;

/**
 * Created by xing on 12/22/15.
 */
public class Node {

    //@Id
    //@GeneratedValue
    private Long id;

    //@NotNull
    private String name;

    //@NotNull
    //@Column(unique = true)
    private String nodeId;

    public Node() {
    }

    public Node(String name, String nodeId) {
        this.name = name;
        this.nodeId = nodeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
