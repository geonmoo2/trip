package com.example.demo.repository;

import com.example.demo.Entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    @Modifying
    @Query("update BoardEntity b set b.boardHits = b.boardHits + 1 where b.id = :id")
    void updateHits(@Param("id") Long id);

    Page<BoardEntity> findByBoardTitleContainingOrderByIdDesc(String query, Pageable pageable);

    Page<BoardEntity> findByBoardWriterContainingOrderByIdDesc(String query, Pageable pageable);

    Page<BoardEntity> findByBoardContentsContainingOrderByIdDesc(String query, Pageable pageable);

    Page<BoardEntity> findAllByOrderByIdDesc(Pageable pageable);

}
