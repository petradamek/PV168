package cz.muni.fi.pv168.gravemanager.backend;

import java.time.LocalDate;
import java.util.Objects;

/**
 * This entity represents Body. Body has some name, date of born, date of death,
 * and flag, if it is vampire. Name and vampire are mandatory attributes, born
 * and died are optional.
 *
 * @author Petr Adámek
 */
public class Body {

    private Long id;
    private String name;
    private LocalDate born;
    private LocalDate died;
    private boolean vampire;

    public LocalDate getBorn() {
        return born;
    }

    public void setBorn(LocalDate born) {
        this.born = born;
    }

    public LocalDate getDied() {
        return died;
    }

    public void setDied(LocalDate died) {
        this.died = died;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVampire() {
        return vampire;
    }

    public void setVampire(boolean vampire) {
        this.vampire = vampire;
    }

    @Override
    public String toString() {
        return "Body{"
                + "id=" + id
                + ", name=" + name
                + ", born=" + born
                + ", died=" + died
                + ", vampire=" + vampire
                + '}';
    }

    /**
     * Returns true if obj represents the same body. Two objects are considered
     * to represent the same body when both are instances of {@link Body} class,
     * both have assigned some id and this id is the same.
     *
     *
     * @param obj the reference object with which to compare.
     * @return true if obj represents the same body.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Body other = (Body) obj;
        if (obj != this && this.id == null) {
            return false;
        }
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}
