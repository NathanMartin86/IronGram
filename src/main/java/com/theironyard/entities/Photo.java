package com.theironyard.entities;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Created by macbookair on 11/17/15.
 */
@Entity
@Table (name="photos")
public class Photo {
    @Id
    @GeneratedValue
    @Column (nullable = false)
    public int id;


    public LocalDateTime accessTime;

    public boolean isPublic;

    @Column (nullable = false)
    public int userInput;

    @ManyToOne
    public User sender;

    @ManyToOne
    public User receiver;

    @Column(nullable = false)
    public String filename;



}

