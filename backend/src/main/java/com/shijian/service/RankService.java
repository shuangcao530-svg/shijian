package com.shijian.service;

import com.shijian.vo.PostVO;
import com.shijian.vo.RankVO;
import java.util.List;

public interface RankService {
    List<PostVO> getHotPosts(Integer limit);
    List<PostVO> getWeeklyRanking(Integer limit);
    List<PostVO> getMonthlyRanking(Integer limit);
    List<PostVO> getCategoryRanking(Long categoryId, Integer limit);
}
