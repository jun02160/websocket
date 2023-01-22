package jjun.server.websocket.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "house")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class House extends BaseTimeEntity {

    @Id
    @Column(name = "house_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long houseId;

    @Column(name = "house_name")
    private String houseName;

    @Column(name = "comment")
    private String comment;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "signboard_image_url")
    private String signboardImageUrl;

    @Column(name = "open")
    private boolean open;  // 오픈 상태인지


}
