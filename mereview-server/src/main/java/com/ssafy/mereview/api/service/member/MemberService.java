package com.ssafy.mereview.api.service.member;

import com.ssafy.mereview.api.controller.member.dto.request.InterestRequest;
import com.ssafy.mereview.api.service.member.dto.request.MemberCreateServiceRequest;
import com.ssafy.mereview.api.service.member.dto.request.MemberUpdateServiceRequest;
import com.ssafy.mereview.api.service.review.ReviewQueryService;
import com.ssafy.mereview.common.util.file.UploadFile;
import com.ssafy.mereview.common.util.jwt.JwtUtils;
import com.ssafy.mereview.domain.member.entity.*;
import com.ssafy.mereview.domain.member.repository.*;
import com.ssafy.mereview.domain.movie.entity.Genre;
import com.ssafy.mereview.domain.movie.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final MemberRepository memberRepository;

    private final MemberQueryRepository memberQueryRepository;

    private final MemberAchievementRepository memberAchievementRepository;

    private final MemberVisitCountRepository memberVisitCountRepository;

    private final MemberInterestRepository memberInterestRepository;

    private final ProfileImageRepository profileImageRepository;

    private final MemberTierRepository memberTierRepository;

    private final GenreRepository genreRepository;

    private final ReviewQueryService reviewQueryService;

    public Long createMember(MemberCreateServiceRequest request) {
        Member existingMember = memberQueryRepository.searchByEmail(request.getEmail());
        if (existingMember != null) {
            throw new DuplicateKeyException("이미 존재하는 회원입니다.");
        }
        log.debug("request check = {}" + request);

        Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));
        log.debug("member = " + member.getEmail());

        Member savedMember = memberRepository.save(member);
        log.debug("savedMember = " + savedMember.getEmail());

        profileImageRepository.save(createProfileImage(request, savedMember.getId()));

        //방문자 수 초기화
        createVisitCount(member);
        //회원 관심사 초기화

        createInterests(request.getInterestRequests(), member);

        //회원 티어 초기화
        createTier(member);
        createAchievement(member);

        return savedMember.getId();
    }

    public Long updateMember(Long memberId, MemberUpdateServiceRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));
        log.debug("update request : {}", request);

        List<InterestRequest> interestRequests = request.getInterestRequests();
        log.debug("interestRequests = " + interestRequests);

        member.update(request, createInterests(interestRequests, member));

        return member.getId();
    }

    public void updateViewCount(Long id){
        Member member = memberRepository.findById(id).orElseThrow(NoSuchElementException::new);
        log.debug("조회수 : {}",member.getMemberVisit());
        member.getMemberVisit().updateVisitCount();
    }

    public void createFollow(Long targetId, Long currentUserId) {
        // 팔로우 할 유저

        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found!"));

        Member currentMember = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Following not found!"));

        // 팔로워가 현재 유저인 타겟(내가 팔로우하는 타겟)이 존재할 경우
        // TODO: 2023-08-03 쿼리로 만들어주기
        if (currentMember.getFollowing().contains(target)) {
            unfollow(target, currentMember);
        }
        else {
            follow(target, currentMember);
        }
    }

    public void updateProfileImage(Long memberId, UploadFile uploadFile) {
        Member member = memberRepository.findById(memberId).orElseThrow(NoSuchElementException::new);
        member.updateProfileImage(uploadFile);
    }

    //***************private method*****************//

    private void createVisitCount(Member member) {
        MemberVisitCount memberVisitCount = MemberVisitCount.builder()
                .member(member)
                .build();
        memberVisitCountRepository.save(memberVisitCount);
    }

    private List<Interest> createInterests(List<InterestRequest> requests, Member member) {
        List<Interest> interests = new ArrayList<>();
        //TODO:genre 없을 경우 exception 터뜨려야함

        requests.stream().map(interestRequest ->
                        genreRepository.findById(interestRequest.getGenreId()).orElseThrow(NoSuchElementException::new))
                .map(genre -> Interest.builder()
                        .member(member)
                        .genre(genre)
                        .build()).forEach(interests::add);

        log.debug("interests = " + interests.size());

        memberInterestRepository.saveAll(interests);
        return interests;
    }

    private void createTier(Member member) {
        List<Genre> genres = memberQueryRepository.searchAllGenre();

        List<MemberTier> memberTiers = new ArrayList<>();
        genres.forEach(genre -> memberTiers.add(MemberTier.builder()
                .member(member)
                .genre(genre)
                .build()));
        log.debug("memberTiers = " + memberTiers.size());

        memberTierRepository.saveAll(memberTiers);
    }

    private void createAchievement(Member member) {
        // TODO: 2023-08-03 장르 레포지토리로 바꾸기
        List<Genre> genres = genreRepository.findAll();

        List<MemberAchievement> memberAchievements = genres.stream().map(genre -> MemberAchievement.builder()
                .member(member)
                .genre(genre)
                .build()).collect(Collectors.toList());
        log.debug("memberAchievements = " + memberAchievements.size());

        memberAchievementRepository.saveAll(memberAchievements);
    }

    private ProfileImage createProfileImage(MemberCreateServiceRequest request, Long saveId) {
        return ProfileImage.builder()
                .member(Member.builder().id(saveId).build())
                .uploadFile(request.getUploadFile())
                .build();
    }

    private void follow(Member target, Member currentMember) {
        currentMember.getFollowing().add(target);
        target.getFollowers().add(currentMember);
    }

    private void unfollow(Member target, Member currentMember) {
        currentMember.getFollowing().remove(target);
        target.getFollowers().remove(currentMember);
    }
}
