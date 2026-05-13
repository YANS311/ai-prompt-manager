package com.ai.promptmanager.repository;

import com.ai.promptmanager.entity.PromptRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PromptRun 数据访问层
 */
@Repository
public interface PromptRunRepository extends JpaRepository<PromptRun, Long> {

    /**
     * 查询某个 Prompt 的所有运行记录，按创建时间倒序
     */
    List<PromptRun> findByPromptIdOrderByCreatedAtDesc(Long promptId);
}
