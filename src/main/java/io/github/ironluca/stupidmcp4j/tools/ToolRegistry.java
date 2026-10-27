package io.github.ironluca.stupidmcp4j.tools;

import static io.github.ironluca.stupidmcp4j.ConfigNV.confDir;
import static io.github.ironluca.stupidmcp4j.ConfigNV.envStupidmcpHome;
import static io.github.ironluca.stupidmcp4j.ConfigNV.toolsConf;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import io.github.ironluca.stupidmcp4j.tools.exception.DefaultConstructorNotFoundException;
import io.github.ironluca.stupidmcp4j.tools.exception.DuplicateToolNameException;
import io.github.ironluca.stupidmcp4j.tools.exception.InvalidInputSchemaException;
import io.github.ironluca.stupidmcp4j.tools.exception.ToolClassNotFoundException;
import io.github.ironluca.stupidmcp4j.tools.exception.ToolConfigurationParseException;
import io.github.ironluca.stupidmcp4j.tools.exception.ToolConstructionException;
import io.github.ironluca.stupidmcp4j.tools.exception.ToolExecutionMethodNotFoundException;

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

    private Path toolsConfigPath;

    private Map<String, Method> toolMethodMap;
    private Map<Method, Object> toolObjectMap;

    private ToolRegistry() throws DefaultConstructorNotFoundException, 
                                  DuplicateToolNameException,
                                  InvalidInputSchemaException,
                                  ToolClassNotFoundException,
                                  ToolConfigurationParseException,
                                  ToolConstructionException,
                                  ToolExecutionMethodNotFoundException{

        //This will always exist, as this is checked during server startup
        toolsConfigPath=Paths.get(System.getenv(envStupidmcpHome), confDir, toolsConf);

        init();

    }//constructor closing

    public static synchronized ToolRegistry getToolRegistry()throws DefaultConstructorNotFoundException, 
                                                                    DuplicateToolNameException,
                                                                    InvalidInputSchemaException,
                                                                    ToolClassNotFoundException,
                                                                    ToolConfigurationParseException,
                                                                    ToolConstructionException,
                                                                    ToolExecutionMethodNotFoundException{

        if(singleton==null)singleton=new ToolRegistry();

        return singleton;

    }//getToolRegistry closing

    /**
     * 
     * @throws DefaultConstructorNotFoundException
     * @throws DuplicateToolNameException
     * @throws InvalidInputSchemaException
     * @throws ToolClassNotFoundException
     * @throws ToolConfigurationParseException
     * @throws ToolConstructionException
     * @throws ToolExecutionMethodNotFoundException
     */
    public void init() throws DefaultConstructorNotFoundException, 
                              DuplicateToolNameException,
                              InvalidInputSchemaException,
                              ToolClassNotFoundException,
                              ToolConfigurationParseException,
                              ToolConstructionException,
                              ToolExecutionMethodNotFoundException{

        try{




        }catch(Exception e){e.printStackTrace();}


    }//init closing

    public void getToolList(){}
    public void getToolDef(String name){}

}//class closing
