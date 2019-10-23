package com.example.chatcomgps;

import android.graphics.Bitmap;
import android.media.Image;

import java.util.Date;

class Mensagem implements Comparable<Mensagem>{

    @Override
    public int compareTo(Mensagem mensagem) {
        return this.data.compareTo(mensagem.data);
    }

    private String texto;
    private Date data;
    private String email;
    private String location;

    public Mensagem(Date data, String email, String location) {
        this.data = data;
        this.email = email;
        this.location = location;
    }

    public Mensagem(String texto, Date data, String email) {
        this.texto = texto;
        this.data = data;
        this.email = email;
    }


    public Mensagem(){
    }


    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

