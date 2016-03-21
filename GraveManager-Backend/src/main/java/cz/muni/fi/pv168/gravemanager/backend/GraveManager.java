package cz.muni.fi.pv168.gravemanager.backend;

import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.util.List;

/**
 * This service allows to manipulate with graves.
 * 
 * @author Petr Adámek
 */
public interface GraveManager {
    
    /**
     * Stores new grave into database. Id for the new grave is automatically
     * generated and stored into id attribute.
     * 
     * @param grave grave to be created.
     * @throws IllegalArgumentException when grave is null, or grave has already 
     * assigned id.
     * @throws ValidationException when grave breaks validation rules.
     * @throws IllegalEntityException when grave has already assigned id.
     * @throws ServiceFailureException when db operation fails.
     */
    void createGrave(Grave grave) throws ServiceFailureException, ValidationException, IllegalEntityException;
    
    /**
     * Returns grave with given id.
     * 
     * @param id primary key of requested grave.
     * @return grave with given id or null if such grave does not exist.
     * @throws IllegalArgumentException when given id is null.
     * @throws ServiceFailureException when db operation fails.
     */
    Grave getGrave(Long id) throws ServiceFailureException;
    
    /**
     * Updates grave in database.
     * 
     * @param grave updated grave to be stored into database.
     * @throws IllegalArgumentException when grave is null, or grave has null id.
     * @throws ValidationException when grave breaks validation rules.
     * @throws IllegalEntityException when grave has null id or does not exist in the database
     * @throws ServiceFailureException when db operation fails.
     */
    void updateGrave(Grave grave) throws ServiceFailureException, ValidationException, IllegalEntityException;
    
    /**
     * Deletes grave from database. 
     * 
     * @param grave grave to be deleted from db.
     * @throws IllegalArgumentException when grave is null.
     * @throws IllegalEntityException when grave has null id or does not exist in the database
     * @throws ServiceFailureException when db operation fails.
     */
    void deleteGrave(Grave grave) throws ServiceFailureException, IllegalEntityException;
    
    /**
     * Returns list of all graves in the database.
     * 
     * @return list of all graves in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Grave> findAllGraves() throws ServiceFailureException;
    
}
