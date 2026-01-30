package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
     * @param clue 线索实体
     * @return 创建的线索
     */
    @PostMapping
    public ResponseEntity<Clue> createClue(@RequestBody Clue clue) {
        Clue createdClue = clueService.createClue(clue);
        return new ResponseEntity<>(createdClue, HttpStatus.CREATED);
    }

    /**
     * 更新线索
     *
     * @param id   线索ID
     * @param clue 线索实体
     * @return 更新后的线索
     */
    @PutMapping("/{id}")
    public ResponseEntity<Clue> updateClue(@PathVariable Long id, @RequestBody Clue clue) {
        Clue updatedClue = clueService.updateClue(id, clue);
        return new ResponseEntity<>(updatedClue, HttpStatus.OK);
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
     * @return 线索实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Clue> getClueById(@PathVariable Long id) {
        Optional<Clue> clue = clueService.getClueById(id);
        return clue.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 查询所有线索
     *
     * @return 线索列表
     */
    @GetMapping
    public ResponseEntity<List<Clue>> getAllClues() {
        List<Clue> clues = clueService.getAllClues();
        return new ResponseEntity<>(clues, HttpStatus.OK);
    }

    /**
     * 根据剧本ID查询线索
     *
     * @param scriptId 剧本ID
     * @return 线索列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<Clue>> getCluesByScriptId(@PathVariable Long scriptId) {
        List<Clue> clues = clueService.getCluesByScriptId(scriptId);
        return new ResponseEntity<>(clues, HttpStatus.OK);
    }

    /**
     * 根据线索类型查询线索
     *
     * @param type 线索类型
     * @return 线索列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Clue>> getCluesByType(@PathVariable ClueType type) {
        List<Clue> clues = clueService.getCluesByType(type);
        return new ResponseEntity<>(clues, HttpStatus.OK);
    }

    /**
     * 根据可见性查询线索
     *
     * @param visibility 线索可见性
     * @return 线索列表
     */
    @GetMapping("/visibility/{visibility}")
    public ResponseEntity<List<Clue>> getCluesByVisibility(@PathVariable ClueVisibility visibility) {
        List<Clue> clues = clueService.getCluesByVisibility(visibility);
        return new ResponseEntity<>(clues, HttpStatus.OK);
    }

    /**
     * 根据场景查询线索
     *
     * @param scene 场景名称
     * @return 线索列表
     */
    @GetMapping("/scene/{scene}")
    public ResponseEntity<List<Clue>> getCluesByScene(@PathVariable String scene) {
        List<Clue> clues = clueService.getCluesByScene(scene);
        return new ResponseEntity<>(clues, HttpStatus.OK);
    }

    /**
     * 查询重要线索
     *
     * @param importance 重要度阈值
     * @return 线索列表
     */
    @GetMapping("/important")
    public ResponseEntity<List<Clue>> getImportantClues(@RequestParam(defaultValue = "50") @Valid Integer importance) {
        List<Clue> clues = clueService.getImportantClues(importance);
        return new ResponseEntity<>(clues, HttpStatus.OK);
    }
}