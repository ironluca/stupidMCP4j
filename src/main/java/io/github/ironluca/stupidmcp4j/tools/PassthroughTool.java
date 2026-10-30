package io.github.ironluca.stupidmcp4j.tools;

import java.util.Map;

/*
 * Copyright 2026 The stupidMCP4j Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
