package cz.muni.fi.pv168.gravemanager.backend;

import java.sql.Date;

/**
 * This entity represents Body. Body has some name, date of born, date of death,
 * and flag, if it is vampire.
 * 
 * @author Petr Adámek
 */
public class Body {
 
    private Long id;
    private String name;
    private java.sql.Date born;
    private java.sql.Date died;
    private boolean vampire;

    public Date getBorn() {
        return born;
    }

    public void setBorn(Date born) {
        this.born = born;
    }

    public Date getDied() {
        return died;
    }

    public void setDied(Date died) {
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
        return "Body{" + "id=" + id + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Body other = (Body) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
        
}
