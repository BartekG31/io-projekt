package org.example.model;

public class Uzytkownik {
    private int id;
    private String imie;
    private String nazwisko;
    private String login;
    private String haslo;
    private String rola;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getHaslo() { return haslo; }
    public void setHaslo(String haslo) { this.haslo = haslo; }

    public String getRola() { return rola; }
    public void setRola(String rola) { this.rola = rola; }
}
