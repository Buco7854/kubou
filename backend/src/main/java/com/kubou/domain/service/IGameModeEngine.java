package com.kubou.domain.service;

import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.GameState;

public interface IGameModeEngine {
    GameState handleNextState(GameSession session, int totalQuestions);
}
