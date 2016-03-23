package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.util.List;

/**
 * This service allows to manipulate with bodies.
 * 
 * @author Petr Adámek
 */
public interface BodyManager {
    
    /**
     * Stores new body into database. Id for the new body is automatically
     * generated and stored into id attribute.
     * 
     * @param body body to be created.
     * @throws IllegalArgumentException when body is null.
     * @throws IllegalEntityException when body has already assigned id.
     * @throws ValidationException when body breaks validation rules (name is
     * null, gender is null, born is not before died or born or died is in
     * future).
     * @throws ServiceFailureException when db operation fails.
     */
    void createBody(Body body) throws ServiceFailureException, ValidationException, IllegalEntityException;
    
    /**
     * Returns body with given id.
     * 
     * @param id primary key of requested body.
     * @return body with given id or null if such body does not exist.
     * @throws IllegalArgumentException when given id is null.
     * @throws ServiceFailureException when db operation fails.
     */
    Body getBody(Long id) throws ServiceFailureException;
    
    /**
     * Updates body in database.
     * 
     * @param body updated body to be stored into database.
     * @throws IllegalArgumentException when body is null.
     * @throws IllegalEntityException when body has null id or does not exist in the database
     * @throws ValidationException when body breaks validation rules (name is
     * null, gender is null, born is not before died or born or died is in
     * future).
     * @throws ServiceFailureException when db operation fails.
     */
    void updateBody(Body body) throws ServiceFailureException, ValidationException, IllegalEntityException;
    
    /**
     * Deletes body from database. 
     * 
     * @param body body to be deleted from db.
     * @throws IllegalArgumentException when body is null.
     * @throws IllegalEntityException when given has null id or does not exist in the database
     * @throws ServiceFailureException when db operation fails.
     */
    void deleteBody(Body body) throws ServiceFailureException, IllegalEntityException;
    
    /**
     * Returns list of all bodies in the database.
     * 
     * @return list of all bodies in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Body> findAllBodies() throws ServiceFailureException;
    
}
