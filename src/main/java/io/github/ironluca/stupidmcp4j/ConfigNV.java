package io.github.ironluca.stupidmcp4j;

import java.util.List;

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
public interface ConfigNV {

    /** The environment variable that contains the stupid MCP home. The path to the installation directory. */
    public static final String envStupidmcpHome="STUPIDMCP_HOME";

    public static final String confDir="conf";
    public static final String serverConf="server.json";
    public static final String toolsConf="tools.json";

    /** The default host is localhost, recommended, as the server does not provide HTTPS or any authentication, can be changed in configuration. */
    public static final String defaultHost="127.0.0.1";

    /** The default port, can be changed inconfiguration */
    public static final int defaultPort=8008;

    /** The JSON-RPC supported version  */
    public static String jsonRPCSupportedVersion="2.0";

    /** The supported MCP versions */
    public List<String> mcpSupportedVersions=List.of("2024-11-05","2026-07-28");

    public static final String serverName="stupidMCP4j";
    public static final String serverVersion="1.0.0";

}//interface closing
