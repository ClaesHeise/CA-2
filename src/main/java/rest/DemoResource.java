package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import dtos.UserDTO;
import entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import errorhandling.API_Exception;
import errorhandling.GenericExceptionMapper;
import facades.UserFacade;
import security.errorhandling.AuthenticationException;
import utils.EMF_Creator;

/**
 * @author lam@cphbusiness.dk
 */
@Path("info")
public class DemoResource {
    
    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();
    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    public static final UserFacade USER_FACADE = UserFacade.getUserFacade(EMF);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response updateUser(String jsonString) throws API_Exception {
        String username = securityContext.getUserPrincipal().getName();
        boolean isCorrect;
        try {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            isCorrect = json.get("isCorrect").getAsBoolean();
        } catch (Exception e) {
            throw new API_Exception("Malformed JSON Suplied",400,e);
        }
        UserDTO userDTO;
        if (isCorrect) {
            userDTO = USER_FACADE.updateUserScore(username);
        } else {
            userDTO = USER_FACADE.resetScore(username);
        }
        return Response.ok().entity(GSON.toJson(userDTO)).build();
    }

    @GET
    @Path("highscores")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response highscores(@QueryParam("max") int max, @QueryParam("start") int start) {
        if (max <= 0) max = 100;
        return Response.ok().entity(GSON.toJson(USER_FACADE.readUserHighscores(max, start))).build();
    }
}