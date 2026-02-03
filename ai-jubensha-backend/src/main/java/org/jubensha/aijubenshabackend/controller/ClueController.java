package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import org.jubensha.aijubenshabackend.models.dto.ClueCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.ClueResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.ClueUpdateDTO;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 线索控制器
 */
@RestController
@RequestMapping("/api/clues")
public class ClueController {

    private final ClueService clueService;

    public ClueController(ClueService clueService) {
        this.clueService = clueService;
    }

    /**
     * 创建线索
     *
     * @param clueCreateDTO 线索创建DTO
     * @return 创建的线索响应DTO
     */
    @PostMapping
    public ResponseEntity<ClueResponseDTO> createClue(@Valid @RequestBody ClueCreateDTO clueCreateDTO) {
        Clue clue = new Clue();
        clue.setScriptId(clueCreateDTO.getScriptId());
        clue.setName(clueCreateDTO.getName());
        clue.setDescription(clueCreateDTO.getDescription());
        clue.setType(clueCreateDTO.getType());
        clue.setVisibility(clueCreateDTO.getVisibility());
        clue.setScene(clueCreateDTO.getScene());
        clue.setImportance(clueCreateDTO.getImportance());
        
        Clue createdClue = clueService.createClue(clue);
        ClueResponseDTO responseDTO = ClueResponseDTO.fromEntity(createdClue);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * 更新线索
     *
     * @param id   线索ID
     * @param clueUpdateDTO 线索更新DTO
     * @return 更新后的线索响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClueResponseDTO> updateClue(@PathVariable Long id, @Valid @RequestBody ClueUpdateDTO clueUpdateDTO) {
        Clue clue = new Clue();
        clue.setName(clueUpdateDTO.getName());
        clue.setDescription(clueUpdateDTO.getDescription());
        clue.setType(clueUpdateDTO.getType());
        clue.setVisibility(clueUpdateDTO.getVisibility());
        clue.setScene(clueUpdateDTO.getScene());
        clue.setImportance(clueUpdateDTO.getImportance());

        try {
            Clue updatedClue = clueService.updateClue(id, clue);
            ClueResponseDTO responseDTO = ClueResponseDTO.fromEntity(updatedClue);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除线索
     *
     * @param id 线索ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClue(@PathVariable Long id) {
        clueService.deleteClue(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据ID查询线索
     *
     * @param id 线索ID
     * @return 线索响应DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClueResponseDTO> getClueById(@PathVariable Long id) {
        Optional<Clue> clue = clueService.getClueById(id);
        return clue.map(value -> {
            ClueResponseDTO responseDTO = ClueResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 查询所有线索
     *
     * @return 线索响应DTO列表
     */
    @GetMapping
    public ResponseEntity<List<ClueResponseDTO>> getAllClues() {
        List<Clue> clues = clueService.getAllClues();
        List<ClueResponseDTO> responseDTOs = clues.stream()
                .map(ClueResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据剧本ID查询线索
     *
     * @param scriptId 剧本ID
     * @return 线索响应DTO列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<ClueResponseDTO>> getCluesByScriptId(@PathVariable Long scriptId) {
        List<Clue> clues = clueService.getCluesByScriptId(scriptId);
        List<ClueResponseDTO> responseDTOs = clues.stream()
                .map(ClueResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据线索类型查询线索
     *
     * @param type 线索类型
     * @return 线索响应DTO列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ClueResponseDTO>> getCluesByType(@PathVariable String type) {
        try {
            ClueType clueType = ClueType.valueOf(type.toUpperCase());
            List<Clue> clues = clueService.getCluesByType(clueType);
            List<ClueResponseDTO> responseDTOs = clues.stream()
                    .map(ClueResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据可见性查询线索
     *
     * @param visibility 线索可见性
     * @return 线索响应DTO列表
     */
    @GetMapping("/visibility/{visibility}")
    public ResponseEntity<List<ClueResponseDTO>> getCluesByVisibility(@PathVariable String visibility) {
        try {
            ClueVisibility clueVisibility = ClueVisibility.valueOf(visibility.toUpperCase());
            List<Clue> clues = clueService.getCluesByVisibility(clueVisibility);
            List<ClueResponseDTO> responseDTOs = clues.stream()
                    .map(ClueResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据场景查询线索
     *
     * @param scene 场景名称
     * @return 线索响应DTO列表
     */
    @GetMapping("/scene/{scene}")
    public ResponseEntity<List<ClueResponseDTO>> getCluesByScene(@PathVariable String scene) {
        List<Clue> clues = clueService.getCluesByScene(scene);
        List<ClueResponseDTO> responseDTOs = clues.stream()
                .map(ClueResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 查询重要线索
     *
     * @param importance 重要度阈值
     * @return 线索响应DTO列表
     */
    @GetMapping("/important")
    public ResponseEntity<List<ClueResponseDTO>> getImportantClues(@RequestParam(defaultValue = "50") @Valid Integer importance) {
        List<Clue> clues = clueService.getImportantClues(importance);
        List<ClueResponseDTO> responseDTOs = clues.stream()
                .map(ClueResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }
}