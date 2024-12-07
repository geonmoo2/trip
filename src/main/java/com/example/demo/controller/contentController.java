package com.example.demo.controller;

import com.example.demo.Entity.Review;
import com.example.demo.Service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/content")
public class contentController {

    @Autowired
    private ReviewService reviewService;


    @GetMapping("/region")
    public String getRegion(Model model) {
        try {
            // 평균 별점 데이터 가져오기
            Map<Long, Double> averageRatings = reviewService.getAllAverageRatings();

            // 모델에 데이터 추가
            model.addAttribute("averageRatings", averageRatings); // 키 이름 수정

            log.info("Region page loaded with average ratings: {}", averageRatings);
            return "region";

        } catch (Exception e) {
            log.error("Error loading region page: {}", e.getMessage(), e);
            return "error";
        }
    }

    // 메인 페이지
    @GetMapping("/main{mainId}")
    public String getMainPage(@PathVariable("mainId") String mainId, Model model) {
        try {
            log.info("Fetching main page for mainId: {}", mainId);

            // mainId에서 숫자 추출 (예: main1 → 1)
            Long numericMainId = Long.parseLong(mainId.replaceAll("[^0-9]", ""));

            // contentId 계산 로직: contentId = 5 * (mainId - 1) + 1
            Long contentId = 5 * (numericMainId - 1) + 1;

            // 평균 별점 및 리뷰 데이터 가져오기
            Map<Long, Double> averageRatings = reviewService.getAllAverageRatings();
            List<Review> contentReviews = reviewService.getReviewsByContentId(contentId);
            double contentAverageRating = reviewService.getAverageRatingByContentId(contentId);

            // 모델에 데이터 추가
            model.addAttribute("averageRatings", averageRatings);
            model.addAttribute("contentReviews", contentReviews);
            model.addAttribute("contentAverageRating", contentAverageRating);
            model.addAttribute("mainId", numericMainId);

            log.info("Content reviews size for mainId {}: {}", numericMainId, contentReviews.size());
            return "main" + numericMainId; // templates/main/main1.html 등
        } catch (Exception e) {
            log.error("Error loading main page for mainId {}: {}", mainId, e.getMessage(), e);
            return "error";
        }
    }

    // 특정 콘텐츠 페이지
    @GetMapping("/content{contentId}")
    public String getContentPage(@PathVariable("contentId") String contentId,
                                 Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Fetching content page for contentId: {}", contentId);
            log.info("UserDetails: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");

            // contentId에서 숫자 부분 추출
            Long numericContentId = Long.parseLong(contentId.replaceAll("[^0-9]", ""));
            List<Review> reviews = reviewService.getReviewsByContentId(numericContentId);
            double averageRating = reviewService.getAverageRatingByContentId(numericContentId);

            if (userDetails != null) {
                model.addAttribute("user", userDetails);
            } else {
                model.addAttribute("user", null);
            }

            model.addAttribute("reviews", reviews);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("contentId", numericContentId);

            return "content/content" + contentId; // 템플릿 경로
        } catch (Exception e) {
            log.error("Error loading content page for contentId {}: {}", contentId, e.getMessage(), e);
            return "error";
        }
    }

    // 리뷰 제출
    @PostMapping("/content{contentId}")
    public String submitReview(@PathVariable("contentId") Long contentId, @ModelAttribute Review review, Model model) {
        try {
            if (review.getRating() < 1 || review.getRating() > 5) {
                log.warn("Invalid rating value: {}", review.getRating());
                model.addAttribute("errorMessage", "별점은 1에서 5 사이의 값이어야 합니다.");
                return "content";
            }
            review.setContentId(contentId); // 리뷰에 contentId 설정
            reviewService.saveReview(review);
            log.info("New review submitted for content {}: {}", contentId, review);
        } catch (Exception e) {
            log.error("Error submitting review for content {}: ", contentId, e);
            model.addAttribute("errorMessage", "리뷰 저장 중 오류가 발생했습니다.");
            return "error";
        }
        return "redirect:/content/content" + contentId;
    }
    @GetMapping("/content{contentId}/average-rating")
    @ResponseBody
    public Map<String, Object> getAverageRating(@PathVariable Long contentId) {
        double averageRating = reviewService.getAverageRatingByContentId(contentId);
        log.info("averageRating for content {}: {}", contentId, averageRating); // 로그 출력
        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", averageRating);
        return response;
    }
    @DeleteMapping("/content/{contentId}/review/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable("contentId") Long contentId,
                                               @PathVariable("reviewId") Long reviewId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Attempting to delete review. ContentId: {}, ReviewId: {}, User: {}",
                    contentId, reviewId, userDetails.getUsername());

            String loggedInUserName = userDetails.getUsername();
            Review review = reviewService.getReviewById(reviewId);

            if (!review.getUsername().equals(loggedInUserName)) {
                log.warn("User {} does not have permission to delete review {}", loggedInUserName, reviewId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("삭제 권한이 없습니다.");
            }

            reviewService.deleteReviewById(reviewId);
            log.info("Review {} deleted successfully by user {}", reviewId, loggedInUserName);
            return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("Error deleting review. ContentId: {}, ReviewId: {}", contentId, reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 삭제 중 오류가 발생했습니다.");
        }
    }
}
