package com.renu.mail.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.renu.mail.models.User;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, String> {  
    User findByEmailIdIgnoreCase(String emailId);
}