package io.github.ironluca.stupidmcp4j;

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

}//interface closing
