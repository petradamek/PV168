package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import java.util.List;

/**
 * This service allows to manipulate with associations between graves and 
 * bodies.
 * 
 * @author Petr Adámek
 */
public interface CemeteryManager {
    
    /**
     * Find grave that contains given body. If the body is not placed 
     * in any grave, null is returned.
     * 
     * @param body body that we want to find
     * @return grave that contains given body or null if there is no such grave
     * @throws IllegalArgumentException when body is null.
     * @throws IllegalEntityException when given body has null id 
     * @throws ServiceFailureException when db operation fails
     */
    Grave findGraveWithBody(Body body) throws ServiceFailureException, IllegalEntityException;
    
    /**
     * Find all bodies that are placed in given grave.
     * 
     * @param grave grave that we want to search
     * @return collection of bodies placed in given grave
     * @throws IllegalArgumentException when grave is null
     * @throws IllegalEntityException when given grave has null id 
     * @throws ServiceFailureException when db operation fails
     */
    List<Body> findBodiesInGrave(Grave grave) throws ServiceFailureException, IllegalEntityException;
    
    /**
     * Find all bodies that are not placed in any grave. 
     * 
     * @return collection of all unburied bodies
     * @throws ServiceFailureException when db operation fails.
     */
    List<Body> findUnburiedBodies() throws ServiceFailureException;

    /**
     * Find all graves that contain no body. 
     * 
     * @return collection of all empty graves
     * @throws ServiceFailureException when db operation fails.
     */
    List<Grave> findEmptyGraves() throws ServiceFailureException;

    /**
     * Find all graves that are not full.
     * 
     * @return collection of all empty graves
     * @throws ServiceFailureException when db operation fails.
     */
    List<Grave> findGravesWithSomeFreeSpace() throws ServiceFailureException;

    /**
     * Inserts body into given grave.
     * 
     * @param body body to be placed to given grave
     * @param grave grave for placing given body
     * @throws IllegalArgumentException when body or grave is null
     * @throws IllegalEntityException when body is already placed in some grave,
     * when grave is already full or when body or grave have null id or do 
     * not exist in database 
     * @throws ServiceFailureException when db operation fails.
     */
    void putBodyIntoGrave(Body body, Grave grave) throws ServiceFailureException, IllegalEntityException;
    
    /**
     * Removes body from given grave.
     * 
     * @param body body to be removed from given grave
     * @param grave grave for removing given body
     * @throws IllegalArgumentException when body or grave is null
     * @throws IllegalEntityException when given body is not placed in given 
     * grave or when body or grave have null id or do not exist in database
     * @throws ServiceFailureException when db operation fails.
     */
    void removeBodyFromGrave(Body body, Grave grave) throws ServiceFailureException, IllegalEntityException;    
    
}
