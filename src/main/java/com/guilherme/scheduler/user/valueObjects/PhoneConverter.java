package com.guilherme.scheduler.user.valueObjects;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PhoneConverter implements AttributeConverter<Phone, String> {

    @Override
    public String convertToDatabaseColumn(Phone phone) {
        if(phone == null) return null;
        return phone.number();
    }

    @Override
    public Phone convertToEntityAttribute(String phone) {
        if(phone == null) return null;
        return new Phone(phone);
    }
}
