package com.ssafy.mereview.domain.member.entity;

import com.ssafy.mereview.common.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Achievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String achievementName;

    @OneToMany(mappedBy = "achievement")
    private List<MemberAchievement> memberAchievement = new ArrayList<>();

    @Builder
    public Achievement(String achievementName) {
        this.achievementName = achievementName;
    }


}
