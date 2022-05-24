package com.example.a1694;

public class Place {
    public String name, label, address, metro, time, site, photoUrl, id;
    Double first_coord, second_coord;
    Place(String name, String label, String address, Double first_coord, Double second_coord, String metro, String time, String site, String photoUrl, String id) {
        this.name = name;
        this.label = label;
        this.address = address;
        this.metro = metro;
        this.time = time;
        this.site = site;
        this.photoUrl = photoUrl;
        this.id = id;
        this.first_coord = first_coord;
        this.second_coord = second_coord;
    }
}
