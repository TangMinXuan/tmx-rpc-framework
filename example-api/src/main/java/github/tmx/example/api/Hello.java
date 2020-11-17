package github.tmx.example.api;

import lombok.*;

import java.io.Serializable;

/**
 * @author: TangMinXuan
 * @created: 2020/10/12 15:02
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
    private Integer id;
    private String message;
}
