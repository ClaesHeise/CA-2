package rest;

import entities.User;
import entities.Role;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

public class LoginEndpointTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;
    private static User u1, u2;
    
    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        
        httpServer.shutdownNow();
    }

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the EntityClass used below to use YOUR OWN (renamed) Entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            u1 = new User("user1", "password1");
            u2 = new User("user2", "password2");
            em.createNamedQuery("User.deleteAllRows").executeUpdate();
            em.createNamedQuery("Role.deleteAllRows").executeUpdate();
            Role userRole = new Role("user");
            Role adminRole = new Role("admin");
            u1.setScore(5L);
            u1.setHighscore(5L);
            u1.addRole(userRole);
            u2.addRole(userRole);
            em.persist(userRole);
            em.persist(adminRole);
            em.persist(u1);
            em.persist(u2);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    //This is how we hold on to the token after login, similar to that a client must store the token somewhere
    private static String securityToken;

    //Utility method to login and set the returned securityToken
    private static void login(String username, String password) {
        String json = String.format("{username: \"%s\", password: \"%s\"}", username, password);
        securityToken = given()
                .contentType("application/json")
                .body(json)
                //.when().post("/api/login")
                .when().post("/user/login")
                .then()
                .extract().path("token");
        //System.out.println("TOKEN ---> " + securityToken);
    }

    private void logOut() {
        securityToken = null;
    }

    @Test
    public void serverIsRunning() {
        given().when().get("/jokes").then().statusCode(200);
    }

    @Test
    public void testUsernameReturnedOnLogin() {
        String json = String.format("{username: \"%s\", password: \"%s\"}", u1.getUserName(), "password1");
        given()
                .contentType("application/json")
                .body(json)
                .when().post("/user/login")
                .then()
                .statusCode(200)
                .body("username", equalTo(u1.getUserName()));
    }

    @Test
    public void testCreateUser() {
        String json = String.format("{username: \"%s\", password: \"%s\"}", "user3", "password3");
        given()
                .contentType("application/json")
                .body(json)
                .when().post("/user/create")
                .then()
                .statusCode(200)
                .body("username", equalTo("user3"));
    }

    @Test
    public void testUserUpdateScore() {
        login("user1", "password1");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"isCorrect\": true}")
                .when().put("/info")
                .then()
                .statusCode(200)
                .body("score", equalTo(6));
    }

    @Test
    public void testUserUpdateScoreWithoutLogin() {
        given()
                .contentType("application/json")
                .when().put("/info")
                .then()
                .statusCode(403);
    }
}
