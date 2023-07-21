package com.ssafy.mereview.domain.movie.entity;

import com.ssafy.mereview.common.domain.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
public class Movie extends BaseEntity {

    @Id
    @Column(name = "movie_id")
    Long id;

    String title;

    String overview;

    Double populity;

    String posterImg;

    String releaseDate;



}
