package com.odinbook.postservice.record;

import java.sql.Date;
import java.util.List;

public record PostRecord(Long id,
                         Long accountId,
                         Boolean isShared,
                         Boolean isVisibleToFollowers,
                         Boolean friendsVisibilityType,
                         List<Long> visibleToFriendList) { }
