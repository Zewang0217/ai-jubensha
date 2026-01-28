package org.jubensha.aijubenshabackend.ai.agent;

public interface Agent {
    
    /**
     * 处理输入消息并生成响应
     */
    String process(String input);
    
    /**
     * 获取Agent的名称
     */
    String getName();
    
    /**
     * 获取Agent的类型
     */
    AgentType getType();
    
    /**
     * 重置Agent状态
     */
    void reset();
}
