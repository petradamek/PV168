package cz.muni.fi.pv168.gravemanager.backend;

/**
 * This is builder for the {@link Grave} class to make tests better readable.
 *
 * @author petr.adamek@bilysklep.cz
 */
public class GraveBuilder {

    private Long id;
    private int column;
    private int row;
    private int capacity = 1;
    private String note;

    public GraveBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public GraveBuilder column(int column) {
        this.column = column;
        return this;
    }

    public GraveBuilder row(int row) {
        this.row = row;
        return this;
    }

    public GraveBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public GraveBuilder note(String note) {
        this.note = note;
        return this;
    }

    public Grave build() {
        Grave grave = new Grave();
        grave.setId(id);
        grave.setColumn(column);
        grave.setRow(row);
        grave.setCapacity(capacity);
        grave.setNote(note);
        return grave;
    }
}
