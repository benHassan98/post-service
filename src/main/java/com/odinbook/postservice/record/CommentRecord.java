package com.odinbook.postservice.record;

import java.sql.Date;

public record CommentRecord(Long id, Long postId, Long accountId) { }
