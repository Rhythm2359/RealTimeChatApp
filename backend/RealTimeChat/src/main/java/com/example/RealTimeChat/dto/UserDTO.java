package com.example.RealTimeChat.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private boolean isOnline;
}
