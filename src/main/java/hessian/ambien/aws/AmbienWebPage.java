package hessian.ambien.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import hessian.ambien.web.AmbienController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class AmbienWebPage implements RequestStreamHandler {
    private static AmbienController ambienController = new AmbienController();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        PrintStream writer = new PrintStream(outputStream);
        writer.println(ambienController.webPage(""));
    }
}
