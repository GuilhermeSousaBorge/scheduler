package com.guilherme.scheduler.user.repository;

import com.guilherme.scheduler.user.model.User;
import com.guilherme.scheduler.user.model.valueObjects.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPhone(Phone phone);
}
