package com.ssafy.mereview.api.controller.review.dto;

import com.ssafy.mereview.api.service.review.dto.CommentLikeServiceRequest;
import com.ssafy.mereview.domain.review.entity.CommentLikeType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentLikeRequest {

    private Long commentId;
    private Long memberId;
    private CommentLikeType type;

    @Builder
    public CommentLikeRequest(Long memberId, CommentLikeType type) {
        this.memberId = memberId;
        this.type = type;
    }

    public CommentLikeServiceRequest toServiceRequest() {
        return CommentLikeServiceRequest.builder()
                .commentId(commentId)
                .memberId(memberId)
                .type(type)
                .build();
    }
}
