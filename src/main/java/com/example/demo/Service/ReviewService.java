package com.example.demo.Service; // 패키지 이름 소문자로 변경

import com.example.demo.Entity.Review;
import com.example.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // 최신 리뷰부터 ID 기준으로 내림차순 정렬하여 모든 리뷰 가져오기
    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByIdDesc();
    }

    // 특정 contentId에 대한 리뷰 가져오기
    public List<Review> getReviewsByContentId(Long contentId) {
        return reviewRepository.findByContentIdOrderByIdDesc(contentId);
    }

    // 모든 리뷰의 평균 평점 계산
    public double getAverageRating() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // 특정 contentId에 대한 평균 평점 계산
    public double getAverageRatingByContentId(Long contentId) {
        List<Review> reviews = reviewRepository.findByContentId(contentId);
        if (reviews.isEmpty()) {
            return 0.0; // 리뷰가 없을 경우 0 반환
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // 모든 콘텐츠의 평균 평점 계산 (contentId -> 평균 평점 Map 반환)
    public Map<Long, Double> getAllAverageRatings() {
        return reviewRepository.findAllContentAverageRatings().stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],  // contentId
                        result -> (Double) result[1] // 평균 평점
                ));
    }
    // 로그인 정보 review 에 저장
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // 로그인된 사용자의 아이디 반환
        }
        return null; // 비로그인 상태
    }

    // 리뷰 저장
    public Review saveReview(Review review) {
        String username = getCurrentUsername(); // 로그인된 사용자 아이디 가져오기
        if (username == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        review.setUsername(username); // 작성자 설정
        return reviewRepository.save(review);
    }
    public void deleteReviewById(Long reviewId) {
        // 리뷰 삭제
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
        } else {
            throw new IllegalArgumentException("해당 ID의 리뷰가 존재하지 않습니다: " + reviewId);
        }
    }
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
    }
}
