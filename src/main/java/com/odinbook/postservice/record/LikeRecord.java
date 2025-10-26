package com.odinbook.postservice.record;

public record LikeRecord(Long accountId, Long postId, Long postAccountId, Boolean isLike) {
}
