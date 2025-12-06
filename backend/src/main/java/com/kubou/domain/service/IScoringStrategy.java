package com.kubou.domain.service;

import com.kubou.domain.entity.GameConfig;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.UserAnswer;

public interface IScoringStrategy {
    int calculate(UserAnswer answer, Question question, GameConfig config);
}
