package com.renu.mail.repositories;

import org.springframework.data.repository.CrudRepository;

import com.renu.mail.models.ConfirmationToken;

public interface ConfirmationTokenRepository extends CrudRepository<ConfirmationToken, String> {  
    ConfirmationToken findByConfirmationToken(String confirmationToken);
}