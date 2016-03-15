package cz.muni.fi.pv168.gravemanager.backend;

import java.util.List;

/**
 * This class provides data access operations for {@link Grave} entity.
 *
 * @author petr.adamek@bilysklep.cz
 */
public interface GraveManager {

    /**
     * Stores new grave into database. Id for the new grave is automatically
     * generated and stored into id attribute.
     *
     * @param grave grave to be created.
     * @throws IllegalArgumentException when grave is null or grave has already
     * assigned id.
     * @throws ServiceFailureException when db operation fails.
     */
    void createGrave(Grave grave) throws ServiceFailureException;

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
     * @throws IllegalArgumentException when grave is null or grave has null id.
     * @throws ServiceFailureException when db operation fails or when given
     * grave does not exist.
     */
    void updateGrave(Grave grave) throws ServiceFailureException;

    /**
     * Deletes grave from database.
     *
     * @param grave grave to be deleted from db.
     * @throws IllegalArgumentException when grave is null or grave has null id.
     * @throws ServiceFailureException when db operation fails or when given
     * grave does not exist.
     */
    void deleteGrave(Grave grave) throws ServiceFailureException;

    /**
     * Returns list of all graves in the database.
     *
     * @return list of all graves in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Grave> findAllGraves() throws ServiceFailureException;

}
