package cz.muni.fi.pv168.gravemanager.backend;

import java.util.Objects;

/**
 * This entity class represents Grave. Grave have some capacity, position
 * specified with row and column, and it could have some note. One grave could
 * contain zero or more bodies up to its capacity. All parameters except of note
 * are mandatory.
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
        return "Grave{"
                + "id=" + id
                + ", column=" + column
                + ", row=" + row
                + ", capacity=" + capacity
                + ", note=" + note
                + '}';
    }

    /**
     * Returns true if obj represents the same grave. Two objects are considered
     * to represent the same grave when both are instances of {@link Grave}
     * class, both have assigned some id and this id is the same.
     *
     *
     * @param obj the reference object with which to compare.
     * @return true if obj represents the same grave.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Grave other = (Grave) obj;
        if (obj != this && this.id == null) {
            return false;
        }
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

}
