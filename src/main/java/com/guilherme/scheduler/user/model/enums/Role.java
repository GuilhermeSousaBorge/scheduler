package com.guilherme.scheduler.user.model.enums;

public enum Role {
    CLIENT, ATTENDANT, ADMIN;


    public boolean canViewOtherProfiles(){
        return switch(this){
            case ADMIN, ATTENDANT -> true;
            case CLIENT -> false;
        };
    }
}
