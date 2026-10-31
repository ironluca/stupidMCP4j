package io.github.ironluca.stupidmcp4j.tools;

import static io.github.ironluca.stupidmcp4j.ConfigNV.confDir;
import static io.github.ironluca.stupidmcp4j.ConfigNV.envStupidmcpHome;
import static io.github.ironluca.stupidmcp4j.ConfigNV.toolNameValidationRegex;
import static io.github.ironluca.stupidmcp4j.ConfigNV.toolsConf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

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
public class ToolRegistry {

    private static ToolRegistry singleton;

    private ObjectMapper mapper;

    private Path toolsConfigPath;

    private List<ToolDefinition> toolDefs;
    private Map<String, Method> toolMethodMap;
    private Map<Method, Object> toolObjectMap;

    private Pattern nameValdationPattern;

    private ToolRegistry() throws Exception{

        mapper=new ObjectMapper();

        //This will always exist, as this is checked during server startup
        toolsConfigPath=Paths.get(System.getenv(envStupidmcpHome), confDir, toolsConf);

        toolDefs=new ArrayList<>();
        toolMethodMap=new HashMap<>();
        toolObjectMap=new HashMap<>();

        nameValdationPattern=Pattern.compile(toolNameValidationRegex);

        init();

    }//constructor closing

    public static synchronized ToolRegistry getToolRegistry()throws Exception{

        if(singleton==null)singleton=new ToolRegistry();

        return singleton;

    }//getToolRegistry closing


    public void init() throws Exception{

        try{

            JsonNode allToolsNode=mapper.readTree(toolsConfigPath);

            if(!allToolsNode.isArray())
                throw new RuntimeException("The conf/tools.json should contain a JSON array of tools, it is not an array");

            ArrayNode toolsListNode=(ArrayNode)allToolsNode;
            for(int i=0;i<toolsListNode.size();i++){

                JsonNode toolDef=toolsListNode.get(i);

                if(!toolDef.has("name"))
                    throw new RuntimeException("One tool does not have name defined, which is mandatory.\n "+toolDef.toPrettyString());

                if(toolDef.get("name").asString()==null || toolDef.get("name").asString().isBlank())
                    throw new RuntimeException("One tool does not have name defined, which is mandatory.\n "+toolDef.toPrettyString());

                if(!nameValdationPattern.matcher(toolDef.get("name").asString()).matches())
                    throw new RuntimeException("The name of the tool is not valid.\n "+toolDef.toPrettyString());

                if(!toolDef.has("implementation"))
                    throw new RuntimeException("The implementation class for this tool has not been provided.\n "+toolDef.toPrettyString());

                if(toolDef.get("implementation").asString()==null || toolDef.get("implementation").asString().isBlank())
                    throw new RuntimeException("The implementation class for this tool has not been provided.\n "+toolDef.toPrettyString());

                if(!toolDef.has("inputSchema"))
                    throw new RuntimeException("The tool does not define inputSchema, which is mandatory.\n "+toolDef.toPrettyString());

                if(toolDef.get("inputSchema").isNull())
                    throw new RuntimeException("The tool does not define inputSchema, which is mandatory.\n "+toolDef.toPrettyString());

                

                try{

                    String toolName=toolDef.get("name").asString();

                    //Check if there is already a tool with the same name registered, duplicate tool names are not allowed
                    if(toolMethodMap.containsKey(toolName))
                        throw new RuntimeException("Duplicate tool name detected. Tool names must be unique\n "+toolDef.toPrettyString());

                    //Load the class
                    String implClassName=toolDef.get("implementation").asString();
                    Class<?> clazz=Class.forName(implClassName);

                    //Check that the class is public 
                    if(!Modifier.isPublic(clazz.getModifiers()))
                        throw new RuntimeException("The tool class must be public.\n "+toolDef.toPrettyString());


                    //Get the constructor
                    Constructor<Object> constructor=null;

                    try{

                        constructor=(Constructor<Object>)clazz.getDeclaredConstructor();
                        
                    }catch(NoSuchMethodException e){

                        throw new RuntimeException("The implementation class does not have a no-arg default constructor, which is mandatory.\n "+toolDef.toPrettyString());

                    }//catch closing

                    //Check if the default constructor is public
                    if(!Modifier.isPublic(constructor.getModifiers()))
                        throw new RuntimeException("The default constructor must be public.\n "+toolDef.toPrettyString());

                    //Get the method
                    Method execMethod=null;
                    try{

                        execMethod=clazz.getDeclaredMethod("execute", Map.class);
                        
                    }catch(NoSuchMethodException e){

                        throw new RuntimeException("The implementation class does not implement the 'public Object execute(Map<String, Object> params) throws Exception' method, which is mandatory.\n "+toolDef.toPrettyString());

                    }//catch closing


                    //Check if the exec method is public is public
                    if(!Modifier.isPublic(execMethod.getModifiers()))
                        throw new RuntimeException("The execution method must be public.\n "+toolDef.toPrettyString());


                    //Create the tool definition
                    ToolDefinition toolDefinitionObject=mapper.readValue(toolDef.toString(), ToolDefinition.class);
                    toolDefs.add(toolDefinitionObject);    
                    

                    //Register the tool name and exec method
                    toolMethodMap.put(toolName, execMethod);


                    //If the tool object for the method already exists, don't put it in the map again.
                    if(!toolObjectMap.containsKey(execMethod))
                        toolObjectMap.put(execMethod, constructor.newInstance());

                }catch(RuntimeException r){throw r;}
                catch(ClassNotFoundException | LinkageError e){

                    throw new RuntimeException("The implementation class could not be loaded ("+e+"). Check the class name and that the tool JAR and its dependencies are in tools/.\n "+toolDef.toPrettyString());

                }//catch closing
                catch(Exception e){throw new RuntimeException("Could not load tool. Check it it is an abstract class or access controlled.\n "+toolDef.toPrettyString());}

            }//for closing

            toolDefs.sort((o1,o2)->o1.name.compareTo(o2.name));

        }catch(JacksonException je){throw new RuntimeException("The conf/tools.json could not be parsed. Check for syntax errors in the file");}
         catch(Exception e){throw e;}

    }//init closing

    public List<ToolDefinition> getToolList(){return toolDefs;}

    public Method getToolMethod(String toolName){return toolMethodMap.get(toolName);}

    public Object getToolObject(Method toolMethod){return toolObjectMap.get(toolMethod);}

    public ToolDefinition getToolDefinition(String toolName){

        return toolDefs.stream().filter(t->t.name.equals(toolName)).findFirst().get();

    }//getToolDefinition closing

}//class closing
