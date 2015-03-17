package ass.prochka6.model;

import java.io.Serializable;

/**
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class IdEntity implements Serializable {

    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
