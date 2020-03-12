package hessian.ambien.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import hessian.ambien.web.AmbienController;

public class AmbienHelloWorld implements RequestHandler<String,String> {
    private static AmbienController ambienController = new AmbienController();

    @Override
    public String handleRequest(String input, Context context) {
        return ambienController.webPage("");
    }
}
