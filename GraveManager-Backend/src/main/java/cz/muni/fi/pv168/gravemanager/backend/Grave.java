package cz.muni.fi.pv168.gravemanager.backend;

/**
 * This entity class represents Grave. Grave have some capacity, position 
 * specified with row and column, and it could have some note. One grave could 
 * contain zero or more bodies up to its capacity.
 * 
 * @author Petr Adámek
 */
public class Grave {
    
    private Long id;
    private int column;
    private int row;
    private int capacity;
    private String note;
    
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
    
    @Override
    public String toString() {
        return "Grave{" + "id=" + id + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Grave other = (Grave) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }    
    
}
