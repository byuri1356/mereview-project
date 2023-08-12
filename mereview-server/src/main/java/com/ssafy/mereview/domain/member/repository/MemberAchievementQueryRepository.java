package com.ssafy.mereview.domain.member.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.mereview.domain.member.entity.MemberAchievement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ssafy.mereview.domain.member.entity.QAchievement.achievement;
import static com.ssafy.mereview.domain.member.entity.QMember.member;
import static com.ssafy.mereview.domain.member.entity.QMemberAchievement.memberAchievement;

@Repository
@RequiredArgsConstructor
public class MemberAchievementQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<MemberAchievement> searchByMemberId(Long memberId){
        return queryFactory
                .select(memberAchievement)
                .from(memberAchievement)
                .innerJoin(memberAchievement.member, member)
                .innerJoin(memberAchievement.achievement, achievement)
                .where(member.id.eq(memberId))
                .fetch();
    }

}