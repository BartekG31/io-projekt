package org.example.model;

import java.sql.Date;

public class Zlecenie {
    private int id;
    private int nadawcaId;
    private int odbiorcaId;
    private int pojazdId;
    private String status;
    private Date dataUtworzenia;

    public Zlecenie() {}

    public Zlecenie(int id, int nadawcaId, int odbiorcaId, int pojazdId, String status, Date dataUtworzenia) {
        this.id = id;
        this.nadawcaId = nadawcaId;
        this.odbiorcaId = odbiorcaId;
        this.pojazdId = pojazdId;
        this.status = status;
        this.dataUtworzenia = dataUtworzenia;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNadawcaId() { return nadawcaId; }
    public void setNadawcaId(int nadawcaId) { this.nadawcaId = nadawcaId; }

    public int getOdbiorcaId() { return odbiorcaId; }
    public void setOdbiorcaId(int odbiorcaId) { this.odbiorcaId = odbiorcaId; }

    public int getPojazdId() { return pojazdId; }
    public void setPojazdId(int pojazdId) { this.pojazdId = pojazdId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDataUtworzenia() { return dataUtworzenia; }
    public void setDataUtworzenia(Date dataUtworzenia) { this.dataUtworzenia = dataUtworzenia; }
}
