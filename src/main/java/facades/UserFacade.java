package facades;

import dtos.UserDTO;
import entities.Role;
import entities.User;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import errorhandling.UserAlreadyExistsException;
import security.errorhandling.AuthenticationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lam@cphbusiness.dk
 */
public class UserFacade {

    private static EntityManagerFactory emf;
    private static UserFacade instance;

    private UserFacade() {
    }

    /**
     *
     * @param _emf
     * @return the instance of this facade.
     */
    public static UserFacade getUserFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserFacade();
        }
        return instance;
    }

    public User getVeryfiedUser(String username, String password) throws AuthenticationException {
        EntityManager em = emf.createEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
            if (user == null || !user.verifyPassword(password)) {
                throw new AuthenticationException("Invalid user name or password");
            }
        } finally {
            em.close();
        }
        return user;
    }

    public UserDTO createUser(String username, String password) throws UserAlreadyExistsException {
        User user = new User(username, password);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Role role = em.find(Role.class, "user");
            user.addRole(role);
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new UserAlreadyExistsException();
        } finally {
            em.close();
        }
        return new UserDTO(user);
    }

    public UserDTO updateUserScore(String username) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, username);
        try {
            em.getTransaction().begin();
            user.setScore(user.getScore()+1);
            if (user.getScore() > user.getHighscore()) user.setHighscore(user.getScore());
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new UserDTO(user);
    }

    public UserDTO resetScore(String username) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, username);
        try {
            em.getTransaction().begin();
            user.setScore(0L);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new UserDTO(user);
    }

    public List<UserDTO> readUserHighscores(int max, int start) {
        EntityManager em = emf.createEntityManager();
//        TypedQuery<User> query = em.createQuery("SELECT u.user_name, u.high_score FROM users u ORDER BY u.high_score DESC", User.class);
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u ORDER BY u.highscore DESC", User.class);
        query.setMaxResults(max);
        query.setFirstResult(start);
        List<User> users = query.getResultList();
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            userDTOs.add(new UserDTO(user));
        }
        return userDTOs;
    }
}
