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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getInfoForAll() {
        return "{\"msg\":\"Hello anonymous\"}";
    }

    //Just to verify if the database is setup
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public String allUsers() {

        EntityManager em = EMF.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery ("select u from User u",entities.User.class);
            List<User> users = query.getResultList();
            return "[" + users.size() + "]";
        } finally {
            em.close();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user")
    @RolesAllowed("user")
    public String getFromUser() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to User: " + thisuser + "\"}";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("admin")
    @RolesAllowed("admin")
    public String getFromAdmin() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to (admin) User: " + thisuser + "\"}";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(String jsonString) throws API_Exception {
        String username;
        String password;
        try {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            username = json.get("username").getAsString();
            password = json.get("password").getAsString();
        } catch (Exception e) {
            throw new API_Exception("Malformed JSON Suplied",400,e);
        }
        return Response.ok().entity(USER_FACADE.createUser(username, password)).build();

//        try {
//            User user = USER_FACADE.getVeryfiedUser(username, password);
//            String token = createToken(username, user.getRolesAsStrings());
//            JsonObject responseJson = new JsonObject();
//            responseJson.addProperty("username", username);
//            responseJson.addProperty("token", token);
//            return Response.ok(new Gson().toJson(responseJson)).build();
//
//        } catch (JOSEException | AuthenticationException ex) {
//            if (ex instanceof AuthenticationException) {
//                throw (AuthenticationException) ex;
//            }
//            Logger.getLogger(GenericExceptionMapper.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

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
    public Response highscores(String jsonString) {
        int max;
        try {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            max = json.get("max").getAsInt();
        } catch (IllegalStateException e) {
            max = 100;
        }
        //if (max ) max = 100;
        return Response.ok().entity(GSON.toJson(USER_FACADE.readUserHighscores(max))).build();
    }
}