package com.example.demo.Service;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.dto.BoardDTO;
import com.example.demo.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.demo.dto.BoardDTO.toBoardDTO;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    private final BoardRepository boardRepository;

    public void save(BoardDTO boardDTO) {
        logger.debug("Entering save method with boardDTO: {}", boardDTO);
        BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
        boardRepository.save(boardEntity);
    }
    public List<BoardDTO> findALL() {
        logger.debug("Entering findAll method");
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (BoardEntity boardEntity : boardEntityList) {
            boardDTOList.add(toBoardDTO(boardEntity));
        }
        return boardDTOList;
    }

    public Page<BoardDTO> search(String searchField, String query, Pageable pageable) {
        logger.debug("Entering search method with searchField: {}, query: {}", searchField, query);
        Page<BoardEntity> boardEntities;
        switch (searchField) {
            case "id":
                try {
                    Long id = Long.parseLong(query);
                    Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
                    if (optionalBoardEntity.isPresent()) {
                        boardEntities = new PageImpl<>(Collections.singletonList(optionalBoardEntity.get()), pageable, 1);

                    }else {
                        boardEntities = Page.empty();
                    }
                } catch (NumberFormatException e) {
                    boardEntities = Page.empty();
                }
                break;
            case "title":
                boardEntities = boardRepository.findByBoardTitleContainingOrderByIdDesc(query, pageable);
                break;
            case "writer":
                boardEntities = boardRepository.findByBoardWriterContainingOrderByIdDesc(query, pageable);
                break;
            case "content":
                boardEntities = boardRepository.findByBoardContentsContainingOrderByIdDesc(query, pageable);
                break;
            default:
                boardEntities = Page.empty();
                break;

        }
        return boardEntities.map(BoardDTO::toBoardDTO);
    }
    public Page<BoardDTO> paging(Pageable pageable) {
        logger.debug("Entering paging method with pageable: {}", pageable);
        Page<BoardEntity> boardEntities = boardRepository.findAllByOrderByIdDesc(pageable);
        return boardEntities.map(BoardDTO::toBoardDTO);
    }

    @Transactional
    public void updateHits(Long id) {
        logger.debug("Entering updateHits method with id: {}", id);
        boardRepository.updateHits(id);
    }

    @Transactional
    public BoardDTO findById(Long id) {
        logger.debug("Entering findById method with id: {}", id);
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        return optionalBoardEntity.map(BoardDTO::toBoardDTO).orElse(null);
    }
    @Transactional
    public BoardDTO update(BoardDTO boardDTO) {
        logger.debug("Entering update method with boardDTO: {}", boardDTO);
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(boardDTO.getId());
        if (optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();

            if (boardDTO.getComments() != null) {
                boardEntity.getComments().clear();
                boardEntity.getComments().addAll(boardDTO.getComments());
            }
            boardEntity.setBoardTitle(boardDTO.getBoardTitle());
            boardEntity.setBoardContents(boardDTO.getBoardContents());
            boardEntity.setBoardHits(boardDTO.getBoardHits());

            boardRepository.save(boardEntity);
            return BoardDTO.toBoardDTO(boardEntity);

        }else {

            throw new IllegalArgumentException("Board with ID " + boardDTO.getId() + "not found.");

        }
    }
    @Transactional
    public void delete(Long id) {
        logger.debug("Entering delete method with id: {}", id);
        boardRepository.deleteById(id);
    }
}
