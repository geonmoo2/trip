package com.example.demo.Service;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.CommentEntity;
import com.example.demo.dto.CommentDTO;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public Long save(CommentDTO commentDTO) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(commentDTO.getBoardId());
        if (optionalBoardEntity.isPresent()) {
            BoardEntity BoardEntity = optionalBoardEntity.get();
            CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, BoardEntity);
            return commentRepository.save(commentEntity).getId();

        } else {
            return null;
        }
    }
    public List<CommentDTO> findAll(Long boardId) {
        BoardEntity BoardEntity = boardRepository.findById(boardId).orElseThrow();
        List<CommentEntity> CommentEntityList = commentRepository.findAllByBoardEntityOrderByIdDesc(BoardEntity);
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (CommentEntity CommentEntity : CommentEntityList) {
            CommentDTO commentDTO = CommentDTO.toCommentDTO(CommentEntity, boardId);
            commentDTOList.add(commentDTO);
        }
        return commentDTOList;
    }
    public boolean delete(Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return true;
        }
        return false;

    }

}
