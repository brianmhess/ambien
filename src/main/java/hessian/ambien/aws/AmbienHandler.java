package hessian.ambien.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import hessian.ambien.web.AmbienController;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Base64;
import java.util.stream.Collectors;

public class AmbienHandler implements RequestStreamHandler {
    private static AmbienController ambienController = new AmbienController();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
    throws IOException {
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String lines = reader.lines().collect(Collectors.joining());
        PrintStream printer = new PrintStream(outputStream);
        context.getLogger().log("lines: " + lines);

        try {
            JSONObject bodyObject = (JSONObject)parser.parse(lines);
            String ddl = (String)bodyObject.get("ddl");
            String packageName = (String)bodyObject.get("packageName");
            String keyspace = (String)bodyObject.get("keyspace");
            if (null == ddl) {
                printer.print(ambienController.webPage("<p><font color=\"red\">ERROR: must specify ddl</font>"));
                return;
            }
            byte[] zip = ambienController.giveMeRest(ddl, packageName, keyspace);
            // base64 encode byte stream
            OutputStream encodedStream = Base64.getEncoder().wrap(outputStream);
            encodedStream.write(zip);
            encodedStream.close();
        }
        catch (ParseException pex) {
            printer.println("ERROR: parse exception: " + pex.getMessage());
            printer.println("lines: " + lines);
        }

    }
/*
public class AmbienHandler {
    private static AmbienController ambienController = new AmbienController();

    public String webPageHandler(String input, Context context) {
        return "Hello World!";
        //return ambienController.webPage("");
    }

    /*
    public byte[] giveMeRest(AmbienForm input, Context context) {
        return ambienController.giveMeRest(input.getDdl(), input.getPackageName(), input.getKeyspace());
    }

    public class AmbienForm {
        private String ddl;
        private String packageName;
        private String keyspace;

        public AmbienForm() {
        }

        public AmbienForm(String ddl, String packageName, String keyspace) {
            this.ddl = ddl;
            this.packageName = packageName;
            this.keyspace = keyspace;
        }

        public String getDdl() {
            return ddl;
        }

        public void setDdl(String ddl) {
            this.ddl = ddl;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getKeyspace() {
            return keyspace;
        }

        public void setKeyspace(String keyspace) {
            this.keyspace = keyspace;
        }
    }
    */
}
