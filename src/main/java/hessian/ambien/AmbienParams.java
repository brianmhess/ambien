package hessian.ambien;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AmbienParams {
    public String host = null;
    public int port = 9042;
    public String username = null;
    public String password = null;
    public String truststorePath = null;
    public String truststorePwd = null;
    public String keystorePath = null;
    public String keystorePwd = null;
    public String table_name = null;
    public String keyspace_name = null;
    public String output_dir = null;
    public int httpPort = 8222;
    public String endpointRoot = "api/$keyspace/$table";

    public String srcDir = null;
    public String srcMainDir = null;
    public String srcMainJavaDir = null;
    public String srcMainJavaHessianDir = null;
    public String srcMainJavaHessianAmbienDir = null;
    public String srcMainJavaHessianAmbienDomainDir = null;
    public String srcMainJavaHessianAmbienRepositoryDir = null;
    public String srcMainJavaHessianAmbienControllerDir = null;
    public String srcMainResourcesDir = null;
    public String srcMainResourcesStaticDir = null;
    public String srcMainResourcesTemplatesDir = null;

    public static String usage() {
        StringBuilder usage = new StringBuilder();
        usage.append("OPTIONS:\n");
        usage.append("  -host <hostname>               Contact point for DSE [required]\n");
        usage.append("  -k <keyspacename>              Keyspace to use [required]\n");
        usage.append("  -t <tablename>                 Table to use [required]\n");
        usage.append("  -o <outputDir>                 Directory to write to (must be empty) [required]\n");
        usage.append("  -configFile <filename>         File with configuration options [none]\n");
        usage.append("  -port <portNumber>             CQL Port Number [9042]\n");
        usage.append("  -user <username>               Cassandra username [none]\n");
        usage.append("  -pw <password>                 Password for user [none]\n");
        usage.append("  -ssl-truststore-path <path>    Path to SSL truststore [none]\n");
        usage.append("  -ssl-truststore-pw <pwd>       Password for SSL truststore [none]\n");
        usage.append("  -ssl-keystore-path <path>      Path to SSL keystore [none]\n");
        usage.append("  -ssl-keystore-pw <pwd>         Password for SSL keystore [none]\n");
        usage.append("  -httpPort <httpPort>           Port for HTTP REST endpoint [8222]\n");
        usage.append("  -endpointRoot <root>           REST endpoint to create (use '$keyspace' for keyspace name and '$table' for table name) [api/$keyspace/$table]");
        return usage.toString();
    }

    private boolean validateArgs() {
        if (null == host) {
            System.err.println("No host provided.");
            return false;
        }
        if (null == table_name) {
            System.err.println("No table name provided.");
            return false;
        }

        if (null == keyspace_name) {
            System.err.println("No keyspace name provided.");
            return false;
        }

        if (null == output_dir) {
            System.err.println("No output directory provided.");
            return false;
        }

        if (false == setEndpointRoot()) return false;
        return true;
    }

    private boolean processConfigFile(String fname, Map<String, String> amap)
            throws IOException {
        File cFile = new File(fname);
        if (!cFile.isFile()) {
            System.err.println("Configuration File must be a file");
            return false;
        }

        BufferedReader cReader = new BufferedReader(new FileReader(cFile));
        String line;
        while ((line = cReader.readLine()) != null) {
            String[] fields = line.trim().split("\\s+");
            if (2 != fields.length) {
                System.err.println("Bad line in config file: " + line);
                return false;
            }
            if (null == amap.get(fields[0])) {
                amap.put(fields[0], fields[1]);
            }
        }
        return true;
    }

    public boolean parseArgs(String[] args)
            throws IOException {
        String tkey;
        if (args.length == 0) {
            System.err.println("No arguments specified");
            return false;
        }
        if (0 != args.length % 2)
            return false;

        Map<String, String> amap = new HashMap<String,String>();
        for (int i = 0; i < args.length; i+=2) {
            amap.put(args[i], args[i+1]);
        }

        if (null != (tkey = amap.remove("-configFile")))
            if (!processConfigFile(tkey, amap))
                return false;

        host = amap.remove("-host");
        if (null == host) { // host is required
            System.err.println("Must provide a host");
            return false;
        }

        if (null != (tkey = amap.remove("-port")))          port = Integer.parseInt(tkey);
        if (null != (tkey = amap.remove("-user")))          username = tkey;
        if (null != (tkey = amap.remove("-pw")))            password = tkey;
        if (null != (tkey = amap.remove("-ssl-truststore-path"))) truststorePath = tkey;
        if (null != (tkey = amap.remove("-ssl-truststore-pw")))  truststorePwd =  tkey;
        if (null != (tkey = amap.remove("-ssl-keystore-path")))   keystorePath = tkey;
        if (null != (tkey = amap.remove("-ssl-keystore-pw")))    keystorePwd = tkey;
        if (null != (tkey = amap.remove("-t")))               table_name = tkey;
        if (null != (tkey = amap.remove("-k")))               keyspace_name = tkey;
        if (null != (tkey = amap.remove(("-o")))) {
            if (tkey.endsWith("\\"))
                tkey = tkey.substring(0, tkey.length()-1);
            output_dir = tkey;
            setPaths();
        }
        if (null != (tkey = amap.remove("-httpPort")))       httpPort = Integer.parseInt(tkey);
        if (null != (tkey = amap.remove("-endpointRoot")))   endpointRoot = tkey;

        return validateArgs();
    }

    private void setPaths() {
        srcDir = output_dir + File.separator + "src";
        srcMainDir = srcDir + File.separator + "main";
        srcMainJavaDir = srcMainDir + File.separator + "java";
        srcMainJavaHessianDir = srcMainJavaDir + File.separator + "hessian";
        srcMainJavaHessianAmbienDir = srcMainJavaHessianDir + File.separator + "ambien";
        srcMainJavaHessianAmbienDomainDir = srcMainJavaHessianAmbienDir + File.separator + "domain";
        srcMainJavaHessianAmbienRepositoryDir = srcMainJavaHessianAmbienDir + File.separator + "repository";
        srcMainJavaHessianAmbienControllerDir = srcMainJavaHessianAmbienDir + File.separator + "contoller";
        srcMainResourcesDir = srcMainDir + File.separator + "resources";
        srcMainResourcesStaticDir = srcMainResourcesDir + File.separator + "static";
        srcMainResourcesTemplatesDir = srcMainResourcesDir + File.separator + "templates";
    }

    private boolean setEndpointRoot() {
        endpointRoot = endpointRoot.replace("$keyspace", keyspace_name);
        endpointRoot = endpointRoot.replace("$table", table_name);
        if (endpointRoot.endsWith("/")) endpointRoot = endpointRoot.substring(0, endpointRoot.length() - 1);
        return true;
    }
}
