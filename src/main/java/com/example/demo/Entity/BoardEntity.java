package com.example.demo.Entity;

import com.example.demo.dto.BoardDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "board_table")
@EntityListeners(AuditingEntityListener.class)
public class BoardEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20 , nullable = false)
    private String boardWriter;

    @Column
    private String boardPass;

    @Column
    private String boardTitle;

    @Column(length = 1000)
    private String boardContents;

    @Column
    private int boardHits;

    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    public static BoardEntity toSaveEntity(BoardDTO boardDTO) {

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setBoardWriter(boardDTO.getBoardWriter());
        boardEntity.setBoardPass(boardDTO.getBoardPass());
        boardEntity.setBoardTitle(boardDTO.getBoardTitle());
        boardEntity.setBoardContents(boardDTO.getBoardContents());
        boardEntity.setBoardHits(0);
        return boardEntity;

    }
    public static BoardEntity toUpdateEntity(BoardDTO boardDTO) {

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setId(boardDTO.getId());
        boardEntity.setBoardWriter(boardDTO.getBoardWriter());
        boardEntity.setBoardPass(boardDTO.getBoardPass());
        boardEntity.setBoardTitle(boardDTO.getBoardTitle());
        boardEntity.setBoardContents(boardDTO.getBoardContents());
        boardEntity.setBoardHits(boardDTO.getBoardHits());
        boardEntity.setComments(boardDTO.getComments());
        return boardEntity;



    }

}
