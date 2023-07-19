package com.ssafy.mereview.domain.member.repository;

import com.ssafy.mereview.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
