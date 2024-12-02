package com.example.demo.repository;


import com.example.demo.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 최신 리뷰부터 내림차순 정렬하여 모든 리뷰 가져오기
    List<Review> findAllByOrderByIdDesc();

    // 특정 contentId에 대한 리뷰를 내림차순으로 가져오기
    List<Review> findByContentIdOrderByIdDesc(Long contentId);

    // 특정 contentId에 대한 모든 리뷰 가져오기
    List<Review> findByContentId(Long contentId);

    // 리뷰에 존재하는 모든 contentId 가져오기 (중복 제거)
    @Query("SELECT DISTINCT r.contentId FROM Review r")
    List<Long> findAllContentIds();

    // 모든 contentId와 평균 평점 가져오기
    @Query("SELECT r.contentId, AVG(r.rating) FROM Review r GROUP BY r.contentId")
    List<Object[]> findAllContentAverageRatings();
}
