package com.example.RealTimeChat.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    @Column(unique = true , nullable = false)
    private String username ;

    @Column(unique = true , nullable = false)
    private String email ;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false , name="is_online")
    private boolean online ;

}
