package ru.ssau.sanya.mettings.Entity;


public class Member {
    private String fio;
    private String position;

    public Member() {
    }

    public Member( String fio, String position) {
        this.fio = fio;
        this.position = position;
    }



    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
