package hessian.ambien;

import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
import com.datastax.oss.driver.shaded.guava.common.io.Files;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class AmbienController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String webForm() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n");
        sb.append("  <head>\n" +
                "      <meta charset=\"utf-8\">\n" +
                "      <title>Ambien... it gives you REST</title>\n" +
                "    </head>");
        sb.append("<body>\n" +
                "<hr>");

        sb.append("  <h1>Ambien... it gives you REST</h1>\n");
        sb.append("  <form action=\"/\" method=\"post\" enctype=\"multipart/form-data\">\n");

        sb.append("  <table>\n");
        sb.append("    <tr>\n");
        sb.append("      <td><label for=\"apolloBundle\">Cloud Secure Connect Bundle:</label></td>");
        sb.append("      <td><input type=\"file\" id=\"apolloBundle\" name=\"apolloBundle\" required></td>");
        sb.append("      <td></td>");
        sb.append("    </tr>\n");
        sb.append("    <tr>\n");
        sb.append("      <td><label for=\"username\">Username:</label></td>");
        sb.append("      <td><input type=\"text\" id=\"username\" name=\"username\" required></td>");
        sb.append("      <td></td>");
        sb.append("    </tr>\n");
        sb.append("    <tr>\n");
        sb.append("      <td><label for=\"password\">Password:</label></td>");
        sb.append("      <td><input type=\"text\" id=\"password\" name=\"password\" required></td>");
        sb.append("      <td></td>");
        sb.append("    </tr>\n");
        sb.append("    <tr>\n");
        sb.append("      <td><label for=\"keyspaceTableList\">Keyspace/Table List:</label></td>");
        sb.append("      <td><input type=\"text\" id=\"keyspaceTableList\" name=\"keyspaceTableList\" required></td>");
        sb.append("      <td><font style=\"font-family:courier;\">ks1.tbl1[,ks2.tbl2,...ks.tbl]</font></td>");
        sb.append("    </tr>\n");
        sb.append("    <tr>\n");
        sb.append("      <td><label for=\"httpPort\">HTTP Port:</label></td>");
        sb.append("      <td><input type=\"text\" id=\"httpPort\" name=\"httpPort\" value=\"8222\"></td>");
        sb.append("      <td></td>");
        sb.append("    </tr>\n");
        sb.append("    </tr>\n");
        sb.append("    <tr>\n");
        sb.append("      <td><label for=\"endpointRoot\">Endpont Root:</label></td>");
        sb.append("      <td><input type=\"text\" id=\"endpointRoot\" name=\"endpointRoot\" value=\"api/$keyspace/$table\"></td>");
        sb.append("      <td>REST endpoint root. Use <font style=\"font-family:courier;\">$keyspace</font> for the keyspace name, <font style=\"font-family:courier;\">$table</font> for the table name</td>");
        sb.append("    </tr>\n");
        sb.append("    </tr>\n");
        sb.append("    <tr>\n");
        sb.append("      <td><label for=\"packageName\">Package Name:</label></td>");
        sb.append("      <td><input type=\"text\" id=\"packageName\" name=\"packageName\" value=\"hessian.ambien\"></td>");
        sb.append("      <td>Java package for generated code.</td>");
        sb.append("    </tr>\n");
        sb.append("    </tr>\n");
        sb.append("    </table>\n");

        sb.append("    <div class=\"button\"><button type=\"submit\">Give me REST!</button></div>\n");
        sb.append("  </form>\n");

        sb.append("  <h3>Instructions:</h3>\n");
        sb.append("  <p>Fill in this form with connection details and a list of keyspace/tables and Ambien" +
                " will give you REST - get it &#128539;\n");
        sb.append("  <p>Ambien will produce a Spring Boot application that contains a REST API for all the" +
                " specified tables - Add, Delete, Select.  Selections by partition key(s), partition key(s) and " +
                " clustering key(s) (some keys, all keys, equality, inequality, etc), partition key(s) and clustering" +
                " keys and regular columns (with ALLOW FILTERING).  The idea is that you can then modify the generated" +
                " code to suit your needs (add new methods, remove some methods, edit some methods - whatever is useful).\n");
        sb.append("  <p>The generated project will include the credentials in the project, so you are ready to go.\n");
        sb.append("  <p>When you are happy, simply run <font style=\"font-family:courier;\">mvn clean package</font> and then" +
                " run via <font style=\"font-family:courier;\">java -jar target/ambien-0.0.1-SNAPSHOT.jar</font>." +
                " By default it will run on <font style=\"font-family:courier;\">http://localhost:8222/</font>.\n");


        sb.append("</body>\n</html>\n");

        return sb.toString();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = {"multipart/form-data"}, produces = "application/zip")
    public byte[] ambienZipFromForm(@RequestParam String username,
                                    @RequestParam String password,
                                    @RequestParam String keyspaceTableList,
                                    @RequestParam(required = false) String httpPort,
                                    @RequestParam(required = false) String endpointRoot,
                                    @RequestParam(required = false) String packageName,
                                    @RequestParam MultipartFile apolloBundle,
                                    HttpServletResponse response) {
        File bundleFile = null;
        File outputDir = null;
        try {
            outputDir = Files.createTempDir();
            bundleFile = File.createTempFile("dsecscb", ".zip");
            FileOutputStream fos = new FileOutputStream(bundleFile);
            fos.write(apolloBundle.getBytes());
            fos.close();
        }
        catch (IOException ioe) {
            bundleFile.delete();
            throw new RuntimeException("Could not create temporary bundle file");
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"ambien.zip\"");

        List<String> argsList = new ArrayList<String>();
        argsList.addAll(Lists.newArrayList(AmbienParams.outputDirOption, outputDir.getAbsolutePath()));
        argsList.addAll(Lists.newArrayList(AmbienParams.usernameOption, username));
        argsList.addAll(Lists.newArrayList(AmbienParams.passwordOption, password));
        argsList.addAll(Lists.newArrayList(AmbienParams.apolloBundleOption, bundleFile.getPath()));
        argsList.addAll(Lists.newArrayList(AmbienParams.ktlistOption, keyspaceTableList));
        if (!httpPort.isEmpty()) {
            argsList.addAll(Lists.newArrayList(AmbienParams.httpPortOption, httpPort));
        }
        if (!endpointRoot.isEmpty()) {
            argsList.addAll(Lists.newArrayList(AmbienParams.endpointRootOption, endpointRoot));
        }
        if (!packageName.isEmpty()) {
            argsList.addAll(Lists.newArrayList(AmbienParams.packageNameOption, packageName));
        }

        Ambien ambien = new Ambien();
        try {
            if (ambien.run(argsList.toArray(new String[0]))) {
                return createZipOutputStream(outputDir);
            }
            else
                throw new RuntimeException("Ambien returned false");
        }
        catch (IOException ioe) {
            throw new RuntimeException("Ambien had an IOException");
        }
        finally {
            FileSystemUtils.deleteRecursively(outputDir);
            bundleFile.delete();
        }
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
