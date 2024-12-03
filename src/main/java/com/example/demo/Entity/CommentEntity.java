package com.example.demo.Entity;

import com.example.demo.dto.CommentDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "comment_table")
public class CommentEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String commentWriter;

    @Column
    private String commentContents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity boardEntity;

    public static CommentEntity toSaveEntity(CommentDTO commentDTO, BoardEntity BoardEntity) {

        CommentEntity CommentEntity = new CommentEntity();
        CommentEntity.setCommentWriter(commentDTO.getCommentWriter());
        CommentEntity.setCommentContents(commentDTO.getCommentContents());
        CommentEntity.setBoardEntity(BoardEntity);
        return CommentEntity;

    }
}
