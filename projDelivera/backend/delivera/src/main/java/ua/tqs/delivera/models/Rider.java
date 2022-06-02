package ua.tqs.delivera.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "rider")
public class Rider{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long riderId;

    @Column
    private String name;

    @Column
    private String password;

    @Column
    private boolean available;

    @OneToOne
    @JoinColumn
    private Location currentLocation;

    @Column
    private int numberOfReviews;

    @Column
    private int sumOfReviews;

    public Rider(){}

    public Rider(String name, String password, boolean available, Location currentLocation, int numberOfReviews, int sumOfReviews){
        this.name = name;
        this.password = password;
        this.available = available;
        this.currentLocation=currentLocation;
        this.numberOfReviews=numberOfReviews;
        this.sumOfReviews = sumOfReviews;
    }

    public Long getRiderId(){
        return this.riderId;
    }

    public String getName(){
        return this.name;
    }

    public String getPassword(){
        return this.password;
    }

    public boolean isAvailable(){
        return this.available;
    }

    public Location getlLocation(){
        return this.currentLocation;
    }

    public int getNumberOfReviews(){
        return this.numberOfReviews;
    }

    public int getSumOfReviews(){
        return this.sumOfReviews;
    }

    public void setLocation(double lat, double lon){
        this.currentLocation.setLatitude(lat);
        this.currentLocation.setLongitude(lon);
    }
}