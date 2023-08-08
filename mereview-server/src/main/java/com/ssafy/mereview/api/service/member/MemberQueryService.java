package com.ssafy.mereview.api.service.member;

import com.ssafy.mereview.api.controller.member.dto.request.MemberLoginRequest;
import com.ssafy.mereview.api.service.member.dto.response.*;
import com.ssafy.mereview.api.service.movie.dto.response.GenreResponse;
import com.ssafy.mereview.api.service.review.dto.response.*;
import com.ssafy.mereview.common.util.jwt.JwtUtils;
import com.ssafy.mereview.domain.member.entity.*;
import com.ssafy.mereview.domain.member.repository.MemberQueryRepository;
import com.ssafy.mereview.domain.member.repository.MemberVisitQueryRepository;
import com.ssafy.mereview.domain.movie.entity.Movie;
import com.ssafy.mereview.domain.review.entity.*;
import com.ssafy.mereview.domain.review.repository.query.NotificationQueryRepository;
import com.ssafy.mereview.domain.review.repository.query.ReviewEvaluationQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.ssafy.mereview.domain.review.entity.ReviewEvaluationType.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {
    private final MemberQueryRepository memberQueryRepository;
    private final MemberVisitQueryRepository memberVisitQueryRepository;
    private final NotificationQueryRepository notificationQueryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewEvaluationQueryRepository reviewEvaluationQueryRepository;

    private final JwtUtils jwtUtils;


    public MemberLoginResponse login(MemberLoginRequest request) {
        log.debug("MemberLoginRequest : {}", request);

        Member searchMember = memberQueryRepository.searchByEmail(request.getEmail());

        if (searchMember == null || searchMember.getRole().equals(Role.DELETED)) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), searchMember.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        return createMemberLoginResponse(searchMember);
    }

    public List<MemberTierResponse> searchMemberTierByGenre(Long memberId, int genreNumber) {
        List<MemberTier> memberTiers = memberQueryRepository.searchMemberTierByGenre(memberId, genreNumber);

        return createMemberTierResponses(memberTiers);
    }

    private List<MemberTierResponse> createMemberTierResponses(List<MemberTier> memberTiers) {
        return memberTiers.stream().map(MemberTierResponse::of).collect(Collectors.toList());
    }


    private MemberLoginResponse createMemberLoginResponse(Member searchMember) {
        log.debug("searchMember : {}", searchMember);
        Map<String, String> token = jwtUtils.generateJwt(searchMember);
        return MemberLoginResponse.builder()
                .id(searchMember.getId())
                .email(searchMember.getEmail())
                .role(searchMember.getRole())
                .nickname(searchMember.getNickname())
                .profileImage(createProfileImageResponse(searchMember.getProfileImage()))
                .accessToken(token.get("accessToken"))
                .refreshToken(token.get("refreshToken"))
                .build();
    }

    public MemberResponse searchMemberInfo(Long id) {
        Member member = memberQueryRepository.searchById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        List<InterestResponse> interestResponses = searchInterestResponse(id);

        List<MemberTierResponse> memberTierResponses = searchMemberTierResponse(id);

        List<MemberAchievementResponse> memberAchievementResponses = searchMemberAchievementResponse(id);

        List<NotificationResponse> notificationResponses = notificationQueryRepository.searchByMemberId(id);
        List<ReviewResponse> reviewResponses = createReviewResponses(member.getReviews());
        int count = notificationQueryRepository.countByMemberId(id);

        return createMemberResponse(member, interestResponses, memberTierResponses, memberAchievementResponses, notificationResponses, count, reviewResponses);
    }

    private MemberResponse createMemberResponse(Member member, List<InterestResponse> interestResponses, List<MemberTierResponse> memberTierResponses, List<MemberAchievementResponse> memberAchievementResponses, List<NotificationResponse> notificationResponses, int notificationCount, List<ReviewResponse> reviewResponses){
        return MemberResponse.builder()
                .id(member.getId())
                .following(member.getFollowing().size())
                .follower(member.getFollowers().size())
                .reviews(reviewResponses)
                .todayVisitCount(member.getMemberVisit().getTodayVisitCount())
                .totalVisitCount(member.getMemberVisit().getTotalVisitCount())
                .email(member.getEmail())
                .introduce(member.getIntroduce())
                .createdTime(member.getCreatedTime())
                .notificationCount(notificationCount)
                .notifications(notificationResponses)
                .nickname(member.getNickname())
                .gender(member.getGender())
                .birthDate(member.getBirthDate())
                .interests(interestResponses)
                .achievements(memberAchievementResponses)
                .tiers(memberTierResponses)
                .profileImage(createProfileImageResponse(member.getProfileImage()))
                .build();
    }

    private ProfileImageResponse createProfileImageResponse(ProfileImage profileImage) {
        log.debug("ProfileImage : {}", profileImage);
        return profileImage.getUploadFile() != null ? ProfileImageResponse.of(profileImage) : ProfileImageResponse.builder().build();
    }

    private List<MemberAchievementResponse> searchMemberAchievementResponse(Long id) {
        List<MemberAchievement> memberAchievements = memberQueryRepository.searchMemberAchievementByMemberId(id);

        return memberAchievements.stream().map(MemberAchievement::of).collect(Collectors.toList());
    }

    private List<MemberTierResponse> searchMemberTierResponse(Long id) {
        List<MemberTier> memberTiers = memberQueryRepository.searchUserTierByMemberId(id);
        return memberTiers.stream()
                .map(MemberTierResponse::of)
                .collect(Collectors.toList());
    }

    private List<InterestResponse> searchInterestResponse(Long id) {
        List<Interest> interests = memberQueryRepository.searchInterestByMemberId(id);
        return interests.stream()
                .map(Interest::of)
                .collect(Collectors.toList());
    }

    public List<FollowResponse> searchFollowingResponse(Long memberId) {
        Member member = memberQueryRepository.searchById(memberId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));


        return member.getFollowing().stream()
                .map(FollowResponse::of)
                .collect(Collectors.toList());
    }

    public List<FollowResponse> searchFollowerResponse(Long memberId) {
       Member member = memberQueryRepository.searchById(memberId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return member.getFollowers().stream()
                .map(FollowResponse::of)
                .collect(Collectors.toList());
    }

    private ReviewDetailResponse createReviewDetailResponse(Review review) {
        Member writeMember = review.getMember();
        Movie movie = review.getMovie();
        return ReviewDetailResponse.builder()
                .reviewId(review.getId())
                .reviewTitle(review.getTitle())
                .reviewContent(review.getContent())
                .hits(review.getHits())
                .backgroundImage(createBackgroundImageResponse(review.getBackgroundImage()))
                .reviewHighlight(review.getHighlight())
                .reviewCreatedTime(review.getCreatedTime())
                .keywords(getKeywordResponses(review.getKeywords()))
                .evaluated(isEvaluated(review.getId(), writeMember.getId()))
                .funCount(getTypeCount(FUN, review.getId()))
                .usefulCount(getTypeCount(USEFUL, review.getId()))
                .badCount(getTypeCount(BAD, review.getId()))
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .genre(GenreResponse.of(review.getGenre()))
                .movieReleaseDate(movie.getReleaseDate())
                .memberId(writeMember.getId())
                .nickname(writeMember.getNickname())
                .memberTiers(getMemberTierResponses(writeMember.getMemberTiers()))
                .profileImage(getProfileImageResponse(writeMember.getProfileImage()))
                .comments(getCommentResponses(review.getComments()))
                .build();
    }

    /**
     * private methods
     */

    private List<ReviewResponse> createReviewResponses(List<Review> reviews) {
        return reviews.stream()
                .map(review -> {
                            Movie movie = review.getMovie();
                            Member writeMember = review.getMember();
                            return ReviewResponse.builder()
                                    .reviewId(review.getId())
                                    .reviewTitle(review.getTitle())
                                    .hits(review.getHits())
                                    .highlight(review.getHighlight())
                                    .movieEvaluationType(review.getType())
                                    .commentCount(review.getComments().size())
                                    .funCount(getTypeCount(FUN, review.getId()))
                                    .usefulCount(getTypeCount(USEFUL, review.getId()))
                                    .badCount(getTypeCount(BAD, review.getId()))
                                    .backgroundImageResponse(createBackgroundImageResponse(review.getBackgroundImage()))
                                    .createdTime(review.getCreatedTime())
                                    .memberId(writeMember.getId())
                                    .nickname(writeMember.getNickname())
                                    .profileImage(getProfileImageResponse(writeMember.getProfileImage()))
                                    .movieId(movie.getId())
                                    .movieTitle(movie.getTitle())
                                    .movieReleaseDate(movie.getReleaseDate())
                                    .genreResponse(GenreResponse.of(review.getGenre()))
                                    .build();
                        }
                ).collect(Collectors.toList());
    }

    private boolean isEvaluated(Long reviewId, Long memberId) {
        Optional<ReviewEvaluation> reviewEvaluation = reviewEvaluationQueryRepository.searchByReviewAndMember(reviewId, memberId);
        return reviewEvaluation.isPresent();
    }

    private int getTypeCount(ReviewEvaluationType type, Long reviewId) {
        return reviewEvaluationQueryRepository.getCountByReviewIdAndType(reviewId, type);
    }

    private BackgroundImageResponse createBackgroundImageResponse(BackgroundImage backgroundImage) {
        if (backgroundImage == null) {
            return null;
        }
        return BackgroundImageResponse.of(backgroundImage);
    }

    private List<KeywordResponse> getKeywordResponses(List<Keyword> keywords) {
        return keywords.stream().map(KeywordResponse::of).collect(Collectors.toList());
    }

    private List<MemberTierResponse> getMemberTierResponses(List<MemberTier> memberTiers) {
        if (memberTiers == null) {
            return new ArrayList<>();
        }
        return memberTiers.stream().map(MemberTierResponse::of).collect(Collectors.toList());
    }

    private ProfileImageResponse getProfileImageResponse(ProfileImage profileImage) {
        if (profileImage == null) {
            return null;
        }
        return ProfileImageResponse.of(profileImage);
    }

    private List<CommentResponse> getCommentResponses(List<Comment> comments) {
        return comments.stream().map(CommentResponse::of).collect(Collectors.toList());
    }
}