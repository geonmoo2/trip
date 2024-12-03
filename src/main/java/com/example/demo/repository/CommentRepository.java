package com.example.demo.repository;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByBoardEntityOrderByIdDesc(BoardEntity boardEntity);
    void deleteByBoardEntity_Id(Long id);
}
