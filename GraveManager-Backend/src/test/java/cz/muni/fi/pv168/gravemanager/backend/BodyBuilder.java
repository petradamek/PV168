package cz.muni.fi.pv168.gravemanager.backend;

import java.time.LocalDate;
import java.time.Month;

/**
 * This is builder for the {@link Body} class to make tests better readable.
 *
 * @author petr.adamek@bilysklep.cz
 */
public class BodyBuilder {

    private Long id;
    private String name;
    private LocalDate born;
    private LocalDate died;
    private boolean vampire;

    public BodyBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public BodyBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BodyBuilder born(LocalDate born) {
        this.born = born;
        return this;
    }

    public BodyBuilder born(int year, Month month, int day) {
        this.born = LocalDate.of(year, month, day);
        return this;
    }

    public BodyBuilder died(LocalDate died) {
        this.died = died;
        return this;
    }

    public BodyBuilder died(int year, Month month, int day) {
        this.died = LocalDate.of(year, month, day);
        return this;
    }

    public BodyBuilder vampire(boolean vampire) {
        this.vampire = vampire;
        return this;
    }

    /**
     * Creates new instance of {@link Body} with configured properties.
     *
     * @return new instance of {@link Body} with configured properties.
     */
    public Body build() {
        Body body = new Body();
        body.setId(id);
        body.setName(name);
        body.setBorn(born);
        body.setDied(died);
        body.setVampire(vampire);
        return body;
    }
}
