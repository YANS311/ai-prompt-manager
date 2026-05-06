package com.ai.promptmanager.mapper;

import com.ai.promptmanager.dto.PromptCreateDTO;
import com.ai.promptmanager.dto.PromptDTO;
import com.ai.promptmanager.dto.PromptUpdateDTO;
import com.ai.promptmanager.entity.Prompt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct 映射器
 *
 * 面试要点:
 * 1. MapStruct vs BeanUtils vs 手动映射?
 *    - MapStruct: 编译期生成代码，性能最高（接近手动映射）
 *    - BeanUtils: 运行时反射，性能较差，每次复制约150ms
 *    - 手动映射: 性能最高但代码冗余，维护成本高
 *
 * 2. @Mapper(componentModel = "spring") 的作用?
 *    - 生成的Mapper实现类会被Spring管理
 *    - 可以通过@Autowired注入使用
 *    - 如果不写，需要用 Mappers.getMapper() 手动获取
 *
 * 3. @MappingTarget 的作用?
 *    - 更新现有对象，而不是创建新对象
 *    - 常用于 PUT/PATCH 操作，避免字段丢失
 *
 * 4. 性能对比（1000次映射）:
 *    - MapStruct: ~1ms
 *    - BeanUtils: ~150ms
 *    - 手动映射: ~0.5ms
 */
@Mapper(componentModel = "spring")
public interface PromptMapper {

    PromptMapper INSTANCE = Mappers.getMapper(PromptMapper.class);

    /**
     * Entity -> DTO (查询返回)
     */
    PromptDTO toDTO(Prompt prompt);

    /**
     * Entity List -> DTO List
     */
    List<PromptDTO> toDTOList(List<Prompt> prompts);

    /**
     * CreateDTO -> Entity (创建时)
     */
    Prompt toEntity(PromptCreateDTO createDTO);

    /**
     * UpdateDTO -> Entity (更新时，更新现有对象的字段)
     *
     * @MappingTarget: 指定目标对象，MapStruct会将source的值复制到target
     * @Mapping(target = "id", ignore = true): 忽略id字段，防止被覆盖
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDTO(PromptUpdateDTO updateDTO, @MappingTarget Prompt prompt);
}
