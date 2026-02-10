package org.jubensha.aijubenshabackend.service.investigation;

import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.exception.InvalidInvestigationException;
import org.jubensha.aijubenshabackend.core.exception.NoInvestigationChanceException;
import org.jubensha.aijubenshabackend.models.dto.InvestigationRequestDTO;
import org.jubensha.aijubenshabackend.models.dto.InvestigationResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.InvestigationStatusDTO;

import java.util.List;
import java.util.Map;

/**
 * 搜证服务接口
 * 定义玩家搜证相关的业务逻辑
 *
 * @author luobo
 * @date 2026-02-10
 */
public interface InvestigationService {

    /**
     * 执行搜证操作
     * 玩家消耗一次搜证机会，获得指定场景中的一个线索
     *
     * @param gameId  游戏ID
     * @param request 搜证请求
     * @return 搜证响应，包含获得的线索和剩余次数
     * @throws NoInvestigationChanceException 当玩家搜证次数已用完时抛出
     * @throws InvalidInvestigationException  当游戏不在搜证阶段或场景无效时抛出
     */
    InvestigationResponseDTO investigate(Long gameId, InvestigationRequestDTO request);

    /**
     * 获取玩家的搜证状态
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 搜证状态DTO
     */
    InvestigationStatusDTO getInvestigationStatus(Long gameId, Long playerId);

    /**
     * 检查玩家是否可以搜证
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return true 如果玩家可以搜证，false 否则
     */
    boolean canInvestigate(Long gameId, Long playerId);

    /**
     * 获取游戏中所有玩家的搜证状态
     *
     * @param gameId 游戏ID
     * @return 玩家ID到搜证状态的映射
     */
    Map<Long, InvestigationStatusDTO> getAllPlayersInvestigationStatus(Long gameId);

    /**
     * 初始化游戏搜证阶段
     * 在游戏进入搜证阶段时调用，设置所有玩家的初始搜证次数
     *
     * @param gameId      游戏ID
     * @param playerIds   玩家ID列表
     * @param totalRounds 搜证总轮次（可选，默认1轮）
     */
    void initInvestigationPhase(Long gameId, List<Long> playerIds, Integer totalRounds);

    /**
     * 获取工作流上下文
     * 用于在工作流节点中访问搜证状态
     *
     * @param gameId 游戏ID
     * @return 工作流上下文
     */
    WorkflowContext getWorkflowContext(Long gameId);

    /**
     * 保存工作流上下文
     * 用于在工作流节点中更新搜证状态
     *
     * @param gameId  游戏ID
     * @param context 工作流上下文
     */
    void saveWorkflowContext(Long gameId, WorkflowContext context);
}
