package com.example.demo.repository;


import com.example.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userName = :userName")
    boolean existsByUserName(@Param("userName") String userName);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    // 추가된 메서드
    Optional<User> findByRealnameAndBirthAndEmail(String realname, LocalDate birth, String email);

    // 사용자 아이디와 이메일을 함께 확인하는 메서드
    Optional<User> findByUserNameAndEmail(String userName, String email);
}
