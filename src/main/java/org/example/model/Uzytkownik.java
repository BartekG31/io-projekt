package org.example.model;

public class Uzytkownik {
    private int id;
    private String login;
    private String haslo;
    private String imie;
    private String nazwisko;
    private String email;
    private int rolaId;

    public Uzytkownik() {}

    public Uzytkownik(int id, String login, String haslo, String imie, String nazwisko, String email, int rolaId) {
        this.id = id;
        this.login = login;
        this.haslo = haslo;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.email = email;
        this.rolaId = rolaId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getHaslo() { return haslo; }
    public void setHaslo(String haslo) { this.haslo = haslo; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getRolaId() { return rolaId; }
    public void setRolaId(int rolaId) { this.rolaId = rolaId; }
}
