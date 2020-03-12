package hessian.ambien.web;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.TableMetadata;
import hessian.ambien.Ambien;
import hessian.ambien.AmbienParams;
import org.apache.cassandra.io.FSWriteError;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class AmbienController {
    private static String HOST = "HOST";
    private static int PORT = 9142;
    private static int HTTP_PORT = 8222;
    private static String USERNAME = "USERNAME";
    private static String PASSWORD = "PASSWORD";
    private static String KEYSTORE_PATH = "KEYSTORE_PATH";
    private static String KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
    private static String TRUSTSTORE_PATH = "TRUSTSTORE_PATH";
    private static String TRUSTSTORE_PASSWORD = "TRUSTSTORE_PASSWORD";
    private static String ENDPOINT_ROOT = "api/$keyspace/$table";
    private static Path ambienTmpPath = Paths.get(System.getProperty("user.dir") + "/target/tmp");
    private Session staticSession = null;

    public AmbienController() {
    }

    public void init() {
        if (null != staticSession)
            return;
        extractLibs();
        mkdir(ambienTmpPath.toString());
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        }
        catch (Exception e) {
            System.out.println("Error starting embedded Cassandra");
            System.err.println("ERROR starting embedded Cassandra");
        }
        System.out.println("Ready to REST");
        System.out.flush();
        staticSession = EmbeddedCassandraServerHelper.getSession();
    }

    public static void mkdir(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            throw new FSWriteError(new IOException("Failed to mkdirs " + dir), dir);
        }
    }

    public boolean extractLibs() {
        String[] libs = {
                "libsigar-amd64-freebsd-6.so",
                "libsigar-amd64-linux.so",
                "libsigar-amd64-solaris.so",
                "libsigar-ia64-hpux-11.sl",
                "libsigar-ia64-linux.so",
                "libsigar-pa-hpux-11.sl",
                "libsigar-ppc-aix-5.so",
                "libsigar-ppc-linux.so",
                "libsigar-ppc64-aix-5.so",
                "libsigar-ppc64-linux.so",
                "libsigar-ppc64le-linux.so",
                "libsigar-s390x-linux.so",
                "libsigar-sparc-solaris.so",
                "libsigar-sparc64-solaris.so",
                "libsigar-universal-macosx.dylib",
                "libsigar-universal64-macosx.dylib",
                "libsigar-x86-freebsd-5.so",
                "libsigar-x86-freebsd-6.so",
                "libsigar-x86-linux.so",
                "libsigar-x86-solaris.so",
                "sigar-amd64-winnt.dll",
                "sigar-x86-winnt.dll",
                "sigar-x86-winnt.lib"};
        String target = System.getProperty("user.dir") + "/target/libs";

        mkdir(target);
        for (String lib : libs) {
            InputStream is = this.getClass().getResourceAsStream("/libs/" + lib);
            try {
                Files.copy(is, Paths.get(target, lib), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
        try {
            System.setProperty("java.library.path", target);
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (null == System.getProperty("cassandra.jmx.local.port"))
            System.setProperty("cassandra.jmx.local.port", "7199");

        return true;
    }


    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return webPage(null);
    }

    public String webPage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n");
        sb.append("  <head>\n" +
                "      <meta charset=\"utf-8\">\n" +
                "      <title>Ambien... it gives you REST</title>\n" +
                "    </head>");
        sb.append("<body>\n" +
                "<hr>");

        sb.append("  <h1>Ambien</h1>\n");
        sb.append("  <h2>Let's give you some REST!</h2>\n");

        if (null != message) {
            sb.append("<p>" + message);
        }

        sb.append("  <form action=\"/givemerest\" method=\"post\" enctype=\"multipart/form-data\" id=\"ambienform\">\n");
        sb.append("    <table border=\"1\">\n");
        sb.append("      <tr>\n");
        sb.append("        <td valign=\"top\"><label for=\"ddl\"><p>CQL Table DDL. <p>Multiple tables are okay, separate them with an empty line. <p>Do not provide keyspace DDL.</label></td>\n");
        sb.append("        <td><textarea name=\"ddl\" rows=\"20\" cols=\"40\">Enter DDL here</textarea></td>\n");
        sb.append("      </tr>\n");
        sb.append("      <tr>\n");
        sb.append("        <td valign=\"top\"><label for=\"packageName\">Package Name (defaults to hessian.ambien)</label></td>\n");
        sb.append("        <td><input type=\"text\" id=\"packageName\" name=\"packageName\"></td>\n");
        sb.append("      </tr>\n");
        sb.append("      <tr>\n");
        sb.append("        <td valign=\"top\"><label for=\"keyspace\">Target keyspace name</label></td>\n");
        sb.append("        <td><input type=\"text\" id=\"keyspace\" name=\"keyspace\" required></td>\n");
        sb.append("      </tr>\n");
        sb.append("    </table>\n");
        sb.append("    <div class=\"button\"><button type=\"submit\">Give me REST!</button></div>\n");
        sb.append("  </form>\n");
        sb.append("  <h2>Instructions</h2>\n");
        sb.append("  <ul>\n");
        sb.append("    <li>Save zip file locally</li>\n");
        sb.append("    <li>Unzip file</li>\n");
        sb.append("    <li>Change directory to the ambien directory</li>\n");
        sb.append("    <li>Edit ambien/src/main/resources/application.properties as appropriate (e.g., dse.contactPoints, dse.username, etc)</li>\n");
        sb.append("    <li>Build project with: mvn clean package</li>\n");
        sb.append("    <li>Start REST API with: java -jar target/*-0.0.1.SNAPSHOT.jar</li>\n");
        sb.append("    <li>Use the REST API</li>\n");
        sb.append("    <li>You can see the endpoint list at (assuming port 8222): http://localhost:8222</li>\n");
        sb.append("  </ul>\n");
        sb.append("  <h2>REST Easy!</h2>");


        sb.append("</body>\n</html>\n");

        return sb.toString();
    }

    @RequestMapping(value = "/givemerest", method = RequestMethod.POST, consumes = {"multipart/form-data"}, produces = "application/zip")
    @ResponseBody
    public byte[] giveMeRest(@RequestParam String ddl, @RequestParam String packageName, @RequestParam String keyspace) {
        init();
        Session session = staticSession.getCluster().newSession();
        File outputDir = null;
        try {
            outputDir = Files.createTempDirectory(ambienTmpPath, "ambientmp").toFile();
        }
        catch (IOException ioe) {
            throw new RuntimeException("ERROR: could not create temporary directory");
        }
        String kt_list = createTables(session, ddl, outputDir);
        AmbienParams params = new AmbienParams(kt_list, packageName, ENDPOINT_ROOT, outputDir.toString(),
                HOST, PORT, USERNAME, PASSWORD, HTTP_PORT,
                null/*TRUSTSTORE_PATH*/, null/*TRUSTSTORE_PASSWORD*/,
                null/*KEYSTORE_PATH*/, null/*KEYSTORE_PASSWORD*/);
        Ambien ambien = new Ambien();
        boolean success = ambien.makeRest(session, params, keyspace);
        byte[] zipped = null;
        if (success) {
            zipped = createZipOutputStream(outputDir);
        }
        FileSystemUtils.deleteRecursively(outputDir);
        session.closeAsync();

        return zipped;
    }

    private String createTables(Session session, String input, File outputDir) {
        StringBuilder kt_list = null;
        Scanner scanner = new Scanner(input);
        List<String> ddls = new ArrayList<String>();
        String cmd = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                if (!cmd.isEmpty()) {
                    ddls.add(cmd);
                    cmd = "";
                }
            }
            else
                cmd = cmd + " " + line;
        }
        if (!cmd.isEmpty())
            ddls.add(cmd);

        // create temporary keyspace
        String tmpKeyspace = randomString();
        SimpleStatement stmt = new SimpleStatement(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};", tmpKeyspace));
        ResultSet resultSet = session.execute(stmt);
        if (!resultSet.wasApplied()) {
            System.err.println("ERROR: could not create keyspace.  Bad CQL:\n" + stmt.getQueryString() + "\n");
            return null;
        }
        session.execute(String.format("USE %s", tmpKeyspace));

        // create tables
        FileWriter writer = null;
        try {
            File ddlfile = new File(outputDir, "ddl.cql");
            writer = new FileWriter(ddlfile);
            for (String ddl : ddls) {
                if (!ddl.substring(1, "CREATE TABLE".length()).equalsIgnoreCase("CREATE TABLE")) {
                    System.err.println("WARN: Only CREATE TABLE statements are allowed: " + ddl);
                    continue;
                }
                stmt = new SimpleStatement(ddl);
                resultSet = session.execute(stmt);
                if (!resultSet.wasApplied()) {
                    System.err.println("ERROR: could not create table.  Bad CQL:\n" + stmt.getQueryString() + "\n");
                    session.execute(String.format("DROP KEYSPACE %s;", tmpKeyspace));
                    return null;
                }
                writer.write(ddl + "\n");
            }
        }
        catch (IOException ioe) {
            throw new RuntimeException("Could not create ddl file");
        }

        // make ktlist
        for (TableMetadata tm : session.getCluster().getMetadata().getKeyspace(tmpKeyspace).getTables()) {
            if (null == kt_list) {
                kt_list = new StringBuilder();
            }
            else {
                kt_list.append(",");
            }
            kt_list.append(tmpKeyspace + "." + tm.getName());
        }

        return (null == kt_list) ? null : kt_list.toString();
    }

    private String randomString() {
        return String.format("ambien%08d", System.currentTimeMillis() % 100000000);
    }

    private List<File> listFiles(File dir) {
        List<File> files = new ArrayList<File>();
        if (null == dir)
            return files;
        if (!dir.isDirectory())
            return files;

        for (File file : dir.listFiles()) {
            files.add(file);
            if (file.isDirectory())
                files.addAll(listFiles(file));
        }
        return files;
    }
    private byte[] createZipOutputStream(File outputDir) {
        String base = "ambien/";
        List<File> files = listFiles(outputDir);
        URI outputDirURI = outputDir.toURI();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

        for (File file : files) {
            try {
                String name = base + outputDirURI.relativize(file.toURI()).getPath();
                if (file.isDirectory()) {
                    name = name + "/";
                    zipOutputStream.putNextEntry(new ZipEntry(name));
                }
                else {
                    zipOutputStream.putNextEntry(new ZipEntry(name));
                    FileInputStream fis = new FileInputStream(file);
                    StreamUtils.copy(fis, zipOutputStream);
                    fis.close();
                    zipOutputStream.closeEntry();
                }
            }
            catch (IOException ioe) {
                throw new RuntimeException("Could not zip file (" + file.getPath() + ")");
            }
        }
        if (null != zipOutputStream) {
            try {
                zipOutputStream.finish();
                zipOutputStream.flush();
                zipOutputStream.close();
                bufferedOutputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException ioe) {
                throw new RuntimeException("Could not finish/flush the zip output stream");
            }
        }

        return byteArrayOutputStream.toByteArray();
    }
}
