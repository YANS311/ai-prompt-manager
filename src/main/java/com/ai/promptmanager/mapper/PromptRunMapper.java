package com.ai.promptmanager.mapper;

import com.ai.promptmanager.dto.PromptRunDTO;
import com.ai.promptmanager.entity.PromptRun;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * PromptRun Entity ↔ DTO 映射器
 */
@Mapper(componentModel = "spring")
public interface PromptRunMapper {

    /**
     * Entity 转 DTO
     */
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    PromptRunDTO toDTO(PromptRun entity);

    /**
     * Entity List 转 DTO List
     */
    List<PromptRunDTO> toDTOList(List<PromptRun> entities);
}
