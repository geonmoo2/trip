package com.example.demo.controller;


import com.example.demo.Service.BoardService;
import com.example.demo.Service.CommentService;
import com.example.demo.dto.BoardDTO;
import com.example.demo.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private final BoardService boardService;
    private final CommentService commentService;


    @GetMapping("/paging")
    public String fanPaging(@RequestParam(value = "searchField", required = false) String searchField,
                            @RequestParam(value = "query", required = false) String query,
                            @RequestParam(value = "page", defaultValue = "1") int page,
                            Model model) {
        logger.debug("Entering Paging method with searchField: {}, query: {}, page: {}", searchField, query, page);
        Pageable pageable = PageRequest.of(page - 1, 8);
        Page<BoardDTO> boardList;
        if (query != null && !query.isEmpty() && searchField != null && !searchField.isEmpty()) {
            boardList = boardService.search(searchField, query, pageable);
        } else {
            boardList = boardService.paging(pageable);
        }
        model.addAttribute("boardList", boardList);
        model.addAttribute("query", query);
        model.addAttribute("searchField", searchField);
        model.addAttribute("currentPage", page);

        int blockLimit = 10;
        int startPage = Math.max(1, ((int) Math.ceil((double) page / blockLimit)) * blockLimit - (blockLimit - 1));
        int endPage = Math.min(startPage + blockLimit - 1, boardList.getTotalPages());

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board/paging";
    }

    @GetMapping("/detail")
    public String Paging(@RequestParam(value = "searchFied", required = false) String searchFied,
                         @RequestParam(value = "query", required = false) String query,
                         @RequestParam(value = "page", defaultValue = "1") int page,
                         Model model){



        logger.debug("Entering Paging methid with searchField: {}, query: {} , page: {}",searchFied, query, page);
        Pageable pageable = PageRequest.of(page -1, 8);
        Page<BoardDTO> boardList;

        if (query != null && !query.isEmpty() && searchFied != null && !searchFied.isEmpty()) {

            boardList = boardService.search(searchFied, query, pageable);
        }else{

            boardList = boardService.paging(pageable);

        }
        // boardList의 첫 번째 게시글을 board 객체로 설정
        if (!boardList.isEmpty()) {
            BoardDTO firstBoard = boardList.getContent().get(0);
            model.addAttribute("board", firstBoard);
        } else {
            // 게시글이 없는 경우 빈 DTO 객체를 생성하여 null 방지
            model.addAttribute("board", new BoardDTO());
        }

        model.addAttribute("boardList", boardList);
        model.addAttribute("query", query);
        model.addAttribute("searchField", searchFied);
        model.addAttribute("currentPage", page);

        int blockLimit = 10;
        int startPage = Math.max(1, ((int) Math.ceil((double) page / blockLimit) * blockLimit - (blockLimit - 1)));
        int endPage = Math.min(startPage + blockLimit - 1 , boardList.getTotalPages());

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board/detail";


    }
    @GetMapping("/save")
    public String boardsave(Model model) {

        BoardDTO boardDTO = new BoardDTO();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("board", boardDTO);
        model.addAttribute("username",username);
        return "board/save";

    }
    @PostMapping("/save")
    public String boardsave(@ModelAttribute BoardDTO boardDTO) {
        logger.debug("Entering fanSave method with boardDTO: {}", boardDTO);
        boardService.save(boardDTO);
        return "redirect:/board/detail";

    }
    @GetMapping("/detail/{id}")
    public String boarddetail(@PathVariable("id") Long id , Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
        logger.debug("Entering fanDetail method with id: {}, page: {}", id, page);
        boardService.updateHits(id);
        BoardDTO boardDTO = boardService.findById(id);

        List<CommentDTO> commentDTOList = commentService.findAll(id);
        model.addAttribute("commentList", commentDTOList);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);

        if (boardDTO != null) {
            model.addAttribute("board", boardDTO);
            model.addAttribute("currentPage", page);
            return "/board/detail";
        } else {

            return "redirect:/board/board?page=" + page;
        }
    }
    @GetMapping("/update/{id}")
    public String boardupdateForm(@PathVariable("id") Long id,
                                  @RequestParam(value = "page", defaultValue = "1") int currentPage,
                                  Model model) {
        logger.debug("Entering UpdateForm method with id: {}, currentPage: {}", id, currentPage);
        BoardDTO boardDTO = boardService.findById(id);
        if (boardDTO != null) {
            model.addAttribute("boardUpdate", boardDTO);
            model.addAttribute("currentPage", currentPage);
            return "/board/update";

        } else {
            return "redirect:/board/paging?page=" + currentPage;

        }
    }
    @PostMapping("/update")
    public String boardupdate(@ModelAttribute BoardDTO boardDTO, @RequestParam(value = "currentPage", defaultValue = "1") int currentPage, Model model) {
        logger.debug("Entering Update method with boardDTO: {}", boardDTO);
        BoardDTO board = boardService.update(boardDTO);
        model.addAttribute("board", board);
        return "redirect:/board/detail/" + boardDTO.getId() + "?page=" + currentPage;

    }
    @GetMapping("/delete/{id}")
    public String boardDelete(@PathVariable("id") Long id, @RequestParam(value = "page", defaultValue = "1") int page) {
        logger.debug("Entering fanDelete method with id: {}", id);
        try{

            boardService.delete(id);
        } catch (Exception e) {

            logger.error("Error deleting board with id: {}", id , e);
        }
        return "redirect:/board/paging?page=" + page;

    }
    @GetMapping("/Download/{fileName:.+}")
    public ResponseEntity<Resource> boardDownloadFile(@PathVariable("fileName") String fileName) {
        try {
            Path filePath = Paths.get("C:/uploads/").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);


            }else {
                return ResponseEntity.notFound().build();
            }
        }catch (IOException ex) {
            return ResponseEntity.status(500).build();
        }
    }
    @PostMapping("/comment/save")
    public ResponseEntity<?> saveComment(@ModelAttribute CommentDTO commentDTO) {
        Long saveResult = commentService.save(commentDTO);

        if (saveResult != null) {
            List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
            return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
        } else {

            return new ResponseEntity<>("해당 게시글이 존재하지 않습니다." , HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/comment/delete/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable("id") Long id) {
        boolean deleteResult = commentService.delete(id);
        if (deleteResult) {

            return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
        }else {
            return new ResponseEntity<>("댓글 삭제 실패", HttpStatus.NOT_FOUND);
        }
    }




}
