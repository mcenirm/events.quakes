package ed3.events.cap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/alerts")
public interface CAPConsumer {

    public static final String APPLICATION_CAP_XML = "application/cap+xml";

    /**
     * Post new alert
     */
    @POST
    @Consumes(APPLICATION_CAP_XML)
    Response newAlert(Alert alert);
}
