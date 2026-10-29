package io.github.ironluca.stupidmcp4j.tools;

import java.util.Map;

/**
 * This is a simple passthrough tool implementation to demonstrate how tools need to be implemented. 
 * Implementing a tool only requires the below:
 * <ol>
 *  <li>The class MUST have a default constructor.</li>
 *  <li>The class MUST implemen a method called 'execute' with signature {@code public Object execute(Map<String, Object> params) throws Exception}.</li>
 * </ol>
 * 
 * @author Ironluca
 * @see #execute(Map)
 */
public class PassthroughTool {

    /**
     * This method MUST be implemented by all tool implementations. This method will be called to execute the tool.
     * 
     * @param params This map will contain the parameters required by the tool, where the keys of this map are 
     * string, which are parameter names and values objects.
     * @return The response of the tool, this can be a String, any Object, Map or List. In case of List, each 
     * object in the list becomes a block in the returned 'content' array of the tool response.
     * @throws Exception Tool MUST throw an exception if there are any tool execution errors. The .getMessage() of 
     * the exception will be called to get any message sent by the tool to the client.
     */
    public Object execute(Map<String, Object> params) throws Exception{

        String message="This is a message from pass through tool";

        if(params.containsKey("message"))message=params.get("message").toString();

        return message;

    }//execute closing

}//class closing
