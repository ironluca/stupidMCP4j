package io.github.ironluca.stupidmcp4j.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.node.ObjectNode;

/**
 * The basic definition of a tool. This is used to list tools primarily.
 * 
 * @author sanjay.debnath
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolDefinition {

    @JsonProperty("name")
    public String name;

    @JsonProperty("title")
    public String title;

    @JsonProperty("description")
    public String description;

    @JsonProperty("inputSchema")
    public ObjectNode inputSchema;

}//class closing
