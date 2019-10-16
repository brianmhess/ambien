package hessian.ambien;

import java.io.*;
import java.util.*;

public class AmbienParams {
    public String dataCenter = "caas-dc";
    public String username = null;
    public String password = null;
    public String kt_list = null;
    public List<String> table_name = new ArrayList<String>();
    public List<String> keyspace_name = new ArrayList<String>();
    public String output_dir = null;
    public int httpPort = 8222;
    public String endpointRoot = "api/$keyspace/$table";
    public String package_name = "hessian.ambien";
    public String apolloBundle = null;

    public String javaSrcDir = null;
    public String resourcesDir = null;
    public String resourcesTemplatesDir = null;
    public String srcDomainDir = null;
    public String srcRepositoryDir = null;
    public String srcControllerDir = null;

    public static final String apolloBundleOption = "-apolloBundle";
    public static final String usernameOption = "-user";
    public static final String passwordOption = "-pw";
    public static final String ktlistOption = "-kt";
    public static final String outputDirOption = "-o";
    public static final String configFileOption = "-configFile";
    public static final String httpPortOption = "-httpPort";
    public static final String endpointRootOption = "-endpointRoot";
    public static final String packageNameOption = "-packageName";
    public static final String dataCenterOption = "-dc";

    public static String usage() {
        StringBuilder usage = new StringBuilder();
        usage.append("Usage: ambien -apolloBundle <creds.zip> -user <username> -pw <password> -kt <keyspaceName.tableName> -o <outputDir> [options]\n");
        usage.append("OPTIONS:\n");
        usage.append("  " + dataCenterOption + " <dataCenter>               Data center to connect to [dc1]\n");
        usage.append("  " + ktlistOption + " <keyspace.table>           Keyspace and Table to use, can be a comma-separated list [required]\n");
        usage.append("  " + outputDirOption + " <outputDir>                 Directory to write to (must be empty) [required]\n");
        usage.append("  " + configFileOption + " <filename>         File with configuration options [none]\n");
        usage.append("  " + usernameOption + " <username>               Cassandra username [none]\n");
        usage.append("  " + passwordOption + " <password>                 Password for user [none]\n");
        usage.append("  " + httpPortOption + " <httpPort>           Port for HTTP REST endpoint [8222]\n");
        usage.append("  " + endpointRootOption + " <root>           REST endpoint to create (use '$keyspace' for keyspace name and '$table' for table name) [api/$keyspace/$table]\n");
        usage.append("  " + packageNameOption + " <pkg>             Package name [hessian.ambien]\n");
        usage.append("  " + apolloBundleOption + " <filename>        Apollo credentials zip file [none]\n");
        return usage.toString();
    }

    private boolean validateArgs() {
        if (null == username) {
            System.err.println("Must specify a username");
            return false;
        }
        if (null == password) {
            System.err.println("Must specify a password");
            return false;
        }
        if (null == kt_list) {
            System.err.println("No keyspace.table provided");
            return false;
        }
        if (!makeKeyspaceTableLists()) {
            System.err.println("Keyspace.Table list incorrect (\"" + kt_list + "\")");
            return false;
        }
        if (0 == table_name.size()) {
            System.err.println("No table name provided.");
            return false;
        }

        if (0 == keyspace_name.size()) {
            System.err.println("No keyspace name provided.");
            return false;
        }

        if (null == output_dir) {
            System.err.println("No output directory provided.");
            return false;
        }

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

        if (null != (tkey = amap.remove(configFileOption)))
            if (!processConfigFile(tkey, amap))
                return false;

        apolloBundle = amap.remove(apolloBundleOption);
        if (null == apolloBundle) { // host is required
            System.err.println("Must provide an Apollo credentials file");
            return false;
        }

        if (null != (tkey = amap.remove(dataCenterOption)))    dataCenter = tkey;
        if (null != (tkey = amap.remove(usernameOption)))      username = tkey;
        if (null != (tkey = amap.remove(passwordOption)))      password = tkey;
        if (null != (tkey = amap.remove(ktlistOption)))        kt_list = tkey;
        if (null != (tkey = amap.remove(httpPortOption)))      httpPort = Integer.parseInt(tkey);
        if (null != (tkey = amap.remove(endpointRootOption)))  endpointRoot = tkey;
        if (null != (tkey = amap.remove(packageNameOption)))   package_name = tkey;
        if (null != (tkey = amap.remove((outputDirOption)))) {
            if (tkey.endsWith("\\"))
                tkey = tkey.substring(0, tkey.length()-1);
            output_dir = tkey;
            setPaths();
        }

        return validateArgs();
    }

    private String pathify(String[] elements) {
        if (elements.length < 1)
            return "";
        String retStr = elements[0];
        for (int i = 1; i < elements.length; i++)
            retStr = retStr + File.separator + elements[i];
        return retStr;
    }
    private String makePath(String ... segments) {
        String retStr = segments[0];
        for (int i = 1; i < segments.length; i++)
            retStr = retStr + File.separator + segments[i];
        return retStr;
    }

    private void setPaths() {
        resourcesDir = makePath(output_dir, "src", "main", "resources");
        resourcesTemplatesDir = makePath(output_dir, "src", "main", "resources", "templates");
        String pkgPath = pathify(package_name.split("\\."));
        javaSrcDir = makePath(output_dir, "src", "main", "java", pkgPath);
        srcDomainDir = makePath(output_dir, "src", "main", "java", pkgPath, "domain");
        srcRepositoryDir = makePath(output_dir, "src", "main", "java", pkgPath, "dao");
        srcControllerDir = makePath(output_dir, "src", "main", "java", pkgPath, "controller");

    }

    private boolean makeKeyspaceTableLists() {
        String[] ktlist = kt_list.split(",");
        if (ktlist.length < 1) {
            System.err.println("Found only " + ktlist.length + " keyspace/table pairs");
            return false;
        }
        for (String kt : ktlist) {
            String[] ktpair = kt.split("\\.");
            if (ktpair.length != 2) {
                System.err.println("Bad keyspace/table input: " + kt + " (" + ktpair.length + ") [" + Arrays.toString(ktpair) + "]");
                return false;
            }
            keyspace_name.add(ktpair[0]);
            table_name.add(ktpair[1]);
        }
        return true;
    }

    public String endpointRoot(String keyspace_name, String table_name) {
        String retStr = endpointRoot.replace("$keyspace", keyspace_name);
        retStr = retStr.replace("$table", table_name);
        if (retStr.endsWith("/")) retStr = retStr.substring(0, retStr.length() - 1);
        return retStr;
    }

    @Override
    public String toString() {
        return "AmbienParams{" +
                "apolloBundle='" + apolloBundle + '\'' +
                ", dc='" + dataCenter + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", kt_list='" + kt_list + '\'' +
                ", output_dir='" + output_dir + '\'' +
                ", httpPort=" + httpPort +
                ", endpointRoot='" + endpointRoot + '\'' +
                ", package_name='" + package_name + '\'' +
                '}';
    }
}
